package com.boxysystems.scriptmonkey.intellij;

import com.intellij.openapi.application.PathManager;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: shameed
 * Date: Oct 8, 2008
 * Time: 4:25:48 PM
 */
public interface Constants {
  public static final String PLUGIN_ID = "Script Monkey";

  public static final String NEW_LINE = System.getProperty("line.separator");

  public static final String SETTINGS_FILE_NAME = "settings.xml";  

  public static final String PLUGIN_HOME_PAGE = "http://code.google.com/p/scriptmonkey";

  public static final File DEFAULT_HOME_FOLDER = new File(new File(System.getProperty("user.home")), "scriptMonkey");

  public static final File TEMP_FOLDER = new File(System.getProperty("java.io.tmpdir"),"ScriptMonkey");

  public static final File CONFIG_FOLDER = new File(PathManager.getConfigPath(), "ScriptMonkey"); 

  public static final String JS_FOLDER_NAME = "js";

}
