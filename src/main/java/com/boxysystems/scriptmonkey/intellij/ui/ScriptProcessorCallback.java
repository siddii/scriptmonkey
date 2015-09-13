package com.boxysystems.scriptmonkey.intellij.ui;

/**
 * Created by IntelliJ IDEA.
 * User: shameed
 * Date: Oct 20, 2008
 * Time: 4:30:42 PM
 */
public interface ScriptProcessorCallback extends ScriptProcessorPrinter {
    void success();
    void failure(Throwable throwable);
    void done();
}
