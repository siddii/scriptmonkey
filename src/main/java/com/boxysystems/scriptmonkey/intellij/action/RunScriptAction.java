package com.boxysystems.scriptmonkey.intellij.action;

import com.boxysystems.scriptmonkey.intellij.ScriptMonkeyPlugin;
import com.boxysystems.scriptmonkey.intellij.ui.*;
import com.boxysystems.scriptmonkey.intellij.util.ProjectUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.content.Content;
import com.intellij.util.PathUtil;

import javax.swing.*;
import java.io.File;

public class RunScriptAction extends ScriptShellPanelAction {

    private JSFileFilter jsFileFilter = new JSFileFilter();

    public void update(AnActionEvent actionEvent) {
        super.update(actionEvent);
        final Presentation presentation = actionEvent.getPresentation();
        presentation.setEnabled(isEnabled(actionEvent));
    }

    public boolean isEnabled(AnActionEvent event) {
        Project project = ProjectUtil.getProject(event);
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor != null) {
            File scriptFile = getScriptFile(editor);
            return jsFileFilter.accept(scriptFile);
        }
        return false;
    }

    private File getScriptFile(Editor editor) {
        VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
        String scriptFilePath = PathUtil.getLocalPath(file);
        if (scriptFilePath != null) {
            return new File(scriptFilePath);
        }
        return null;
    }

    public void actionPerformed(AnActionEvent event) {

        Project project = ProjectUtil.getProject(event);
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

        if (editor != null) {
            String scriptContent = getEditorContent(editor, project);

            File scriptFile = getScriptFile(editor);

            if (scriptFile != null) {

                ScriptMonkeyPlugin scriptMonkeyPlugin = ScriptMonkeyPlugin.getInstance(project);
                ScriptCommandProcessor commandProcessor = new ScriptCommandProcessor(ApplicationManager.getApplication(), project, scriptMonkeyPlugin);
                commandProcessor.setCommandShell(false);

                String contentName = scriptFile.getName();

                ScriptMonkeyToolWindow toolWindow = scriptMonkeyPlugin.getToolWindow();

                Content content = toolWindow.getContentManager().findContent(contentName);

                ScriptShellPanel panel;
                if (content == null) {
                    RerunScriptAction rerunAction = new RerunScriptAction();
                    StopScriptAction stopScriptAction = new StopScriptAction();
                    CloseScriptConsoleAction closeAction = new CloseScriptConsoleAction(contentName);
                    OpenHelpAction openHelpAction = new OpenHelpAction();

                    AnAction scriptConsoleActions[] = {rerunAction, stopScriptAction, closeAction, openHelpAction};

                    panel = new ScriptShellPanel(commandProcessor, scriptConsoleActions);
                    content = toolWindow.addContentPanel(contentName, panel);
                } else {
                    ScriptShellTabContent tabContent = (ScriptShellTabContent) content.getComponent();
                    panel = tabContent.getScriptShellPanel();
                    panel.toggleActions();
                }
                commandProcessor.addGlobalVariable("window", panel);
                panel.clear();
                panel.println("Running script '" + scriptFile.getAbsolutePath() + "' ...");
                ScriptCommandProcessor.ScriptRunningTask task = commandProcessor.processScript(scriptContent, scriptFile.getAbsolutePath(), new RunScriptActionCallback(panel), panel.getStopScriptAction());

                toolWindow.activate();
                toolWindow.getContentManager().setSelectedContent(content);
            }
        }
    }

    private String getEditorContent(Editor editor, Project project) {
        final PsiDocumentManager pdm = PsiDocumentManager.getInstance(project);
        pdm.commitDocument(editor.getDocument());
        PsiFile file = pdm.getPsiFile(editor.getDocument());
        if (file != null) {
            return file.getText();
        }
        return "";
    }

    private class RunScriptActionCallback implements ScriptProcessorCallback {
        private ScriptShellPanel panel;

        private RunScriptActionCallback(ScriptShellPanel panel) {
            this.panel = panel;
        }

        public void success() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    panel.println("Successfully processed!");
                    finishUp();
                }
            });
        }

        public void failure(final Throwable throwable) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    panel.println("Error running script ....");
                    panel.println(throwable.toString());
                    finishUp();
                }
            });
        }

        public void println(String msg) {
            panel.println(msg);
        }

        public void finishUp() {
            panel.toggleActions();
        }

    }
}
