package com.boxysystems.scriptmonkey.intellij.action;

import com.boxysystems.scriptmonkey.intellij.icons.Icons;
import com.boxysystems.scriptmonkey.intellij.ui.ScriptCommandProcessor;
import com.boxysystems.scriptmonkey.intellij.ui.ScriptShellPanelAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: shameed
 * Date: Oct 6, 2008
 * Time: 3:56:07 PM
 */
public class StopScriptAction extends ScriptShellPanelAction {
    private ScriptCommandProcessor.ScriptRunningTask task;

    public StopScriptAction() {
        super("Stop",
                "Stop the running script",
                Icons.SUSPEND_ICON);
        setEnabled(true);
    }

    public void actionPerformed(AnActionEvent anActionEvent) {
        if (task != null && task.isRunning()) {
            task.cancel();
        }
    }

    public void setTask(ScriptCommandProcessor.ScriptRunningTask task) {
        this.task = task;
    }
}