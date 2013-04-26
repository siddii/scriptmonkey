package com.boxysystems.scriptmonkey.intellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.*;
import javax.script.ScriptEngine;
import javax.script.Invocable;
import javax.script.ScriptException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by IntelliJ IDEA.
 * User: siddique
 * Date: Jun 23, 2009
 * Time: 7:39:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScriptMonkeyIntelliJPluginAction extends AnAction {
    private ScriptEngine engine;
    private Object callableObject;

    public ScriptMonkeyIntelliJPluginAction() {
        super();
    }

    public void setScriptingEngine(ScriptEngine engine) {
        this.engine = engine;
    }

    public void setCallableObject(Object callableObject) {
        this.callableObject = callableObject;
    }


    public void actionPerformed(AnActionEvent anActionEvent) {
        Invocable invocableEngine = (Invocable) engine;
        try {
            invocableEngine.invokeMethod(callableObject, "actionPerformed", anActionEvent);
        } catch (Throwable e) {
            //don't care if the method doesn't exist
        }
    }

    @Override
    public void update(AnActionEvent anActionEvent) {
        super.update(anActionEvent);
        Invocable invocableEngine = (Invocable) engine;
        try {
            invocableEngine.invokeMethod(callableObject, "update", anActionEvent);
        } catch (Throwable e) {
            //don't care if the method doesn't exist
        }
    }
}
