package com.boxysystems.scriptmonkey.intellij.action;

import com.boxysystems.scriptmonkey.intellij.ui.ScriptShellPanel;
import com.boxysystems.scriptmonkey.intellij.icons.Icons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: shameed
 * Date: Oct 6, 2008
 * Time: 3:56:07 PM
 */
public class ClearEditorAction extends AnAction {
  private ScriptShellPanel scriptShellPanel;

  public ClearEditorAction() {
    super("Clear",
      "Clear command shell",
      Icons.CLEAR_ICON);
  }

  public void setScriptShellPanel(ScriptShellPanel scriptShellPanel) {
    this.scriptShellPanel = scriptShellPanel;
  }

  public void actionPerformed(AnActionEvent anActionEvent) {
    scriptShellPanel.clear();
  }
}