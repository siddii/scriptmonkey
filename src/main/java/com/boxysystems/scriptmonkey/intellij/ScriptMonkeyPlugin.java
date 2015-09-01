package com.boxysystems.scriptmonkey.intellij;

import com.boxysystems.scriptmonkey.intellij.action.ClearEditorAction;
import com.boxysystems.scriptmonkey.intellij.action.OpenHelpAction;
import com.boxysystems.scriptmonkey.intellij.action.ShowScriptMonkeyConfigurationAction;
import com.boxysystems.scriptmonkey.intellij.ui.ScriptCommandProcessor;
import com.boxysystems.scriptmonkey.intellij.ui.ScriptMonkeyToolWindow;
import com.boxysystems.scriptmonkey.intellij.ui.ScriptShellPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;

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


  public ScriptMonkeyPlugin(Project project) throws MalformedURLException {
    this.project = project;
  }

  public void projectOpened() {
    toolWindow = new ScriptMonkeyToolWindow(project);
    ScriptCommandProcessor commandProcessor = new ScriptCommandProcessor(ApplicationManager.getApplication(), project, this);

    ClearEditorAction clearEditorAction = new ClearEditorAction();
    ShowScriptMonkeyConfigurationAction showConfigurationAction = new ShowScriptMonkeyConfigurationAction();
    OpenHelpAction openHelpAction = new OpenHelpAction();

    AnAction commandShellActions[] = {clearEditorAction, showConfigurationAction, openHelpAction};

    commandShellPanel = new ScriptShellPanel(commandProcessor, commandShellActions);
    commandShellPanel.applySettings(ScriptMonkeyApplicationComponent.getInstance().getSettings());
    clearEditorAction.setScriptShellPanel(commandShellPanel);
    commandProcessor.processCommandLine();
    commandProcessor.addGlobalVariable("window", commandShellPanel);
    toolWindow.addContentPanel("JS Shell", commandShellPanel);
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
