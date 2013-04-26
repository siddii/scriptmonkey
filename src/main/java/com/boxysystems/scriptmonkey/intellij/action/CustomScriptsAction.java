package com.boxysystems.scriptmonkey.intellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: shameed
 * Date: Oct 6, 2008
 * Time: 3:51:13 PM
 */
public class CustomScriptsAction extends AnAction {
  public void actionPerformed(AnActionEvent anActionEvent) {
    System.out.println("Calling "+this.getClass());
  }
}
