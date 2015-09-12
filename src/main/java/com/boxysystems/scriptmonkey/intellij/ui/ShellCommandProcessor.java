package com.boxysystems.scriptmonkey.intellij.ui;

import com.intellij.openapi.project.Project;

import java.util.concurrent.Future;

/**
 * Created by IntelliJ IDEA.
 * User: siddique
 * Date: Oct 9, 2008
 * Time: 7:25:42 PM
 * To change this template use File | Settings | File Templates.
 */
interface ShellCommandProcessor {
    Object executeCommand(String cmd, int lineOffset, int firstLineColumnOffset, ScriptTaskInterrupter taskStopSetter, ScriptProcessorPrinter printer);
    String getPrompt();
    boolean isCommandShell();
    Project getProject();
}
