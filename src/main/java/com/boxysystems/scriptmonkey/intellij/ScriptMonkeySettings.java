package com.boxysystems.scriptmonkey.intellij;

import com.boxysystems.scriptmonkey.intellij.ui.PluginScript;

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
public class ScriptMonkeySettings implements Serializable {

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
}
