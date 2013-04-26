package com.boxysystems.scriptmonkey.intellij.ui;

import com.boxysystems.scriptmonkey.intellij.Constants;
import com.boxysystems.scriptmonkey.intellij.ScriptMonkeySettings;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: siddique
 * Date: Oct 9, 2008
 * Time: 7:11:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScriptMonkeyConfigurationForm {
  private JTabbedPane tabbedPane1;
  private JPanel rootPanel;
  private JTextField txtHomeFolder;
  private JButton btnBrowseHomeDir;
  private JTable pluginScriptsTable;
  private JPanel spacerPanel;
  private JScrollPane pluginScriptsTableScrollPane;
  private JButton addButton;
  private JButton removeButton;
  private JEditorPane commandShellEditorPane;
  private JButton changeBackgroundColorButton;
  private JButton changeForegroundColorButton;
  private PluginScriptsTable table;
  private PluginScriptsTableModel pluginScriptsTableModel;

  private final FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(false, true, false, false, false, false);

  private boolean modified = false;

  public ScriptMonkeyConfigurationForm() {

    btnBrowseHomeDir.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        final VirtualFile[] files = FileChooser.chooseFiles(getRootComponent(), fileChooserDescriptor);
        if (files.length == 1) {
          String filePath = files[0].getPresentableUrl();
          txtHomeFolder.setText(filePath);
        }
      }
    });
    addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addPluginScript();
      }
    });

    removeButton.setEnabled(false);
    removeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        removePluginScript();
      }
    });

    changeBackgroundColorButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Color originalBackgroundColor = commandShellEditorPane.getBackground();
        Color selectedBackgroundColor = JColorChooser.showDialog(changeBackgroundColorButton.getParent(), "Choose background color", originalBackgroundColor);
        if (!originalBackgroundColor.equals(selectedBackgroundColor)) {
          commandShellEditorPane.setBackground(selectedBackgroundColor);
          modified = true;
        }
      }
    });
    changeForegroundColorButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Color originalForegroundColor = commandShellEditorPane.getForeground();
        Color selectedForegroundColor = JColorChooser.showDialog(changeBackgroundColorButton.getParent(), "Choose foreground color", originalForegroundColor);
        if (!originalForegroundColor.equals(selectedForegroundColor)) {
          commandShellEditorPane.setForeground(selectedForegroundColor);
          modified = true;
        }
      }
    });
  }

  private void createUIComponents() {
    pluginScriptsTableModel = new PluginScriptsTableModel(new ArrayList<PluginScript>());
    table = new PluginScriptsTable(pluginScriptsTableModel);
    table.setPreferredScrollableViewportSize(new Dimension(500, 70));
    table.setFillsViewportHeight(true);

    table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (table.getSelectedRow() > -1) {
          removeButton.setEnabled(true);
        } else {
          removeButton.setEnabled(false);
        }
      }
    });

    TableCellEditor cellEditor = table.getDefaultEditor(Boolean.class);
    cellEditor.addCellEditorListener(new CellEditorListener() {

      public void editingStopped(ChangeEvent e) {
        pluginScriptsTableModel.fireTableDataChanged();
      }

      public void editingCanceled(ChangeEvent e) {
        pluginScriptsTableModel.fireTableDataChanged();
      }
    });


    table.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
          if (table.getSelectedRow() > -1 && pluginScriptsTableModel.getPluginScripts().size() > 0) {
            PluginScriptsTablePopupMenu popupMenu = new PluginScriptsTablePopupMenu(table, pluginScriptsTableModel.getPluginScripts().get(table.getSelectedRow()));
            popupMenu.show(table, e.getPoint().x, e.getPoint().y);
          }
        }
      }
    });

    pluginScriptsTableScrollPane = new JScrollPane(table);
  }

  public JComponent getRootComponent() {
    return rootPanel;
  }

  public boolean isModified(ScriptMonkeySettings settings) {
    if (pluginScriptsTableModel.getPluginScripts() != null && !pluginScriptsTableModel.getPluginScripts().equals(settings.getPluginScripts())) {
      return true;
    }

    if ((txtHomeFolder.getText() != null) && !txtHomeFolder.getText().equals(settings.getHomeFolder())) {
      return true;
    }

    if (commandShellEditorPane.getBackground() != null && !commandShellEditorPane.getBackground().equals(settings.getCommandShellBackgroundColor())) {
      return true;
    }

    if (commandShellEditorPane.getForeground() != null && !commandShellEditorPane.getForeground().equals(settings.getCommandShellForegroundColor())) {
      return true;
    }
    return false;
  }

  public void setData(ScriptMonkeySettings settings) {
    txtHomeFolder.setText(settings.getHomeFolder());
    pluginScriptsTableModel.addPluginScripts(settings.getPluginScripts());
    commandShellEditorPane.setBackground(settings.getCommandShellBackgroundColor());
    commandShellEditorPane.setForeground(settings.getCommandShellForegroundColor());
  }

  public void getData(ScriptMonkeySettings settings) {
    settings.setHomeFolder(txtHomeFolder.getText());
    List<PluginScript> pluginScripts = pluginScriptsTableModel.getPluginScripts();
    settings.setPluginScripts(pluginScripts);
    settings.setCommandShellBackgroundColor(commandShellEditorPane.getBackground());
    settings.setCommandShellForegroundColor(commandShellEditorPane.getForeground());
  }

  private void addPluginScript() {
    JFileChooser pluginScriptChooser = new JFileChooser(Constants.DEFAULT_HOME_FOLDER);
    pluginScriptChooser.setFileFilter(new FileFilter() {

      public boolean accept(File f) {
        return f.isDirectory() || f.getName().endsWith(".js");
      }

      public String getDescription() {
        return "*.js (Javascript files)";
      }
    });

    if (JFileChooser.APPROVE_OPTION == pluginScriptChooser.showDialog(getRootComponent(), "Select Script")) {
      PluginScript pluginScript = new PluginScript(true, pluginScriptChooser.getSelectedFile().getAbsolutePath(), PluginScript.RUN_MODE.PROJECT_OPEN);
      pluginScriptsTableModel.addPluginScript(pluginScript);
    }
  }

  private void removePluginScript() {
    if (table.getSelectedRow() > -1 && (JOptionPane.showConfirmDialog(this.getRootComponent(), "Are you sure you want to remove the selected plugin script?", "Remove plugin script?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)) {
      pluginScriptsTableModel.removePluginScript(table.getSelectedRow());
    }
  }

  public JTextField getTxtHomeFolder() {
    return txtHomeFolder;
  }
}
