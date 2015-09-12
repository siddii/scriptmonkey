package com.boxysystems.scriptmonkey.intellij;

import com.boxysystems.scriptmonkey.intellij.action.CopyScriptsOnStartupAction;
import com.boxysystems.scriptmonkey.intellij.icons.Icons;
import com.boxysystems.scriptmonkey.intellij.ui.PluginScript;
import com.boxysystems.scriptmonkey.intellij.ui.ScriptMonkeyConfigurationForm;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;

public class ScriptMonkeyApplicationComponent implements ApplicationComponent, Configurable {
    private static final Logger logger = Logger.getLogger(ScriptMonkeyApplicationComponent.class);

    private ScriptMonkeyConfigurationForm form = null;
    private ScriptMonkeySettings settings = null;
    private PluginScriptRunner pluginScriptRunner = new PluginScriptRunner();
    private CopyScriptsOnStartupAction copyScriptsAction;

    public ScriptMonkeyApplicationComponent() {
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return this.getClass().getName();
    }

    public void setSettings(ScriptMonkeySettings settings) {
        this.settings = settings;
    }

    public void initComponent() {
        initSettings();
        copyScriptsAction = new CopyScriptsOnStartupAction();
        copyScriptsAction.copyScripts(new File(settings.getHomeFolder()));
        pluginScriptRunner.runPluginScripts(this, PluginScript.RUN_MODE.INTELLIJ_STARTUP, false);
    }

    private void initSettings() {
        settings = ScriptMonkeySettings.getInstance();
    }

    private File setupConfigDir() {
        if (!Constants.CONFIG_FOLDER.exists() && !Constants.CONFIG_FOLDER.mkdir()) {
            return null;
        }
        return Constants.CONFIG_FOLDER;
    }

    public void disposeComponent() {
        pluginScriptRunner.runPluginScripts(this, PluginScript.RUN_MODE.INTELLIJ_SHUTDOWN, true);
    }

    @Nls
    public String getDisplayName() {
        return Constants.PLUGIN_ID;
    }

    @Nullable
    public Icon getIcon() {
        return Icons.MONKEY_ICON;
    }

    @Nullable
    @NonNls
    public String getHelpTopic() {
        return null;
    }

    public JComponent createComponent() {
        if (form == null) {
            form = new ScriptMonkeyConfigurationForm();
        }
        return form.getRootComponent();
    }

    public boolean isModified() {
        if (form != null && settings != null) {
            return form.isModified(settings);
        }
        return false;
    }

    public void apply() throws ConfigurationException {
        if (form != null && settings != null) {
            form.getData(settings);
            Project[] projects = ProjectManager.getInstance().getOpenProjects();
            for (Project project : projects) {
                ScriptMonkeyPlugin.getInstance(project).getCommandShellPanel().applySettings(settings);
            }
            //      SerializationUtil.toXml(settingsFile.getAbsolutePath(), settings);
        }
    }

    public void reset() {
        if (form != null && settings != null) {
            form.setData(settings);
        }
    }

    public void disposeUIResources() {
        form = null;
    }

    public static ScriptMonkeyApplicationComponent getInstance() {
        return ApplicationManager.getApplication().getComponent(ScriptMonkeyApplicationComponent.class);
    }

    public ScriptMonkeySettings getSettings() {
        return settings;
    }

    public CopyScriptsOnStartupAction getCopyScriptsAction() {
        return copyScriptsAction;
    }

    public ScriptMonkeyConfigurationForm getForm() {
        return form;
    }
}
