package com.boxysystems.scriptmonkey.intellij.ui;

import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: siddique
 * Date: Oct 20, 2008
 * Time: 6:39:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScriptShellTabContent extends JPanel {
    private ScriptShellPanel scriptShellPanel;

    public ScriptShellTabContent(ScriptShellPanel scriptShellPanel) {
        this.scriptShellPanel = scriptShellPanel;
        this.setLayout(new BorderLayout());
        this.setBackground(UIUtil.getTreeTextBackground());
        this.add(scriptShellPanel, BorderLayout.CENTER);
    }

    public ScriptShellPanel getScriptShellPanel() {
        return scriptShellPanel;
    }
}
