package com.boxysystems.scriptmonkey.intellij.util;

import com.boxysystems.scriptmonkey.intellij.ScriptMonkeySettings;
import com.boxysystems.scriptmonkey.intellij.ui.PluginScript;
import com.thoughtworks.xstream.XStream;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NonNls;

import java.io.*;

public class SerializationUtil {
  @NonNls
  private static final Logger logger = Logger.getLogger(SerializationUtil.class);

  private static XStream xstream = new XStream();
  private static final String ENCODING_TYPE = "UTF-8";

  static {
    xstream.alias("settings", ScriptMonkeySettings.class);
    xstream.alias("pluginscript", PluginScript.class);
  }

  public static Object fromXml(String fullFileName) {
    Reader fileReader = null;
    try {
      BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(fullFileName));
      fileReader = new InputStreamReader(inputStream, ENCODING_TYPE);
      return xstream.fromXML(fileReader);
    } catch (FileNotFoundException e) {
      return null;
    }
    catch (Throwable e) {
      logger.error("Error reading " + fullFileName, e);
      return null;
    }
    finally {
      if (fileReader != null) {
        try {
          fileReader.close();
        } catch (IOException e) {
          logger.error(e);
        }
      }
    }
  }

  public static void toXml(String fullFileName, Object object) {
    Writer writer = null;
    try {
      BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(fullFileName));
      writer = new OutputStreamWriter(stream, ENCODING_TYPE);
      xstream.toXML(object, writer);
    } catch (Exception e) {
      logger.error("Error writing " + fullFileName, e);
    }
    finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          logger.error(e);
        }
      }
    }
  }
}
