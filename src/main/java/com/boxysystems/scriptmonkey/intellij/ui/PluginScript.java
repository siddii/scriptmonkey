package com.boxysystems.scriptmonkey.intellij.ui;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: siddique
 * Date: Oct 11, 2008
 * Time: 2:44:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class PluginScript implements Serializable, Cloneable {

  public enum RUN_MODE {
    INTELLIJ_STARTUP("While IntelliJ Starts"),
    PROJECT_OPEN("While Project Opens"),
    PROJECT_CLOSE("While Project Closes"),
    INTELLIJ_SHUTDOWN("While IntelliJ Shutdowns");

    private String value;

    RUN_MODE(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  private boolean enabled = false;
  private String filePath;
  private RUN_MODE runMode = RUN_MODE.PROJECT_OPEN;

  public PluginScript() {
  }

  public PluginScript(boolean enabled, String filePath, RUN_MODE runMode) {
    this.enabled = enabled;
    this.filePath = filePath;
    this.runMode = runMode;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public void setRunMode(RUN_MODE runMode) {
    this.runMode = runMode;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public String getFilePath() {
    return filePath;
  }

  public RUN_MODE getRunMode() {
    return runMode;
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PluginScript that = (PluginScript) o;

    if (enabled != that.enabled) {
      return false;
    }
    if (filePath != null ? !filePath.equals(that.filePath) : that.filePath != null) {
      return false;
    }
    if (runMode != that.runMode) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    int result;
    result = (enabled ? 1 : 0);
    result = 31 * result + (filePath != null ? filePath.hashCode() : 0);
    result = 31 * result + (runMode != null ? runMode.hashCode() : 0);
    return result;
  }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            //ignore this
        }
        return null;
    }
}
