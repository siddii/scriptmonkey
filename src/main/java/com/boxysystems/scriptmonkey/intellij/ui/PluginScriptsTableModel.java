package com.boxysystems.scriptmonkey.intellij.ui;

import javax.swing.table.AbstractTableModel;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: siddique
 * Date: Oct 11, 2008
 * Time: 2:52:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class PluginScriptsTableModel extends AbstractTableModel {
  String[] columnNames = {"Enabled ?",
    "File path",
    "When to run ?"};


  private List<PluginScript> pluginScripts = new ArrayList<PluginScript>();

  public final static int ENABLED_COL_IDX = 0;
  public final static int FILEPATH_COL_IDX = 1;
  public final static int RUN_MODE_COL_IDX = 2;


  public PluginScriptsTableModel(List<PluginScript> pluginScripts) {
    this.pluginScripts = pluginScripts;
  }

  public void addPluginScripts(List<PluginScript> pluginScripts) {
    for (PluginScript pluginScript : pluginScripts) {
      this.pluginScripts.add((PluginScript) pluginScript.clone());
    }
    fireTableDataChanged();
  }

  public void addPluginScript(PluginScript pluginScript) {
    this.pluginScripts.add((PluginScript) pluginScript.clone());
    fireTableDataChanged();
  }

  public void updatePluginScript(PluginScript script) {
    for (Iterator<PluginScript> pluginScriptIterator = pluginScripts.iterator(); pluginScriptIterator.hasNext();) {
      PluginScript pluginScript = pluginScriptIterator.next();
      if (pluginScript.getFilePath().equals(script.getFilePath()) && pluginScript.getRunMode().equals(script.getRunMode())) {
        pluginScript.setEnabled(script.isEnabled());
        break;
      }
    }
    fireTableDataChanged();
  }


  public void removePluginScript(int rowIndex) {
    this.pluginScripts.remove(rowIndex);
    fireTableDataChanged();
  }

  public int getColumnCount() {
    return columnNames.length;
  }

  public int getRowCount() {
    return pluginScripts.size();
  }

  public String getColumnName(int col) {
    return columnNames[col];
  }

  public Object getValueAt(int row, int col) {
    switch (col) {
      case ENABLED_COL_IDX:
        return pluginScripts.get(row).isEnabled();
      case FILEPATH_COL_IDX:
        return pluginScripts.get(row).getFilePath();
      case RUN_MODE_COL_IDX:
        return pluginScripts.get(row).getRunMode().getValue();
    }
    return null;
  }

  public Class getColumnClass(int c) {
    return getValueAt(0, c).getClass();
  }

  /*
  * Don't need to implement this method unless your table's
  * editable.
  */
  public boolean isCellEditable(int row, int col) {
    return true;
  }

  /*
  * Don't need to implement this method unless your table's
  * data can change.
  */
  public void setValueAt(Object value, int row, int col) {
    switch (col) {
      case ENABLED_COL_IDX:
        pluginScripts.get(row).setEnabled(Boolean.parseBoolean(value.toString()));
        break;
      case FILEPATH_COL_IDX:
        pluginScripts.get(row).setFilePath(value.toString());
        break;
      case RUN_MODE_COL_IDX:
        for (int i = 0; i < PluginScript.RUN_MODE.values().length; i++) {
          PluginScript.RUN_MODE runMode = PluginScript.RUN_MODE.values()[i];
          if (runMode.getValue().equals(value)) {
            pluginScripts.get(row).setRunMode(runMode);
            break;
          }
        }
    }
    fireTableCellUpdated(row, col);
  }

  public List<PluginScript> getPluginScripts() {
    return pluginScripts;
  }

}
