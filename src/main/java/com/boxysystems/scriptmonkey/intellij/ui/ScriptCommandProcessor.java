package com.boxysystems.scriptmonkey.intellij.ui;

import com.boxysystems.scriptmonkey.intellij.Constants;
import com.boxysystems.scriptmonkey.intellij.ScriptMonkeyApplicationComponent;
import com.boxysystems.scriptmonkey.intellij.ScriptMonkeyPlugin;
import com.boxysystems.scriptmonkey.intellij.ScriptMonkeyPluginClassLoader;
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
    private ScriptMonkeyPluginClassLoader pluginClassLoader;

    public ScriptCommandProcessor(Application application) {
        this.application = application;
        createScriptEngine(null);
    }

    public ScriptCommandProcessor(Application application, Project project, ScriptMonkeyPlugin scriptMonkeyPlugin) {
        this.application = application;
        this.project = project;
        this.plugin = scriptMonkeyPlugin;
        this.pluginClassLoader = new ScriptMonkeyPluginClassLoader(plugin);
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

    public ScriptRunningTask processScript(final String scriptContent, final String scriptFileName, final ScriptProcessorCallback callback) {
        if (project != null) {
            ScriptRunningTask task = new ScriptRunningTask("Running script...", scriptContent, scriptFileName, callback);
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

    public String executeCommand(String cmd) {
        return executeCommand(cmd, 0, 0);
    }

    public String executeCommand(String cmd, int lineOffset) {
        return executeCommand(cmd, lineOffset, 0);
    }

    public String executeCommand(String cmd, int lineOffset, int firstLineColumnOffset) {
        String res;
        try {
            engineReady.await();
            // vsch: set the script file name for exceptions and __FILE__ setting
            engine.getContext().setAttribute(ScriptEngine.FILENAME, "<Script Monkey JS Shell>", ScriptContext.ENGINE_SCOPE);
            Object tmp = engine.eval(cmd);
            res = (tmp == null) ? null : tmp.toString();
        } catch (InterruptedException ie) {
            res = ie.getMessage();
        } catch (ScriptException se) {
            // adjust the position of the error to correspond to actual source
            res = se.getMessage();
            if (se.getFileName().equals("<Script Monkey JS Shell>")) {
                //if (se.getFileName().equals("<eval>")) {
                int lineNumber = se.getLineNumber() + lineOffset;
                int colNumber = se.getColumnNumber() + (se.getLineNumber() == 1 ? firstLineColumnOffset : 0);
                res = res.replace(se.getFileName() + ":" + se.getLineNumber() + ":" + se.getColumnNumber() + " ", ""); //se.getFileName() + ":" + lineNumber + ":" + colNumber);
                res = res.replace("at line number " + se.getLineNumber(), "at line " + lineNumber);
                res = res.replace(" at column number " + se.getColumnNumber(), ":" + colNumber);
            }
            //}
        } catch (Exception e) {
            res = e.toString();
        } catch (AssertionError e) {
            res = e.toString();
        }
        return res;
    }

    private void createScriptEngine(ScriptMonkeyPlugin scriptMonkeyPlugin) {
        if (scriptMonkeyPlugin != null && pluginClassLoader != null) {
            UrlClassLoader augmentedClassLoader = pluginClassLoader.getAugmentedClassLoader();
            if (augmentedClassLoader != null) {
                Thread.currentThread().setContextClassLoader(augmentedClassLoader);
            }
        }
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

    public class ScriptRunningTask extends Task.Backgroundable {
        private ScriptProcessorCallback callback;
        private String scriptContent;
        private ExecutorService executor;
        private String scriptFilename;

        public ScriptRunningTask(@NotNull String title, String scriptContent, String scriptFilename, ScriptProcessorCallback callback) {
            super(project, title, false);
            this.scriptContent = scriptContent;
            this.callback = callback;
            this.scriptFilename = scriptFilename == null || scriptFilename.length() == 0 ? "<eval>" : scriptFilename;
            this.setCancelText("Stop running scripts");
        }

        public void cancel() {
            executor.shutdownNow();
        }

        public boolean isRunning() {
            return !executor.isTerminated();
        }

        public void run(ProgressIndicator indicator) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    executor = Executors.newFixedThreadPool(1);
                    executor.execute(new Runnable() {
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
                    });
                }
            });
        }
    }
}
