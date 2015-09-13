package com.boxysystems.scriptmonkey.intellij.ui;

import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import org.apache.log4j.Logger;

import java.util.concurrent.Executors;

/**
 * Created by IntelliJ IDEA.
 * User: shameed
 * Date: Oct 20, 2008
 * Time: 12:38:17 PM
 */
public class CommandShellDocumentListener implements DocumentListener {
    private static final Logger logger = Logger.getLogger(CommandShellDocumentListener.class);
    private ScriptShellPanel scriptShellPanel;

    public CommandShellDocumentListener(ScriptShellPanel scriptShellPanel) {
        this.scriptShellPanel = scriptShellPanel;
    }

    @Override
    public void beforeDocumentChange(DocumentEvent e) {
    }

    protected boolean allWhiteSpace(String text) {
        int iMax = text.length();
        for (int i = 0; i < iMax; i++) {
            if (text.charAt(i) != ' ' && text.charAt(i) != '\n') return false;
        }
        return true;
    }

    @Override
    public void documentChanged(DocumentEvent e) {
        final CommandShellDocument document = scriptShellPanel.getDocument();

        if (document.isUpdating() || (e.getOldLength() != 0 && !e.isWholeTextReplaced())
                || e.getOffset() < document.getMarkOffset()) {
            // editing above the prompt, reset the mark
            if (!document.isUpdating() && e.getOffset() < document.getMarkOffset()) {
                //logger.error("Resetting mark: e.offset: " + e.getOffset() + ", mark: " + document.getMarkOffset());
                if (!document.isUpdating()) document.setMark();
            }
            return;
        }

        // this is either a replacement of the whole thing or an insert
        //        scriptShellPanel.getEditor().getCaretModel().getPrimaryCaret().moveToOffset(scriptShellPanel
        // .getEditor().getDocument().getTextLength());
        if (insertContains(e, '\n')) {
            String s = document.getMarkedText();
            int i = s.length();
            //while ((i > 0) && (s.charAt(i - 1) == '\n')) {
            //    i--;
            //}
            String cmd = s.substring(0, i);
            final int startLine = document.getMarkLine();
            final int firstLineColumnOffset = document.getMarkColumn();

            // Handle multi-line input
            if (!allWhiteSpace(cmd) && !cmd.endsWith("\\\n")) {
                // Trim "\\n" combinations
                final String cmdTrimmed = trimContinuations(cmd);

                document.beginUpdate();

                // this needs to be done in a separate thread not to lockup the ui
                Executors.newCachedThreadPool().execute(new Runnable() {
                    public void run() {
                        // make sure if script output any text to the window, it is terminated by \n
                        document.beginUpdate();
                        scriptShellPanel.executeCommand(cmdTrimmed, startLine, firstLineColumnOffset);
                        document.endUpdate(true, false);

                        logger.debug("waiting for update to be done");
                        while (document.isUpdating(1)) {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e1) {
                            }
                        }
                        logger.debug("update is done");

                        scriptShellPanel.printPrompt();
                        document.endUpdate();
                        //logger.error("mark: " + document.getMarkOffset() + ", textLength: " + document.getTextLength());
                    }
                });
            }
            else {
                // just append prompt
                final String finalCmd = cmd;
                Executors.newCachedThreadPool().execute(new Runnable() {
                    public void run() {
                        document.beginUpdate();
                        if (finalCmd.length() > 1) scriptShellPanel.print(finalCmd);
                        scriptShellPanel.printPrompt();
                        document.endUpdate();
                    }
                });
            }
        }
    }

    private boolean insertContains(DocumentEvent e, char c) {
        CharSequence s;

        s = e.getNewFragment();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                return true;
            }
        }
        return false;
    }

    private String trimContinuations(String text) {
        int i;
        while ((i = text.indexOf("\\\n")) >= 0) {
            text = text.substring(0, i) + text.substring(i + 1, text.length());
        }
        return text;
    }
}
