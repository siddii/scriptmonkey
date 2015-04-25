package com.boxysystems.scriptmonkey.intellij.ui;

import com.boxysystems.scriptmonkey.intellij.*;
import com.boxysystems.scriptmonkey.intellij.action.JSFileFilter;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
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
        try {
            System.out.println("new InvokeScriptMethod().sayHello() = " + new InvokeScriptMethod().sayHello());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    initScriptingEngineAndRunGlobalScripts();
                    if (scriptFile != null) {
                        logger.info("Evaluating script file '" + scriptFile + "' ...");
                        engine.eval(new FileReader(scriptFile));
                    }
                    callback.success();
                } catch (Throwable e) {
                    callback.failure(e);
                }

            }
        });
    }

    public ScriptRunningTask processScript(final String scriptContent, final ScriptProcessorCallback callback) {
        if (project != null) {
            ScriptRunningTask task = new ScriptRunningTask("Running script...", scriptContent, callback);
            task.queue();
            return task;
        }
        return null;
    }

    private void initScriptingEngineAndRunGlobalScripts() {
        initScriptEngine();
        runGlobalScripts();
    }

    public void processCommandLine() {
        new Thread(new Runnable() {
            public void run() {
                initScriptingEngineAndRunGlobalScripts();
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
        String res;
        try {
            engineReady.await();
            Object tmp = engine.eval(cmd);
            res = (tmp == null) ? null : tmp.toString();
        } catch (InterruptedException ie) {
            res = ie.getMessage();
        } catch (ScriptException se) {
            res = se.getMessage();
        }
        return res;
    }

    private void createScriptEngine(ScriptMonkeyPlugin scriptMonkeyPlugin) {
        if (scriptMonkeyPlugin != null && pluginClassLoader != null) {
            ScriptMonkeyPluginClassLoader augmentedClassLoader = pluginClassLoader.getAugmentedClassLoader();
            if (augmentedClassLoader != null) {
                Thread.currentThread().setContextClassLoader(augmentedClassLoader);
            }
        }
        engine = getScriptingEngine();
        String extension = engine.getFactory().getExtensions().get(0);
        prompt = extension + ">";
        engine.setBindings(createGlobalBindings(), ScriptContext.ENGINE_SCOPE);
    }

    private ScriptEngine getScriptingEngine() {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        String engines[] = {"JavaScript", "nashorn"};
        for (String engine1 : engines) {
            try {
                System.out.println("##### Looking for " + engine1);
                ScriptEngine scriptEngine = engineManager.getEngineByName(engine1);
                if (scriptEngine != null) {
                    return scriptEngine;
                }
                System.out.println("##### scriptEngine = " + scriptEngine);
            } catch (Exception e) {
                logger.warn("Couldn't load engine - " + engine1);
            }
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
        Map<String, Object> map =
                Collections.synchronizedMap(new HashMap<String, Object>());
        return new SimpleBindings(map);
    }

    private void initScriptEngine() {
        addGlobalVariable("engine", engine);
        addGlobalVariable("application", application);
        addGlobalVariable("project", project);
        addGlobalVariable("plugin", plugin);
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

        public ScriptRunningTask(@NotNull String title, String scriptContent, ScriptProcessorCallback callback) {
            super(project, title, false);
            this.scriptContent = scriptContent;
            this.callback = callback;
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
                            initScriptingEngineAndRunGlobalScripts();
                            try {

                                if (scriptContent != null) {
                                    logger.info("Evaluating script ...");
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
