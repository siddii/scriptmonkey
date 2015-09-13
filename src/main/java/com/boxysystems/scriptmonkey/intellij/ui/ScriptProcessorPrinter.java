package com.boxysystems.scriptmonkey.intellij.ui;

public interface ScriptProcessorPrinter {
    void println(String msg);
    void progressln(String msg);
    boolean hadOutput();
    void startProgress();
    void endProgress();
}
