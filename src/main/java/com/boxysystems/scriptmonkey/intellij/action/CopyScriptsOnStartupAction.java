package com.boxysystems.scriptmonkey.intellij.action;

import com.boxysystems.scriptmonkey.intellij.Constants;
import com.boxysystems.scriptmonkey.intellij.util.JarExploder;
import com.boxysystems.scriptmonkey.intellij.util.ScriptMonkeyFileUtil;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: shameed
 * Date: Oct 8, 2008
 * Time: 4:22:51 PM
 */
public class CopyScriptsOnStartupAction {


  private final static Logger logger = Logger.getLogger(CopyScriptsOnStartupAction.class);
  private File pluginFolder;

  public CopyScriptsOnStartupAction() {
    pluginFolder = getPluginFolder();

    if (!Constants.TEMP_FOLDER.exists()) {
      Constants.TEMP_FOLDER.mkdir();
    }
  }

  public void copyScripts(File targetFolder) {
    try {
      List<File> jsFolders = new ArrayList<File>();
      collectJSFolders(jsFolders, pluginFolder);

      for (File jsFolder : jsFolders) {
        File tmpDir = new File(Constants.TEMP_FOLDER, jsFolder.getName() + new Random().nextInt());
        logger.info("Creating temporary folder for staging files " + tmpDir);
        if (tmpDir.mkdir()) {
          String folderName = jsFolder.getName().endsWith(".jar") ? jsFolder.getName().replaceAll(".jar", "") : jsFolder.getName();
          logger.info("folderName = " + folderName);
          File resourceTmpFolder = new File(tmpDir, folderName);
          logger.info("resourceTmpFolder = " + resourceTmpFolder);
          if (resourceTmpFolder.mkdir()) {
            if (jsFolder.isFile()) {
              logger.info("Exploding jar..."+jsFolder);
              JarExploder.explodeJar(resourceTmpFolder, jsFolder);
            }
            ScriptMonkeyFileUtil.copyDir(tmpDir, targetFolder);
          }
        }
      }
    } catch (IOException e) {
      logger.error("Error copying scripts !", e);
    }
  }

  private void collectJSFolders(List<File> resources, File targetResourceFolder) {
    if (targetResourceFolder != null) {
      File childDirs[] = targetResourceFolder.listFiles();
      if (childDirs != null) {
        for (File childDir : childDirs) {
          if (childDir.isFile() && childDir.getName().equals(Constants.JS_FOLDER_NAME + ".jar")) {
            resources.add(childDir);
          }
          collectJSFolders(resources, childDir);
        }
      }
    }
  }

  protected File getPluginFolder() {
    IdeaPluginDescriptor[] plugins = PluginManager.getPlugins();
    for (int i = 0; i < plugins.length; i++) {
      IdeaPluginDescriptor plugin = plugins[i];
      if (plugin.getName().equals(Constants.PLUGIN_ID)) {
        return plugin.getPath();
      }
    }
    return null;
  }
}
