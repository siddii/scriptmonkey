package com.boxysystems.scriptmonkey.intellij;

import com.boxysystems.scriptmonkey.intellij.ui.PluginScript;
import com.boxysystems.scriptmonkey.intellij.ui.ScriptCommandProcessor;
import com.boxysystems.scriptmonkey.intellij.ui.ScriptProcessorCallback;
import com.boxysystems.scriptmonkey.intellij.util.ScriptMonkeyFileUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;

public class PluginScriptRunner {

    private static final Logger logger = Logger.getLogger(PluginScriptRunner.class);
    private Project project;
    private ScriptMonkeyPlugin plugin;

    public PluginScriptRunner() {
    }

    public PluginScriptRunner(Project project, ScriptMonkeyPlugin plugin) {
        this.project = project;
        this.plugin = plugin;
    }

    public void runPluginScripts(PluginScript.RUN_MODE runMode) {
        ScriptMonkeyApplicationComponent applicationComponent = ScriptMonkeyApplicationComponent.getInstance();
        runPluginScripts(applicationComponent, runMode, false);
    }

    public void runPluginScriptsSynchronously(PluginScript.RUN_MODE runMode) {
        ScriptMonkeyApplicationComponent applicationComponent = ScriptMonkeyApplicationComponent.getInstance();
        runPluginScripts(applicationComponent, runMode, true);
    }

    public void disposeComponent() {
        project = null;
        plugin = null;
    }

    public void runPluginScripts(ScriptMonkeyApplicationComponent applicationComponent, PluginScript.RUN_MODE runMode, boolean synchronous) {
        ScriptMonkeySettings settings = applicationComponent.getSettings();
        if (settings != null) {
            List<PluginScript> pluginScripts = settings.getPluginScripts();
            for (final PluginScript pluginScript : pluginScripts) {
                if (pluginScript.isEnabled() && runMode.equals(pluginScript.getRunMode()) && ScriptMonkeyFileUtil.exists(pluginScript.getFilePath())) {
                    logger.info("Running plugin script '" + pluginScript.getFilePath() + "' on runMode = " + runMode);
                    ScriptCommandProcessor commandProcessor;
                    final File scriptFile = new File(pluginScript.getFilePath());
                    try {
                        if (project != null) {
                            commandProcessor = new ScriptCommandProcessor(ApplicationManager.getApplication(), project, plugin);
                        }
                        else {
                            commandProcessor = new ScriptCommandProcessor(ApplicationManager.getApplication());
                        }

                        if (synchronous) {
                            commandProcessor.processScriptFileSynchronously(scriptFile, new ScriptProcessorCallbackImpl(pluginScript, scriptFile));
                        }
                        else {
                            commandProcessor.processScriptFile(scriptFile, new ScriptProcessorCallbackImpl(pluginScript, scriptFile));
                        }
                    } catch (Exception e) {
                        logger.error("Error running script file = " + scriptFile, e);
                    }
                }
            }
        }
    }

    private class ScriptProcessorCallbackImpl implements ScriptProcessorCallback {
        private PluginScript pluginScript;
        private File pluginScriptFile;

        ScriptProcessorCallbackImpl(PluginScript pluginScript, File pluginScriptFile) {
            this.pluginScript = pluginScript;
            this.pluginScriptFile = pluginScriptFile;
        }

        @Override
        public void success() {
            logger.info("Completed running plugin script '" + pluginScript.getFilePath() + "'");
        }

        @Override
        public void failure(Throwable throwable) {
            logger.error("Error running script file = " + pluginScriptFile, throwable);
        }

        @Override
        public void done() {

        }

        @Override
        public void println(String msg) {
            logger.warn(msg);
        }

        @Override
        public void progressln(String msg) {
            logger.warn(msg);
        }

        @Override
        public boolean hadOutput() {
            return false;
        }

        @Override
        public void startProgress() {

        }

        @Override
        public void endProgress() {

        }
    }
}
