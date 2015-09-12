package com.boxysystems.scriptmonkey.intellij;

import com.boxysystems.scriptmonkey.intellij.ui.PluginScript;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: shameed
 * Date: Oct 17, 2008
 * Time: 3:24:06 PM
 */
public class ScriptMonkeyProjectComponent implements ProjectComponent {

    private PluginScriptRunner pluginScriptRunner = null;
    private Project project;

    public ScriptMonkeyProjectComponent(Project project) {
        this.project = project;
        pluginScriptRunner = new PluginScriptRunner(project, ScriptMonkeyPlugin.getInstance(project));
    }

    public void projectOpened() {
        pluginScriptRunner.runPluginScripts(PluginScript.RUN_MODE.PROJECT_OPEN);
    }

    public void projectClosed() {
        pluginScriptRunner.runPluginScriptsSynchronously(PluginScript.RUN_MODE.PROJECT_CLOSE);
        pluginScriptRunner.disposeComponent();
        pluginScriptRunner = null;
        project = null;
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return this.getClass().getName();
    }

    public void initComponent() {

    }

    public void disposeComponent() {
    }
}
