package com.boxysystems.scriptmonkey.intellij.action;

import com.boxysystems.scriptmonkey.intellij.icons.Icons;
import com.boxysystems.scriptmonkey.intellij.ui.ScriptShellPanelAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: shameed
 * Date: Oct 6, 2008
 * Time: 3:56:07 PM
 */
public class RerunScriptAction extends ScriptShellPanelAction {

  private RunScriptAction runScriptAction = new RunScriptAction();

  public RerunScriptAction() {
    super("Rerun",
      "Re-run the script",
      Icons.RERUN_ICON);
  }


  public void actionPerformed(AnActionEvent anActionEvent) {
    runScriptAction.actionPerformed(anActionEvent);
  }
}