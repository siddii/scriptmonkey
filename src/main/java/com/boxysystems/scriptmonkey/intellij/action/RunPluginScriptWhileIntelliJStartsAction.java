package com.boxysystems.scriptmonkey.intellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: siddique
 * Date: Oct 19, 2008
 * Time: 2:37:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class RunPluginScriptWhileIntelliJStartsAction extends ToggleAction {

    private boolean selected = true;

    public boolean isSelected(AnActionEvent event) {
        selected = !selected;
        return selected;
    }

    public void setSelected(AnActionEvent event, boolean b) {
    }
}
