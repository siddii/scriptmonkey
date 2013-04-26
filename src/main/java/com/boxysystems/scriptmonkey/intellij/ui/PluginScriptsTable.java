package com.boxysystems.scriptmonkey.intellij.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.OpenSourceUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: siddique
 * Date: Oct 11, 2008
 * Time: 2:30:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class PluginScriptsTable extends JTable {
  private PluginScriptsTableModel pluginScriptsTableModel;

  public PluginScriptsTable(PluginScriptsTableModel pluginScriptsTableModel) {
    super(pluginScriptsTableModel);
    this.pluginScriptsTableModel = pluginScriptsTableModel;
    setRunModeColumnRenderer(this.getColumnModel().getColumn(2));

    this.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    this.getColumnModel().getColumn(PluginScriptsTableModel.ENABLED_COL_IDX).setPreferredWidth(60);
    this.getColumnModel().getColumn(PluginScriptsTableModel.FILEPATH_COL_IDX).setPreferredWidth(500);
    this.getColumnModel().getColumn(PluginScriptsTableModel.RUN_MODE_COL_IDX).setPreferredWidth(120);
    this.setSelectionBackground(Color.lightGray);
  }

  private void setRunModeColumnRenderer(TableColumn runModeColumn) {

    JComboBox runModeComboBox = new JComboBox();

    PluginScript.RUN_MODE[] runModes = PluginScript.RUN_MODE.values();

    for (PluginScript.RUN_MODE runMode : runModes) {
      runModeComboBox.addItem(runMode.getValue());
    }

    runModeColumn.setCellEditor(new DefaultCellEditor(runModeComboBox));

    DefaultTableCellRenderer renderer =
      new DefaultTableCellRenderer();
    renderer.setToolTipText("Choose when to run...");
    runModeColumn.setCellRenderer(renderer);
  }

  public PluginScriptsTableModel getPluginScriptsTableModel() {
    return pluginScriptsTableModel;
  }

  public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
    Component component = super.prepareRenderer(renderer, row, column);

    if (getRowCount() > 0) {
      String filePath = (String) getModel().getValueAt(row, PluginScriptsTableModel.FILEPATH_COL_IDX);
      File scriptFile = new File(filePath);
      if (!scriptFile.exists()) {
        component.setForeground(Color.RED);
      } else {
        component.setForeground(Color.BLACK);
      }
      Boolean isEnabled = (Boolean) getModel().getValueAt(row, PluginScriptsTableModel.ENABLED_COL_IDX);
      if (isEnabled && scriptFile.exists()) {
        Font originalFont = component.getFont();
        Font boldFont = originalFont.deriveFont(Font.BOLD);
        component.setFont(boldFont);
      } else {
        Font originalFont = component.getFont();
        Font plainFont = originalFont.deriveFont(Font.PLAIN);
        component.setFont(plainFont);
      }
    }
    return component;
  }
}
