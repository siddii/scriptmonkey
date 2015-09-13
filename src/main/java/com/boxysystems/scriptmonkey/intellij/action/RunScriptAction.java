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
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.File;

public class RunScriptAction extends ScriptShellPanelAction {
    private static final Logger logger = Logger.getLogger(RunScriptAction.class);

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

    // vsch: changed to re-use the same commandProcessor so that globals are preserved accross runs to allow for
    // more intuitive re-run as if in the JS Shell which keeps the same processor for multiple commands and enabled
    // this as a script shell panel so that these globals can be examined on the command line for debug purposes
    public void actionPerformed(AnActionEvent event) {
        Project project = ProjectUtil.getProject(event);
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

        if (editor != null) {
            String scriptContent = getEditorContent(editor, project);

            File scriptFile = getScriptFile(editor);

            if (scriptFile != null) {
                ScriptMonkeyPlugin scriptMonkeyPlugin = ScriptMonkeyPlugin.getInstance(project);
                String contentName = scriptFile.getName();
                ScriptMonkeyToolWindow toolWindow = scriptMonkeyPlugin.getToolWindow();
                Content content = toolWindow.getContentManager().findContent(contentName);
                ScriptShellPanel panel;

                ScriptCommandProcessor commandProcessor;
                if (content == null) {
                    RerunScriptAction rerunAction = new RerunScriptAction();
                    StopScriptAction stopScriptAction = new StopScriptAction();
                    CloseScriptConsoleAction closeAction = new CloseScriptConsoleAction(contentName);
                    OpenHelpAction openHelpAction = new OpenHelpAction();

                    AnAction scriptConsoleActions[] = {rerunAction, stopScriptAction, closeAction, openHelpAction};

                    commandProcessor = new ScriptCommandProcessor(ApplicationManager.getApplication(), project, scriptMonkeyPlugin);

                    // vsch: make it a panel so that we can examine globals after the run for debug purposes
                    commandProcessor.setCommandShell(true);
                    panel = new ScriptShellPanel(commandProcessor, scriptConsoleActions);

                    content = toolWindow.addContentPanel(contentName, panel);
                    commandProcessor.addGlobalVariable("window", panel);
                }
                else {
                    ScriptShellTabContent tabContent = (ScriptShellTabContent) content.getComponent();
                    panel = tabContent.getScriptShellPanel();
                    panel.toggleActions();
                    commandProcessor = (ScriptCommandProcessor) panel.getShellCommandProcessor();
                }

                panel.clear();
                panel.println("Running script '" + scriptFile.getAbsolutePath() + "' ...");

                panel.getDocument().beginUpdate();
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

        @Override
        public void success() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    panel.println("Successfully processed!");
                }
            });
        }

        @Override
        public void failure(final Throwable throwable) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    panel.println("Error running script ....");
                    panel.println(throwable.toString());
                }
            });
        }

        @Override
        public void done() {
            logger.info("done called");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        panel.getDocument().endUpdate(true, false);
                        panel.printPrompt();
                        panel.toggleActions();
                        logger.info("done complete");
                    }catch (Throwable e) {
                        logger.info("done exception: " + e.getMessage());
                    }
                }
            });
        }

        @Override
        public void println(String msg) {
            panel.println(msg);
        }

        @Override
        public void progressln(String msg) {
            panel.progressln(msg);
        }

        @Override
        public boolean hadOutput() {
            return panel.hadOutput();
        }

        @Override
        public void startProgress() {
            panel.startProgress();
        }

        @Override
        public void endProgress() {
            panel.endProgress();
        }
    }
}
