package com.boxysystems.scriptmonkey.intellij;

/**
 * Created by siddique on 11/6/14.
 */
import javax.script.*;

public class InvokeScriptMethod {
    public static void main(String[] args) throws Exception {
        String value = new InvokeScriptMethod().sayHello();
        System.out.println("##### Value = " + value);
    }

    public String sayHello() throws ScriptException, NoSuchMethodException {
        ScriptEngineManager manager = new ScriptEngineManager();
        System.out.println("manager.getEngineFactories() = " + manager.getEngineFactories().get(0));
        ScriptEngine engine = manager.getEngineByName("nashorn");

        // evaluate JavaScript code that defines an object with one method
        engine.eval("var obj = new Object()");
        engine.eval("obj.hello = function(name) {return 'Hello, ' + name;}");

        // expose object defined in the script to the Java application
        Object obj = engine.get("obj");

        // create an Invocable object by casting the script engine object
        Invocable inv = (Invocable) engine;

        // invoke the method named "hello" on the object defined in the script
        // with "Script Method!" as the argument
        return (String) inv.invokeMethod(obj, "hello", "Script Method!");
    }
}
