package com.boxysystems.scriptmonkey.intellij;

import com.boxysystems.scriptmonkey.intellij.ui.PluginScript;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: shameed
 * Date: Oct 16, 2008
 * Time: 2:28:52 PM
 */
@State(
        name = "ScriptMonkeySettings",
        storages = {
                @Storage(id = "ScriptMonkeySettings", file = StoragePathMacros.APP_CONFIG + "/ScriptMonkeySettings.xml")
        }
)
public class ScriptMonkeySettings implements ApplicationComponent, PersistentStateComponent<ScriptMonkeySettings> {

    private String homeFolder = Constants.DEFAULT_HOME_FOLDER.getAbsolutePath();
    private List<PluginScript> pluginScripts = new ArrayList<PluginScript>();
    private Color commandShellBackgroundColor = Color.WHITE;
    private Color commandShellForegroundColor = Color.BLACK;

    public ScriptMonkeySettings() {
    }

    public String getHomeFolder() {
        return homeFolder;
    }

    public void setHomeFolder(String homeFolder) {
        this.homeFolder = homeFolder;
    }

    public List<PluginScript> getPluginScripts() {
        return pluginScripts;
    }

    public void setPluginScripts(List<PluginScript> pluginScripts) {
        this.pluginScripts = pluginScripts;
    }

    public Color getCommandShellBackgroundColor() {
        return commandShellBackgroundColor;
    }

    public void setCommandShellBackgroundColor(Color commandShellBackgroundColor) {
        this.commandShellBackgroundColor = commandShellBackgroundColor;
    }

    public Color getCommandShellForegroundColor() {
        return commandShellForegroundColor;
    }

    public void setCommandShellForegroundColor(Color commandShellForegroundColor) {
        this.commandShellForegroundColor = commandShellForegroundColor;
    }

    @Nullable
    public ScriptMonkeySettings getState() {
        return this;
    }

    public void loadState(ScriptMonkeySettings scriptMonkeySettings) {
        XmlSerializerUtil.copyBean(scriptMonkeySettings, this);
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return this.getClass().getName();
    }

    public static ScriptMonkeySettings getInstance() {
        return ApplicationManager.getApplication().getComponent(ScriptMonkeySettings.class);
    }
}
