package com.boxysystems.scriptmonkey.intellij.action;

import com.boxysystems.scriptmonkey.intellij.ScriptMonkeyPlugin;
import com.boxysystems.scriptmonkey.intellij.icons.Icons;
import com.boxysystems.scriptmonkey.intellij.ui.ScriptMonkeyToolWindow;
import com.boxysystems.scriptmonkey.intellij.ui.ScriptShellPanelAction;
import com.boxysystems.scriptmonkey.intellij.util.ProjectUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;

/**
 * Created by IntelliJ IDEA.
 * User: shameed
 * Date: Oct 6, 2008
 * Time: 3:56:07 PM
 */
public class CloseScriptConsoleAction extends ScriptShellPanelAction {
    private String contentName;

    public CloseScriptConsoleAction(String contentName) {
        super("Close",
                "Close the console",
                Icons.CLOSE_ICON);
        this.contentName = contentName;
    }


    public void actionPerformed(AnActionEvent anActionEvent) {
        Project project = ProjectUtil.getProject(anActionEvent);
        ScriptMonkeyPlugin plugin = ScriptMonkeyPlugin.getInstance(project);
        ScriptMonkeyToolWindow toolWindow = plugin.getToolWindow();
        ContentManager contentManager = toolWindow.getContentManager();
        Content content = contentManager.findContent(contentName);
        contentManager.removeContent(content, true);
    }
}