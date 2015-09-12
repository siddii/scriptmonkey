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
import com.intellij.util.lang.UrlClassLoader;
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
                engineReady.countDown();
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

    public String executeCommand(final String cmd, final int lineOffset, final int firstLineColumnOffset, ScriptTaskInterrupter taskInterrupter, ScriptProcessorPrinter printer) {
        final String[] result = new String[1];
        try {
            engineReady.await();

            // vsch: set the script file name for exceptions and __FILE__ setting
            engine.getContext().setAttribute(ScriptEngine.FILENAME, "<Script Monkey JS Shell>", ScriptContext.ENGINE_SCOPE);

            InterruptibleScriptTaskImpl scriptSafetyNet = new InterruptibleScriptTaskImpl(printer, new Thread(new Runnable() {
                @Override
                public void run() {
                    Object evalResult = null;
                    try {
                        evalResult = engine.eval(cmd);
                        result[0] = (evalResult == null) ? null : evalResult.toString();
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

        return result[0];
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
        protected long interrupWaitMillis;

        InterruptibleScriptTaskImpl(ScriptProcessorPrinter printer, Thread scriptSafetyNet) {
            this.scriptSafetyNet = scriptSafetyNet;
            this.printer = printer;
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
            // vsch: give it 2 second to terminate gracefully then kill it
            // signal interruption to give it a chance to gracefully terminate which the JS script can
            // test for the interrupt using: if (java.lang.Thread.interrupted())
            if (scriptSafetyNet == null) return;

            if (!scriptSafetyNet.isInterrupted()) {
                if (printer != null) printer.println("Attempting to interrupt script thread.");
                scriptSafetyNet.interrupt();

                // let it try and catch this one
                Thread.yield();
            }

            Executors.newCachedThreadPool().submit(new Runnable() {
                @Override
                public void run() {
                    boolean firstWait = true;
                    for (long i = 0; i < interrupWaitMillis; i += 100) {
                        if (!scriptSafetyNet.isAlive()) break;
                        if (firstWait && printer != null) {
                            printer.println("Waiting for thread to respond to interrupt...");
                            firstWait = false;
                        }

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // we'll finish soon enough
                        }
                    }

                    if (scriptSafetyNet.isAlive()) {
                        // vsch: we have to take more extreme measures it is stuck in a tight loop and not responding
                        // javadocs say don't use Thread.stop blah, blah, blah, but it is the only thing that works
                        // which does not require the cooperation of the running thread. Programming is not always a
                        // gentleman's tea party and you need the ability to boot misbehaving code.
                        if (printer != null) printer.println("Script thread is not responding. Stopping thread...");

                        scriptSafetyNet.stop(); // that'll learn ya
                        Thread.yield();
                        if (scriptSafetyNet.isAlive()) scriptSafetyNet.stop(); // if not then maybe, this'll learn ya

                        // vsch: just one stop is not enough. If javascript catches() the exception (with a twist) then it also
                        // catches java.lang.ThreadDeath and continues running here is the JavaScript that manages to survive
                        // a single thread death:
                        // while(true) { try { java.lang.Thread.sleep(100); } catch (e) {  } }
                        // this one is unstoppable. If there is no processing in the catch() block then it will manage
                        // to handle multiple consecutive stops and keep going.
                        // however, the second one catches it in its exception handler (I'm guessing) and causes the thread
                        // to terminate. Talk about 9 lives.
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
