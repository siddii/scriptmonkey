package com.boxysystems.scriptmonkey.intellij.ui;

import com.boxysystems.scriptmonkey.intellij.Constants;
import com.boxysystems.scriptmonkey.intellij.ScriptMonkeyApplicationComponent;
import com.boxysystems.scriptmonkey.intellij.ScriptMonkeyPlugin;
import com.boxysystems.scriptmonkey.intellij.action.JSFileFilter;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.script.*;
import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: shameed
 * Date: Sep 30, 2008
 * Time: 5:10:21 PM
 */
public class ScriptCommandProcessor implements ShellCommandProcessor {
    private static final Logger logger = Logger.getLogger(ScriptCommandProcessor.class);

    private volatile ScriptEngine engine;

    private CountDownLatch engineReady = new CountDownLatch(1);

    private volatile String prompt;

    private boolean commandShell = true;
    private Application application;

    @Override
    public Project getProject() {
        return project;
    }

    private Project project;
    private ScriptMonkeyPlugin plugin;

    public ScriptCommandProcessor(Application application) {
        this.application = application;
        createScriptEngine(null);
    }

    public ScriptCommandProcessor(Application application, Project project, ScriptMonkeyPlugin scriptMonkeyPlugin) {
        this.application = application;
        this.project = project;
        this.plugin = scriptMonkeyPlugin;
        createScriptEngine(scriptMonkeyPlugin);
    }

    public ExecutorService processScriptFile(final File scriptFile, final ScriptProcessorCallback callback) {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(new Runnable() {
            public void run() {
                evaluateScriptFile(scriptFile, callback);
            }
        });
        executor.shutdown();
        return executor;
    }

    public void processScriptFileSynchronously(final File scriptFile, final ScriptProcessorCallback callback) {
        ExecutorService executor = processScriptFile(scriptFile, callback);
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.error("Error while processing script file synchronously", e);
        }
    }

    private void evaluateScriptFile(final File scriptFile, final ScriptProcessorCallback callback) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    runGlobalScripts();
                    if (scriptFile != null) {
                        logger.info("Evaluating script file '" + scriptFile + "' ...");
                        // vsch: set the script file name for exceptions and __FILE__ setting
                        engine.getContext().setAttribute(ScriptEngine.FILENAME, scriptFile.getAbsolutePath(), ScriptContext.ENGINE_SCOPE);
                        engine.eval(new FileReader(scriptFile));
                    }
                    callback.success();
                } catch (ScriptException e) {
                    String msg = e.getMessage();
                    ScriptException se = e;
                    msg = msg.replace(e.getFileName() + ":" + e.getLineNumber() + ":" + e.getColumnNumber() + " ", "");
                    msg = msg.replaceAll("\\n\\n\\s*\\^?\\s*", ":");
                    msg = msg.replace("in " + e.getFileName() + " ", "");
                    msg = msg.replace(" at line number " + e.getLineNumber(), "");
                    msg = msg.replace(" at column number " + e.getColumnNumber(), "");
                    if (!msg.equals(e.getMessage())) {
                        se = new ScriptException(msg.trim(), scriptFile.getPath(), e.getLineNumber(), e.getColumnNumber());
                        se.setStackTrace(e.getStackTrace());
                    }
                    callback.failure(se);
                } catch (Throwable e) {
                    // adjust file name, it is off in the message
                    callback.failure(e);
                }
            }
        });
    }

    public ScriptRunningTask processScript(final String scriptContent, final String scriptFileName, final ScriptProcessorCallback callback, final ScriptTaskInterrupter taskInterrupter) {
        if (project != null) {
            ScriptRunningTask task = new ScriptRunningTask("Running script...", scriptContent, scriptFileName, callback);
            taskInterrupter.setTask(task);
            task.queue();
            return task;
        }
        return null;
    }

    public void processCommandLine() {
        new Thread(new Runnable() {
            public void run() {
                runGlobalScripts();
            }
        }).start();
    }

    public String getPrompt() {
        return prompt;
    }

    public boolean isCommandShell() {
        return commandShell;
    }

    public void setCommandShell(boolean commandShell) {
        this.commandShell = commandShell;
    }

    protected boolean allWhiteSpace(String text) {
        int iMax = text.length();
        for (int i = 0; i < iMax; i++) {
            if (text.charAt(i) != ' ' && text.charAt(i) != '\n') return false;
        }
        return true;
    }

    protected String formatStringResult(String text) {
        int iMax = text.length();
        int eolCount = 0;
        for (int i = 0; i < iMax; i++) {
            if (text.charAt(i) == '\n' && ++eolCount > 1) break;
        }
        if (eolCount > 1) {
            text = StringEscapeUtils.escapeJava(text).replace("\\n", "\\n\" + \n\"");
            return text;
        }
        return StringEscapeUtils.escapeJava(text);
    }

    public Object executeCommand(final String cmd, final int lineOffset, final int firstLineColumnOffset, ScriptTaskInterrupter taskInterrupter, final ScriptProcessorPrinter printer) {
        final Object[] evalResult = {null};
        final String[] result = new String[1];
        try {
            engineReady.await();

            // vsch: set the script file name for exceptions and __FILE__ setting
            engine.getContext().setAttribute(ScriptEngine.FILENAME, "<Script Monkey JS Shell>", ScriptContext.ENGINE_SCOPE);

            InterruptibleScriptTaskImpl scriptSafetyNet = new InterruptibleScriptTaskImpl(printer, new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        evalResult[0] = engine.eval(cmd);
                        if (evalResult[0] == null) {
                            result[0] = (printer == null || printer.hadOutput() || allWhiteSpace(cmd)) ? null : "null";
                        }
                        else if (evalResult[0] instanceof String) {
                            result[0] = "\"" + formatStringResult((String) evalResult[0]) + "\"";
                        }
                        else {
                            result[0] = evalResult[0].toString();
                        }
                    } catch (ScriptException se) {
                        // adjust the position of the error to correspond to actual source
                        result[0] = se.getMessage();
                        if (se.getFileName().equals("<Script Monkey JS Shell>")) {
                            //if (se.getFileName().equals("<eval>")) {
                            int lineNumber = se.getLineNumber() + lineOffset;
                            int colNumber = se.getColumnNumber() + (se.getLineNumber() == 1 ? firstLineColumnOffset :
                                    0);
                            result[0] = result[0].replace(se.getFileName() + ":" + se.getLineNumber() + ":" + se.getColumnNumber() + " ", ""); //se.getFileName() + ":" + lineNumber + ":" + colNumber);
                            result[0] = result[0].replace("at line number " + se.getLineNumber(), "at line " + lineNumber);
                            result[0] = result[0].replace(" at column number " + se.getColumnNumber(), ":" + colNumber);
                        }
                    } catch (Throwable se) {
                        if (se instanceof ThreadDeath) {
                            result[0] = "java.lang.ThreadDeath";
                        }
                        else {
                            result[0] = se.getMessage() != null ? se.getMessage() : se.getClass().toString();
                        }
                    }
                }
            }), 1000); // only wait a second for interrupt, this is hand rolled code so don't expect long processing that will respond to interrupts

            if (taskInterrupter != null) {
                taskInterrupter.setTask(scriptSafetyNet);
            }

            scriptSafetyNet.run();
        } catch (InterruptedException e) {
            result[0] = e.getMessage();
        } catch (Exception e) {
            result[0] = e.toString();
        } catch (AssertionError e) {
            result[0] = e.toString();
        }

        if (printer != null && result[0] != null && result[0].length() > 0) {
            printer.println(result[0]);
        }

        return evalResult[0];
    }

    private void createScriptEngine(ScriptMonkeyPlugin scriptMonkeyPlugin) {
        //if (scriptMonkeyPlugin != null && pluginClassLoader != null) {
        //    UrlClassLoader augmentedClassLoader = pluginClassLoader.getAugmentedClassLoader();
        //    if (augmentedClassLoader != null) {
        //        Thread.currentThread().setContextClassLoader(augmentedClassLoader);
        //    }
        //}
        engine = getScriptingEngine();
        String extension = engine.getFactory().getExtensions().get(0);
        prompt = extension + ">";

        /*
         vsch: since we get a new instance of ScriptEngineManager every time our Global Scope becomes per engine scope
               to have shared global scope between engines you need to create them from the same ScriptEngineFactory instance
               which would mean re-using the ScriptEngine Manager. So here we change to global scope so that our bindings
               will work inside with() {} scope, which they don't for ENGINE_SCOPE
        */
        engine.setBindings(createGlobalBindings(), ScriptContext.GLOBAL_SCOPE);

        // vsch: moved initialization of globals here so it is the same as window, which is the only one that works in issue: #6
        initScriptEngine();
    }

    private ScriptEngine getScriptingEngine() {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        try {
            ScriptEngine scriptEngine = engineManager.getEngineByName("nashorn");
            if (scriptEngine != null) {
                scriptEngine.eval("load(\"nashorn:parser.js\");");
                //scriptEngine.eval("load(\"nashorn:mozilla_compat.js\");");
                return scriptEngine;
            }
        } catch (Exception e) {
            logger.error("Couldn't load nashorn scripting engine!", e);
        }
        throw new RuntimeException("Cannot load scripting engine!");
    }

    private void runGlobalScripts() {
        if (engineReady.getCount() == 0) return;

        try {
            ScriptMonkeyApplicationComponent applicationComponent = ApplicationManager.getApplication().getComponent(ScriptMonkeyApplicationComponent.class);
            File jsFolder = new File(applicationComponent.getSettings().getHomeFolder(), Constants.JS_FOLDER_NAME);
            final File globalScripts = new File(jsFolder, "global");
            if (globalScripts.exists()) {
                File[] jsFiles = globalScripts.listFiles(new JSFileFilter());
                for (File jsFile : jsFiles) {
                    try {
                        logger.info("Evaluating script '" + jsFile + "' ...");
                        // vsch: set the script file name for exceptions and __FILE__ setting
                        engine.getContext().setAttribute(ScriptEngine.FILENAME, jsFile.getAbsolutePath(), ScriptContext.ENGINE_SCOPE);
                        engine.eval(new FileReader(jsFile));
                        logger.info("Script successfully processed !");
                    } catch (ScriptException e) {
                        logger.error("Error executing script '" + jsFile + "'", e);
                    } catch (FileNotFoundException e) {
                        logger.error("Unable to find script file '" + jsFile + "' !");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        engineReady.countDown();
    }

    private void executeScripts(File globalScripts) {
        File[] jsFiles = globalScripts.listFiles(new JSFileFilter());
        for (File jsFile : jsFiles) {
            try {
                logger.info("Evaluating script '" + jsFile + "' ...");
                // vsch: set the script file name for exceptions and __FILE__ setting
                engine.getContext().setAttribute(ScriptEngine.FILENAME, jsFile.getAbsolutePath(), ScriptContext.ENGINE_SCOPE);
                engine.eval(new FileReader(jsFile));
                logger.info("Script successfully processed !");
            } catch (ScriptException e) {
                logger.error("Error executing script '" + jsFile + "'", e);
            } catch (FileNotFoundException e) {
                logger.error("Unable to find script file '" + jsFile + "' !");
            }
        }
    }

    private Bindings createGlobalBindings() {
        Map<String, Object> map = Collections.synchronizedMap(new HashMap<String, Object>());
        return new SimpleBindings(map);
    }

    private void initScriptEngine() {
        addGlobalVariable("engine", engine);
        addGlobalVariable("application", application);
        addGlobalVariable("project", project);
        addGlobalVariable("plugin", plugin);
        addGlobalVariable("windowManager", WindowManager.getInstance());
    }

    public void addGlobalVariable(String name, Object globalObject) {
        if (name != null && globalObject != null) {
            engine.put(name, globalObject);
        }
    }

    public interface InterruptibleScriptTask {
        void cancel();
        boolean isRunning();
    }

    protected class InterruptibleScriptTaskImpl implements InterruptibleScriptTask {
        protected Thread scriptSafetyNet;
        protected ScriptProcessorPrinter printer;
        protected ScriptProcessorCallback callback = null;
        protected long interrupWaitMillis;
        protected boolean inCancel = false;
        protected boolean cancelFailed = false;

        InterruptibleScriptTaskImpl(ScriptProcessorCallback callback, Thread scriptSafetyNet) {
            this.scriptSafetyNet = scriptSafetyNet;
            this.printer = callback;
            this.callback = callback;
            this.interrupWaitMillis = 2000;
        }

        InterruptibleScriptTaskImpl(ScriptProcessorPrinter printer, Thread scriptSafetyNet, int interruptWaitMillis) {
            this.scriptSafetyNet = scriptSafetyNet;
            this.printer = printer;
            this.interrupWaitMillis = interruptWaitMillis;
        }

        public void start() {
            scriptSafetyNet.start();
        }

        public void run() throws InterruptedException {
            scriptSafetyNet.start();
            scriptSafetyNet.join(0);
        }

        public void join(int i) throws InterruptedException {
            scriptSafetyNet.join(i);
        }

        public void cancel() {
            // vsch: give it interruptWaitMillis to terminate gracefully then kill it
            // signal interruption to give it a chance to gracefully terminate which the JS script can
            // test for the interrupt using: if (java.lang.Thread.interrupted())
            if (scriptSafetyNet == null || inCancel) return;

            inCancel = true;

            if (!scriptSafetyNet.isInterrupted()) {
                if (printer != null) printer.progressln("Attempting to interrupt script thread.");
                scriptSafetyNet.interrupt();

                // let it try and catch this one
                Thread.yield();
            }

            Executors.newCachedThreadPool().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!cancelFailed) {
                            boolean firstWait = true;
                            for (long i = 0; i < interrupWaitMillis; i += 100) {
                                if (!scriptSafetyNet.isAlive()) break;
                                if (firstWait && printer != null) {
                                    printer.progressln("Waiting for thread to respond to interrupt...");
                                    firstWait = false;
                                }

                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    // we'll finish soon enough
                                }
                            }
                        }

                        if (scriptSafetyNet.isAlive()) {
                            // vsch: we have to take more extreme measures it is stuck in a tight loop and not responding
                            // javadocs say don't use Thread.stop blah, blah, blah, but it is the only thing that works
                            // which does not require the cooperation of the running thread. Programming is not always a
                            // gentleman's tea party and you need the ability to boot misbehaving code. Especially one that
                            // goes into an infinite loop
                            // vsch: just one stop is not enough. If javascript catches() the exception then it also
                            // catches java.lang.ThreadDeath and continues running here is the JavaScript that manages to survive
                            // while (true) { try { java.lang.Thread.sleep(n); } catch (e) { } } // where n=0,1
                            // BTW, this was the most optimal configuration found by trial and error that will terminate
                            // this kind of script 'most' of the time withing a few loops, but sometimes you need to press
                            // stop one more time.
                            if (printer != null)
                                printer.progressln("Script thread is not responding. Stopping thread...");

                            int i = 0;
                            long delay = 0;
                            long lastDelay = 0;
                            for (i = 0; i < 20 && scriptSafetyNet.isAlive(); i++) {
                                try {
                                    scriptSafetyNet.stop();
                                    Thread.yield();
                                    if (scriptSafetyNet.isAlive()) scriptSafetyNet.stop();
                                    Thread.sleep(0);
                                    if (scriptSafetyNet.isAlive()) scriptSafetyNet.stop();
                                    Thread.sleep(1);
                                    if (scriptSafetyNet.isAlive()) scriptSafetyNet.stop();
                                    Thread.sleep(2);
                                    if (scriptSafetyNet.isAlive()) scriptSafetyNet.stop();
                                    Thread.sleep(3);
                                    if (scriptSafetyNet.isAlive()) scriptSafetyNet.stop();
                                    Thread.sleep(4);
                                    if (scriptSafetyNet.isAlive()) scriptSafetyNet.stop();
                                    Thread.sleep(delay += lastDelay = (long) (5 + i + 5 * Math.random()));
                                } catch (Throwable e) {
                                    break;
                                }
                            }

                            logger.info("Stop loop executed " + String.valueOf(i) + " times, last delay " + String.valueOf(lastDelay) + " avg delay " + (i > 0 ? String.valueOf(delay / i) : ""));
                        }
                    } finally {
                        Thread.yield();

                        if (!scriptSafetyNet.isAlive()) {
                            logger.info("cancel calling done");
                            if (callback != null) callback.done();
                            // leave inCancel on so that done does not get called, just in case finally execution is pending
                        }
                        else {
                            inCancel = false;
                            cancelFailed = true;
                            if (printer != null)
                                printer.progressln("Script thread is still alive. Press stop to try again. Nothing lives forever.");
                        }
                    }
                }
            });
        }

        public boolean isRunning() {
            return scriptSafetyNet.isAlive();
        }
    }

    public class ScriptRunningTask extends Task.Backgroundable implements InterruptibleScriptTask {
        private ScriptProcessorCallback callback;
        private String scriptContent;
        private ExecutorService executor;
        private String scriptFilename;
        private InterruptibleScriptTaskImpl scriptSafetyNet;

        public ScriptRunningTask(@NotNull String title, String scriptContent, String scriptFilename, ScriptProcessorCallback callback) {
            super(project, title, false);
            this.scriptContent = scriptContent;
            this.callback = callback;
            this.scriptFilename = scriptFilename == null || scriptFilename.length() == 0 ? "<eval>" : scriptFilename;
            this.setCancelText("Stop running scripts");
        }

        public void run(ProgressIndicator indicator) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    scriptSafetyNet = new InterruptibleScriptTaskImpl(callback, new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runGlobalScripts();
                            try {
                                if (scriptContent != null) {
                                    logger.info("Evaluating script ...");
                                    // vsch: set the script file name for exceptions and __FILE__ setting
                                    engine.getContext().setAttribute(ScriptEngine.FILENAME, scriptFilename, ScriptContext.ENGINE_SCOPE);
                                    engine.eval(scriptContent);
                                }
                                callback.success();
                            } catch (Throwable e) {
                                callback.failure(e);
                            } finally {
                                if (!scriptSafetyNet.inCancel) {
                                    logger.info("finally calling done");
                                    callback.done();
                                }
                            }
                        }
                    }));

                    // we execute it withing another thread so that we can kill it if it refuses to properly handle
                    // an interrupt.
                    scriptSafetyNet.start();
                }
            });
        }

        @Override
        public void cancel() {
            scriptSafetyNet.cancel();
        }

        @Override
        public boolean isRunning() {
            return scriptSafetyNet.isRunning();
        }
    }
}
