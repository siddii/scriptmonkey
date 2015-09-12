package com.boxysystems.scriptmonkey.intellij;

import com.boxysystems.scriptmonkey.intellij.action.ClearEditorAction;
import com.boxysystems.scriptmonkey.intellij.action.OpenHelpAction;
import com.boxysystems.scriptmonkey.intellij.action.ShowScriptMonkeyConfigurationAction;
import com.boxysystems.scriptmonkey.intellij.action.StopScriptAction;
import com.boxysystems.scriptmonkey.intellij.ui.ScriptCommandProcessor;
import com.boxysystems.scriptmonkey.intellij.ui.ScriptMonkeyToolWindow;
import com.boxysystems.scriptmonkey.intellij.ui.ScriptShellPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: shameed
 * Date: Oct 6, 2008
 * Time: 2:23:29 PM
 */
public class ScriptMonkeyPlugin implements ProjectComponent {

    private Project project;

    private ScriptMonkeyToolWindow toolWindow = null;

    private ScriptShellPanel commandShellPanel;

    public ScriptMonkeyPlugin(Project project) {
        this.project = project;

        // vsch: this may be a problem if someone has multiple projects open with the same classes in different libraries
        // vsch: TODO: implement a custom class loader that will take the pluginclassloader as the parent and resolve library paths
        // before resorting to the parent

        ApplicationManager.getApplication().getComponent(ScriptMonkeyApplicationComponent.class).augmentClassLoader(project);
    }

    public void projectOpened() {
        toolWindow = new ScriptMonkeyToolWindow(project);
        ScriptCommandProcessor commandProcessor = new ScriptCommandProcessor(ApplicationManager.getApplication(), project, this);

        ClearEditorAction clearEditorAction = new ClearEditorAction();
        ShowScriptMonkeyConfigurationAction showConfigurationAction = new ShowScriptMonkeyConfigurationAction();
        StopScriptAction stopScriptAction = new StopScriptAction();
        OpenHelpAction openHelpAction = new OpenHelpAction();

        AnAction commandShellActions[] = {clearEditorAction, stopScriptAction, showConfigurationAction, openHelpAction };

        commandProcessor.setCommandShell(true);
        commandShellPanel = new ScriptShellPanel(commandProcessor, commandShellActions);

        commandShellPanel.applySettings(ScriptMonkeyApplicationComponent.getInstance().getSettings());
        clearEditorAction.setScriptShellPanel(commandShellPanel);
        toolWindow.addContentPanel("JS Shell", commandShellPanel);
        commandProcessor.addGlobalVariable("window", commandShellPanel);

        // now that all is setup we can run the command.
        commandProcessor.processCommandLine();
    }

    public Project getProject() {
        return project;
    }

    public ScriptMonkeyToolWindow getToolWindow() {
        return toolWindow;
    }

    public void projectClosed() {
        if (toolWindow != null) {
            toolWindow.unregisterToolWindow();
        }

        toolWindow = null;
        commandShellPanel = null;
        project = null;
    }

    public void initComponent() {
        // empty
    }

    public void disposeComponent() {
        // empty
    }

    @NotNull
    public String getComponentName() {
        return this.getClass().getName();
    }

    public static ScriptMonkeyPlugin getInstance(Project project) {
        return project.getComponent(ScriptMonkeyPlugin.class);
    }

    public ScriptShellPanel getCommandShellPanel() {
        return commandShellPanel;
    }
}
