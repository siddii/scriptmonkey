package com.boxysystems.scriptmonkey.intellij.ui;

import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import org.apache.log4j.Logger;

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
            if ((cmd.length() == 0) || !cmd.endsWith("\\\n")) {
                // Trim "\\n" combinations
                final String cmdTrimmed = trimContinuations(cmd);

                // TODO: make document updates handle setting readonly
                //        editor.getDocument().setReadOnly(false);
                document.beginUpdate();

                // this needs to be done in the executor thread
                scriptShellPanel.getCommandExecutor().execute(new Runnable() {
                    public void run() {
                        // make sure if script output any text to the window, it is terminated by \n
                        document.beginUpdate();
                        final String result = scriptShellPanel.executeCommand(cmdTrimmed, startLine, firstLineColumnOffset);
                        document.endUpdate(true);

                        // this needs to be done in a dispatch thread
                        document.runWriteAction(new Runnable() {
                            @Override
                            public void run() {
                                if (result != null) {
                                    document.beginUpdate();
                                    scriptShellPanel.print(result + "\n");
                                    document.endUpdate(true);
                                }
                                scriptShellPanel.printPrompt();
                                // this is done by the last end update
                                //scriptShellPanel.setMark();
                                // TODO: make document updates handle setting readonly
                                //        editor.getDocument().setReadOnly(true);
                                document.endUpdate();
                                //logger.error("mark: " + document.getMarkOffset() + ", textLength: " + document.getTextLength());
                            }
                        });
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
