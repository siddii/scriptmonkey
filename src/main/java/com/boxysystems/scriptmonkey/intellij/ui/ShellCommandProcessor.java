package com.boxysystems.scriptmonkey.intellij.ui;

/**
 * Created by IntelliJ IDEA.
 * User: siddique
 * Date: Oct 9, 2008
 * Time: 7:25:42 PM
 * To change this template use File | Settings | File Templates.
 */
interface ShellCommandProcessor {
    public String executeCommand(String cmd);
    public String getPrompt();
    public boolean isCommandShell(); 
}
