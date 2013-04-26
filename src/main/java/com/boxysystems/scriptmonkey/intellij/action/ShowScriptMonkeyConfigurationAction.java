package com.boxysystems.scriptmonkey.intellij.action;

import com.boxysystems.scriptmonkey.intellij.icons.Icons;
import com.boxysystems.scriptmonkey.intellij.ScriptMonkeyApplicationComponent;
import com.boxysystems.scriptmonkey.intellij.util.ProjectUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;

/**
 * Created by IntelliJ IDEA.
 * User: siddique
 * Date: Oct 19, 2008
 * Time: 2:50:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class ShowScriptMonkeyConfigurationAction extends AnAction {

    public ShowScriptMonkeyConfigurationAction() {
        super("Show Settings",
                "Configure settings",
                Icons.CONFIGURE_ICON);
    }

    public void actionPerformed(AnActionEvent event) {
        Project project = ProjectUtil.getProject(event);
        ShowSettingsUtil.getInstance().editConfigurable(project, ScriptMonkeyApplicationComponent.getInstance());
    }
}
