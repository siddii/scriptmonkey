package com.boxysystems.scriptmonkey.intellij.action;

import com.boxysystems.scriptmonkey.intellij.ScriptMonkeyPlugin;
import com.boxysystems.scriptmonkey.intellij.util.ProjectUtil;
import com.boxysystems.scriptmonkey.intellij.ui.ScriptMonkeyToolWindow;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

/**
 * Created by IntelliJ IDEA.
 * User: shameed
 * Date: Oct 6, 2008
 * Time: 3:47:22 PM
 */
public class ShowCommandShellAction extends AnAction {

    public void actionPerformed(AnActionEvent anActionEvent) {

        Project project = ProjectUtil.getProject(anActionEvent);
        ScriptMonkeyPlugin scriptMonkeyPlugin = ScriptMonkeyPlugin.getInstance(project);
        if (scriptMonkeyPlugin != null) {
            ScriptMonkeyToolWindow toolWindow = scriptMonkeyPlugin.getToolWindow();
            toolWindow.activate();
        }
    }
}
