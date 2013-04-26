package com.boxysystems.scriptmonkey.intellij.util;

import com.intellij.openapi.util.io.FileUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: shameed
 * Date: Oct 21, 2008
 * Time: 8:39:35 AM
 */
public class ScriptMonkeyFileUtil extends FileUtil {

  private static final Logger logger = Logger.getLogger(ScriptMonkeyFileUtil.class);

  public static void copy(File fromFile, File toFile) throws IOException {
    if (!toFile.exists()) {
      FileUtil.copy(fromFile, toFile);
    } else if (toFile.exists() && fromFile.lastModified() > toFile.lastModified()) {
      String backUpFileName = toFile.getAbsolutePath() + ".bak";
      FileUtil.copy(toFile, new File(backUpFileName));
      FileUtil.copy(fromFile, toFile);
    }
  }

  public static void copyDir(File fromDir, File toDir) throws IOException {
    copyDir(fromDir, toDir, true);
  }

  public static void copyDir(File fromDir, File toDir, boolean copySystemFiles) throws IOException {
    toDir.mkdirs();
    if (isAncestor(fromDir, toDir, true)) {
      logger.error(fromDir.getAbsolutePath() + " is ancestor of " + toDir + ". Can't copy to itself.");
      return;
    }
    File[] files = fromDir.listFiles();
    for (File file : files) {
      if (!copySystemFiles && file.getName().startsWith(".")) {
        continue;
      }
      if (file.isDirectory()) {
        copyDir(file, new File(toDir, file.getName()), copySystemFiles);
      } else {
        copy(file, new File(toDir, file.getName()));
      }
    }
  }

  public static boolean exists(String filePath) {
    return new File(filePath).exists();
  }
}
