package com.boxysystems.scriptmonkey.intellij;

import com.intellij.ide.plugins.cl.PluginClassLoader;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.lang.UrlClassLoader;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: shameed
 * Date: Apr 29, 2010
 * Time: 11:43:51 AM
 */
public class ScriptMonkeyPluginClassLoader extends UrlClassLoader {

  private static final Logger logger = Logger.getLogger(ScriptMonkeyPluginClassLoader.class);
  private ScriptMonkeyPlugin plugin;

  public ScriptMonkeyPluginClassLoader(ScriptMonkeyPlugin plugin) {
    super(((PluginClassLoader) plugin.getClass().getClassLoader()).getUrls(), plugin.getClass().getClassLoader());
    this.plugin = plugin;
  }

  public ScriptMonkeyPluginClassLoader getAugmentedClassLoader() {
    try {
      Library[] globalLibraries = getGlobalLibraries();
      addLibrariesToClassLoader(globalLibraries);
      Library[] projectLibraries = getProjectLibraries(plugin.getProject());
      addLibrariesToClassLoader(projectLibraries);
      Library[] moduleLibraries = getModuleLibraries(plugin.getProject());
      addLibrariesToClassLoader(moduleLibraries);
      return this;
    } catch (Exception e) {
      logger.error("Error augmenting classloader " + e);
    }
    return null;
  }

  private void addLibrariesToClassLoader(Library[] libraries) throws MalformedURLException {
    for (Library library : libraries) {
      VirtualFile[] files = library.getFiles(OrderRootType.CLASSES);
      for (VirtualFile file : files) {
        URL url = new File(file.getPresentableUrl()).toURI().toURL();
        if (!this.getUrls().contains(url)) {
          this.addURL(url);
        }
      }
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
}
