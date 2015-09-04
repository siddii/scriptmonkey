package com.boxysystems.scriptmonkey.intellij.ui;

import com.boxysystems.scriptmonkey.intellij.Constants;
import com.boxysystems.scriptmonkey.intellij.ScriptMonkeySettings;
import com.boxysystems.scriptmonkey.intellij.action.CloseScriptConsoleAction;
import com.boxysystems.scriptmonkey.intellij.action.RerunScriptAction;
import com.boxysystems.scriptmonkey.intellij.action.StopScriptAction;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScriptShellPanel extends JPanel {

    private ShellCommandProcessor shellCommandProcessor;
    private AnAction[] actions;
    private EditorImpl editor;

    private final ExecutorService commandExecutor = Executors.newSingleThreadExecutor();

    public ScriptShellPanel(ShellCommandProcessor cmdProc, AnAction actions[]) {
        this.shellCommandProcessor = cmdProc;
        this.actions = actions;

        setLayout(new BorderLayout());

        final DefaultActionGroup toolbarGroup = new DefaultActionGroup();

        for (int i = 0; i < actions.length; i++) {
            AnAction action = actions[i];
            toolbarGroup.add(action);
        }

        final ActionManager actionManager = ActionManager.getInstance();
        final ActionToolbar toolbar = actionManager.createActionToolbar(Constants.PLUGIN_ID, toolbarGroup, false);

        add(toolbar.getComponent(), BorderLayout.WEST);

        // use IDEA's editor for the console
        Language language = Language.findLanguageByID("JS");
        FileType fileType = language != null ? language.getAssociatedFileType() : null;
        boolean foundType = fileType != null;
        FileType fileTypeHighlighOnly = null;

        if (!foundType) {
            fileType = StdFileTypes.PLAIN_TEXT;
            fileTypeHighlighOnly = FileTypeManagerEx.getInstanceEx().getFileTypeByExtension(".js");
        }

        FileType highlighterFileType = foundType || fileTypeHighlighOnly == null ? fileType : fileTypeHighlighOnly;

        Project project = shellCommandProcessor.getProject();
        assert project != null;

        CommandShellDocument myDocument = new CommandShellDocument((DocumentEx) EditorFactory.getInstance().createDocument(""), this);

        editor = (EditorImpl) (cmdProc.isCommandShell() ?
                EditorFactory.getInstance().createEditor(myDocument, project) :
                EditorFactory.getInstance().createViewer(myDocument, project));

        editor.setHighlighter(HighlighterFactory.createHighlighter(project, highlighterFileType));

        editor.getDocument().addDocumentListener(new CommandShellDocumentListener(this));

        editor.getSettings().setUseTabCharacter(false);

        add(editor.getComponent());

        if (shellCommandProcessor.isCommandShell()) {
            clear();
        }
    }

    public void disposeComponent() {
        // release the editor
        remove(editor.getComponent());
        // vsch: if uncommented the on project close we get double release of editor, when commented we get editor
        // was not released. It would be better to use EditorTextField for the editor but it creates its editor
        // on demand and we need to have it during init. So we live with an unreleased editor.
        //editor.release();

        editor = null;
        shellCommandProcessor = null;
        actions = null;
    }

    public Project getProject() {
        return shellCommandProcessor.getProject();
    }

    public void clear() {
        clear(true);
    }

    public void clear(boolean prompt) {
        // TODO: make document updates handle setting readonly
        //        editor.getDocument().setReadOnly(false);
        getDocument().beginUpdate();
        CommandShellDocument d = (CommandShellDocument) editor.getDocument();
        d.clear();
        if (prompt) {
            printPrompt();
        }
        // TODO: make document updates handle setting readonly
        //        editor.getDocument().setReadOnly(true);
        getDocument().endUpdate();
        // this is done automatically after bulk update
        // setMark();
        requestFocus();
    }

    @Override
    public void requestFocus() {
        editor.getContentComponent().requestFocus();
        editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
    }

    public CommandShellDocument getDocument() {
        return (CommandShellDocument) editor.getDocument();
    }

    public void print(String s) {
        CommandShellDocument d = (CommandShellDocument) editor.getDocument();
        d.beginUpdate();
        d.appendString(s);
        d.endUpdate();
    }

    public void println(final String s) {
        CommandShellDocument d = getDocument();
        d.beginUpdate();
        d.appendString(s + "\n");
        d.endUpdate();
    }

    //TODO: Need to implement this
    public void stopScript() {
        commandExecutor.shutdownNow();
        println("Script cancelled!");
    }

    public void printPrompt() {
        if (shellCommandProcessor.isCommandShell()) {
            print(getPrompt());
        }
    }

    private String getPrompt() {
        return shellCommandProcessor.getPrompt();
    }

    public String executeCommand(String cmd) {
        // TODO: make document updates handle setting readonly
        //        editor.getDocument().setReadOnly(false);
        getDocument().beginUpdate();
        String s = shellCommandProcessor.executeCommand(cmd);
        // TODO: make document updates handle setting readonly
        //        editor.getDocument().setReadOnly(true);
        getDocument().endUpdate();
        return s;
    }

    public String executeCommand(String cmd, int lineOffset) {
        // TODO: make document updates handle setting readonly
        //        editor.getDocument().setReadOnly(false);
        getDocument().beginUpdate();
        String s = shellCommandProcessor.executeCommand(cmd, lineOffset);
        // TODO: make document updates handle setting readonly
        //        editor.getDocument().setReadOnly(true);
        getDocument().endUpdate();
        return s;
    }

    public String executeCommand(String cmd, int lineOffset, int firstLineColumnOffset) {
        // TODO: make document updates handle setting readonly
        //        editor.getDocument().setReadOnly(false);
        getDocument().beginUpdate();
        String s = shellCommandProcessor.executeCommand(cmd, lineOffset, firstLineColumnOffset);
        // TODO: make document updates handle setting readonly
        //        editor.getDocument().setReadOnly(true);
        getDocument().endUpdate();
        return s;
    }

    public ExecutorService getCommandExecutor() {
        return commandExecutor;
    }

    public EditorImpl getEditor() {
        return editor;
    }

    public void applySettings(ScriptMonkeySettings settings) {
        //editor.setBackgroundColor(settings.getCommandShellBackgroundColor());
        //editor.setForeground(settings.getCommandShellForegroundColor());
    }

    public void toggleActions() {
        for (int i = 0; i < actions.length; i++) {
            AnAction action = actions[i];
            if (action instanceof RerunScriptAction) {
                RerunScriptAction rerunScriptAction = (RerunScriptAction) action;
                rerunScriptAction.setEnabled(!rerunScriptAction.isEnabled());
            }

            if (action instanceof StopScriptAction) {
                StopScriptAction stopScriptAction = (StopScriptAction) action;
                stopScriptAction.setEnabled(!stopScriptAction.isEnabled());
            }

            if (action instanceof CloseScriptConsoleAction) {
                CloseScriptConsoleAction closeSctiptConsoleAction = (CloseScriptConsoleAction) action;
                closeSctiptConsoleAction.setEnabled(!closeSctiptConsoleAction.isEnabled());
            }
        }
    }

    public StopScriptAction getStopScriptAction() {
        for (int i = 0; i < actions.length; i++) {
            AnAction action = actions[i];
            if (action instanceof StopScriptAction) {
                return (StopScriptAction) action;
            }
        }
        return null;
    }
}
