package com.boxysystems.scriptmonkey.intellij;

import com.boxysystems.scriptmonkey.intellij.action.CopyScriptsOnStartupAction;
import com.boxysystems.scriptmonkey.intellij.icons.Icons;
import com.boxysystems.scriptmonkey.intellij.ui.PluginScript;
import com.boxysystems.scriptmonkey.intellij.ui.ScriptMonkeyConfigurationForm;
import com.intellij.ide.plugins.cl.PluginClassLoader;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.HashSet;
import org.apache.log4j.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class ScriptMonkeyApplicationComponent implements ApplicationComponent, Configurable {
    private static final Logger logger = Logger.getLogger("com.boxysystems.scriptmonkey");

    private ScriptMonkeyConfigurationForm form = null;
    private ScriptMonkeySettings settings = null;
    private PluginScriptRunner pluginScriptRunner = new PluginScriptRunner();
    private CopyScriptsOnStartupAction copyScriptsAction;
    private HashSet<String> augmentedLibraries = new HashSet<String>();

    public ScriptMonkeyApplicationComponent() {
        ConsoleAppender appender = new ConsoleAppender(new PatternLayout("%p %m%n"));
        Enumeration<Appender> appenders = Logger.getRootLogger().getAllAppenders();
        while (appenders.hasMoreElements()) {
            Appender app = appenders.nextElement();
            String name = app.getName();
            int tmp = 0;
        }
        logger.addAppender(appender);
        logger.setAdditivity(false);
        logger.setLevel(Level.INFO);

        // add nashorn to the lib dirs of our plugin class loader
        PluginClassLoader pluginClassLoader = (PluginClassLoader) getClass().getClassLoader();
        String javaHome = System.getProperties().getProperty("java.home");
        String javaVersion = System.getProperties().getProperty("java.version");
        String libExtDir = FileUtil.join(javaHome, "lib", "ext");
        String fileName = FileUtil.join(libExtDir, "nashorn.jar");
        File file = new File(fileName);
        if (!file.exists()) {
            logger.error("Nashorn library nashorn.jar not found in the provided jre: " + javaHome + ", version " + javaVersion);
            // vsch: TODO: gracefully abort continuing to initialize and don't try to instantiate script engines
        }
        else {
            logger.info("Nashorn library nashorn.jar found in " + libExtDir + " the current jre: " + javaHome + ", version " + javaVersion);
            augmentedLibraries.add(libExtDir);
            ArrayList<String> libs = new ArrayList<String>(1);
            libs.add(libExtDir);
            pluginClassLoader.addLibDirectories(libs);
        }
    }

    // vsch: TODO: need to write a ClassLoader that will use the pluginClassLoader as parent and implement the needed
    // loading of project and module libraries. The global ones can be added to pluginClassLoader.
    // at the moment everything is added into the pluginClassLoader and that may cause problems if multiple projects are
    // open and each uses its own libraries with overlapping class names.
    public void augmentClassLoader(Project project) {
        try {
            PluginClassLoader pluginClassLoader = (PluginClassLoader) getClass().getClassLoader();
            Library[] globalLibraries = getGlobalLibraries();
            addLibrariesToClassLoader(pluginClassLoader, globalLibraries);
            Library[] projectLibraries = getProjectLibraries(project);
            addLibrariesToClassLoader(pluginClassLoader, projectLibraries);
            Library[] moduleLibraries = getModuleLibraries(project);
            addLibrariesToClassLoader(pluginClassLoader, moduleLibraries);
        } catch (Exception e) {
            logger.error("Error augmenting classloader " + e);
        }
    }

    // ClassLoader helpers
    private void addLibrariesToClassLoader(PluginClassLoader pluginClassLoader, Library[] libraries) throws MalformedURLException {
        for (Library library : libraries) {
            VirtualFile[] files = library.getFiles(OrderRootType.CLASSES);
            boolean newUrls = false;
            ArrayList<String> libs = new ArrayList<String>(files.length);
            for (VirtualFile file : files) {
                File lib = new File(file.getPresentableUrl());
                if (!augmentedLibraries.contains(lib.getAbsolutePath())) {
                    augmentedLibraries.add(lib.getAbsolutePath());
                    libs.add(lib.getAbsolutePath());
                }
            }
            pluginClassLoader.addLibDirectories(libs);
        }
    }

    private Library[] getProjectLibraries(Project project) {
        LibraryTablesRegistrar registrar = LibraryTablesRegistrar.getInstance();
        LibraryTable libraryTable = registrar.getLibraryTable(project);
        return libraryTable.getLibraries();
    }

    private Library[] getGlobalLibraries() {
        LibraryTablesRegistrar registrar = LibraryTablesRegistrar.getInstance();
        LibraryTable libraryTable = registrar.getLibraryTable();
        return libraryTable.getLibraries();
    }

    private Library[] getModuleLibraries(Project project) {
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        Module[] modules = moduleManager.getModules();
        List<Library> moduleLibraries = new ArrayList<Library>();
        for (Module module : modules) {
            ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
            ModifiableRootModel rootModel = rootManager.getModifiableModel();
            LibraryTable libraryTable = rootModel.getModuleLibraryTable();
            Collections.addAll(moduleLibraries, libraryTable.getLibraries());
        }
        return moduleLibraries.toArray(new Library[moduleLibraries.size()]);
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return this.getClass().getName();
    }

    public void setSettings(ScriptMonkeySettings settings) {
        this.settings = settings;
    }

    public void initComponent() {
        initSettings();
        copyScriptsAction = new CopyScriptsOnStartupAction();
        copyScriptsAction.copyScripts(new File(settings.getHomeFolder()));
        pluginScriptRunner.runPluginScripts(this, PluginScript.RUN_MODE.INTELLIJ_STARTUP, false);
    }

    private void initSettings() {
        settings = ScriptMonkeySettings.getInstance();
    }

    private File setupConfigDir() {
        if (!Constants.CONFIG_FOLDER.exists() && !Constants.CONFIG_FOLDER.mkdir()) {
            return null;
        }
        return Constants.CONFIG_FOLDER;
    }

    public void disposeComponent() {
        pluginScriptRunner.runPluginScripts(this, PluginScript.RUN_MODE.INTELLIJ_SHUTDOWN, true);
    }

    @Nls
    public String getDisplayName() {
        return Constants.PLUGIN_ID;
    }

    @Nullable
    public Icon getIcon() {
        return Icons.MONKEY_ICON;
    }

    @Nullable
    @NonNls
    public String getHelpTopic() {
        return null;
    }

    public JComponent createComponent() {
        if (form == null) {
            form = new ScriptMonkeyConfigurationForm();
        }
        return form.getRootComponent();
    }

    public boolean isModified() {
        if (form != null && settings != null) {
            return form.isModified(settings);
        }
        return false;
    }

    public void apply() throws ConfigurationException {
        if (form != null && settings != null) {
            form.getData(settings);
            Project[] projects = ProjectManager.getInstance().getOpenProjects();
            for (Project project : projects) {
                ScriptMonkeyPlugin.getInstance(project).getCommandShellPanel().applySettings(settings);
            }
            //      SerializationUtil.toXml(settingsFile.getAbsolutePath(), settings);
        }
    }

    public void reset() {
        if (form != null && settings != null) {
            form.setData(settings);
        }
    }

    public void disposeUIResources() {
        form = null;
    }

    public static ScriptMonkeyApplicationComponent getInstance() {
        return ApplicationManager.getApplication().getComponent(ScriptMonkeyApplicationComponent.class);
    }

    public ScriptMonkeySettings getSettings() {
        return settings;
    }

    public CopyScriptsOnStartupAction getCopyScriptsAction() {
        return copyScriptsAction;
    }

    public ScriptMonkeyConfigurationForm getForm() {
        return form;
    }
}
