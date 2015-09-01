package com.boxysystems.scriptmonkey.intellij.action;

import com.boxysystems.scriptmonkey.intellij.Constants;
import com.boxysystems.scriptmonkey.intellij.ui.ScriptShellPanelAction;
import com.boxysystems.scriptmonkey.intellij.icons.Icons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

/**
 * Created by IntelliJ IDEA.
 * User: shameed
 * Date: Oct 6, 2008
 * Time: 3:56:07 PM
 */
public class OpenHelpAction extends ScriptShellPanelAction {

    public OpenHelpAction() {
        super("Help",
                "Open Help Page",
                Icons.HELP_ICON);
        setEnabled(true);
    }


    public void actionPerformed(AnActionEvent anActionEvent) {
        try {
            com.intellij.ide.BrowserUtil.launchBrowser(Constants.PLUGIN_HOME_PAGE);
        } catch (Exception e) {
            Messages.showErrorDialog("Error launching web browser from IntelliJ! " + Constants.NEW_LINE + "Please visit the webpage '" + Constants.PLUGIN_HOME_PAGE + "' using your browser. " + Constants.NEW_LINE + "Exception: " + e.getMessage(), "Error");
        }
    }
}
