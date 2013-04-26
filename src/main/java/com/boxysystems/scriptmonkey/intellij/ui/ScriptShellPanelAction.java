package com.boxysystems.scriptmonkey.intellij.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: siddique
 * Date: Dec 9, 2008
 * Time: 7:58:35 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ScriptShellPanelAction extends AnAction {
    protected boolean enabled = false;

    protected ScriptShellPanelAction() {
    }

    protected ScriptShellPanelAction(String text, String description, Icon icon) {
        super(text, description, icon);
    }

    public void update(AnActionEvent actionEvent) {
        super.update(actionEvent);
        final Presentation presentation = actionEvent.getPresentation();
        presentation.setEnabled(enabled);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
