package com.boxysystems.scriptmonkey.intellij.ui;

import com.boxysystems.scriptmonkey.intellij.util.ProjectUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.OpenSourceUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: shameed
 * Date: Nov 3, 2008
 * Time: 12:21:56 PM
 */
public class PluginScriptsTablePopupMenu extends JPopupMenu {

  public PluginScriptsTablePopupMenu(PluginScriptsTable table, PluginScript script) {
    EnableOrDisableAction action = new EnableOrDisableAction(table, script);
    JMenuItem menuItem = new JMenuItem(action);
    add(menuItem);
    OpenScriptAction openScriptAction = new OpenScriptAction(table);
    add(new JMenuItem(openScriptAction));
    if (!new File(script.getFilePath()).exists()) {
      openScriptAction.setEnabled(false);
    }
  }


  private class EnableOrDisableAction extends AbstractAction {
    private PluginScriptsTable table;
    private PluginScript script;

    private EnableOrDisableAction(PluginScriptsTable table, PluginScript script) {
      super(script.isEnabled() ? "Disable" : "Enable");
      this.table = table;
      this.script = script;
    }

    public void actionPerformed(ActionEvent e) {
      script.setEnabled(!script.isEnabled());
      table.getPluginScriptsTableModel().updatePluginScript(script);
    }
  }


  private class OpenScriptAction extends AbstractAction {
    private PluginScriptsTable table;

    private OpenScriptAction(PluginScriptsTable table) {
      super("Open source in editor");
      this.table = table;
    }

    public void actionPerformed(ActionEvent e) {
      PluginScript pluginScript = table.getPluginScriptsTableModel().getPluginScripts().get(table.getSelectedRow());
      Project project = ProjectUtil.getProject();
      if (project != null && project.isOpen()) {
        File file = new File(pluginScript.getFilePath());
        try {
          VirtualFile virtualFile = VfsUtil.findFileByURL(file.toURI().toURL());
          PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
          PsiFile psiFiles[] = {psiFile};
          OpenSourceUtil.navigate(psiFiles);
        } catch (Exception e1) {
          e1.printStackTrace();
        }
      }
    }
  }
}
