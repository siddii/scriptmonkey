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
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.reflect.CallerSensitive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: shameed
 * Date: Apr 29, 2010
 * Time: 11:43:51 AM
 * <p/>
 * <p/>
 * vsch: Modified to delegate to UrlClassLoader instead of extending it because all the addUrl() and related methods
 * have been removed from IDEA 15 and up.
 */
public class ScriptMonkeyPluginClassLoader
{

  private static final Logger logger = Logger.getLogger(ScriptMonkeyPluginClassLoader.class);
  private ScriptMonkeyPlugin plugin;
  protected UrlClassLoader myLoader;
  protected ArrayList<URL> myUrls = new ArrayList<URL>(10);

  public ScriptMonkeyPluginClassLoader(ScriptMonkeyPlugin plugin)
  {
    List<URL> urls = ((PluginClassLoader) plugin.getClass().getClassLoader()).getUrls();
    myUrls.addAll(urls);
    myLoader = null;
    this.plugin = plugin;
  }

  private UrlClassLoader getMyLoader()
  {
    if (myLoader == null)
    {
      UrlClassLoader.Builder builder = UrlClassLoader.build().urls(myUrls);
      builder.parent(plugin.getClass().getClassLoader());
      myLoader = builder.get();
    }
    return myLoader;
  }

  public UrlClassLoader getAugmentedClassLoader()
  {
    try
    {
      Library[] globalLibraries = getGlobalLibraries();
      addLibrariesToClassLoader(globalLibraries);
      Library[] projectLibraries = getProjectLibraries(plugin.getProject());
      addLibrariesToClassLoader(projectLibraries);
      Library[] moduleLibraries = getModuleLibraries(plugin.getProject());
      addLibrariesToClassLoader(moduleLibraries);
      return getMyLoader();
    }
    catch (Exception e)
    {
      logger.error("Error augmenting classloader " + e);
    }
    return null;
  }

  private void addLibrariesToClassLoader(Library[] libraries) throws MalformedURLException
  {
    for (Library library : libraries)
    {
      VirtualFile[] files = library.getFiles(OrderRootType.CLASSES);
      boolean newUrls = false;
      for (VirtualFile file : files)
      {
        URL url = new File(file.getPresentableUrl()).toURI().toURL();
        if (!myUrls.contains(url))
        {
          myUrls.add(url);
          myLoader = null;
        }
      }
    }
  }

  private Library[] getProjectLibraries(Project project)
  {
    LibraryTablesRegistrar registrar = LibraryTablesRegistrar.getInstance();
    LibraryTable libraryTable = registrar.getLibraryTable(project);
    return libraryTable.getLibraries();
  }

  private Library[] getGlobalLibraries()
  {
    LibraryTablesRegistrar registrar = LibraryTablesRegistrar.getInstance();
    LibraryTable libraryTable = registrar.getLibraryTable();
    return libraryTable.getLibraries();
  }

  private Library[] getModuleLibraries(Project project)
  {
    ModuleManager moduleManager = ModuleManager.getInstance(project);
    Module[] modules = moduleManager.getModules();
    List<Library> moduleLibraries = new ArrayList<Library>();
    for (Module module : modules)
    {
      ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
      ModifiableRootModel rootModel = rootManager.getModifiableModel();
      LibraryTable libraryTable = rootModel.getModuleLibraryTable();
      Collections.addAll(moduleLibraries, libraryTable.getLibraries());
    }
    return moduleLibraries.toArray(new Library[moduleLibraries.size()]);
  }

    /*
     *  Delegated methods
     *
     *
     *  vsch: CAUTION: any myLoader delegated methods that change the state of UrlClassLoader must be called after all
     *  libraries have been added because a new UrlClassLoader is created when a URL is added. Therefore after
     *  adding a library all state is reset to default.
     */

  public static UrlClassLoader.Builder build()
  {
    return UrlClassLoader.build();
  }

  public void setDefaultAssertionStatus(boolean enabled)
  {
    getMyLoader().setDefaultAssertionStatus(enabled);
  }

  public static URL getSystemResource(String name)
  {
    return ClassLoader.getSystemResource(name);
  }

  @CallerSensitive
  public static ClassLoader getSystemClassLoader()
  {
    return ClassLoader.getSystemClassLoader();
  }

  public void setClassAssertionStatus(String className, boolean enabled)
  {
    getMyLoader().setClassAssertionStatus(className, enabled);
  }

  public void clearAssertionStatus()
  {
    getMyLoader().clearAssertionStatus();
  }

  public URL getResource(String name)
  {
    return getMyLoader().getResource(name);
  }

  public void setPackageAssertionStatus(String packageName, boolean enabled)
  {
    getMyLoader().setPackageAssertionStatus(packageName, enabled);
  }

  public Enumeration<URL> getResources(String name) throws IOException
  {
    return getMyLoader().getResources(name);
  }

  @CallerSensitive
  public ClassLoader getParent()
  {
    return getMyLoader().getParent();
  }

  public static Enumeration<URL> getSystemResources(String name) throws IOException
  {
    return ClassLoader.getSystemResources(name);
  }

  public static InputStream getSystemResourceAsStream(String name)
  {
    return ClassLoader.getSystemResourceAsStream(name);
  }

  public static URL internProtocol(@NotNull URL url)
  {
    return UrlClassLoader.internProtocol(url);
  }

  public List<URL> getUrls()
  {
    return getMyLoader().getUrls();
  }

  public Class<?> loadClass(String name) throws ClassNotFoundException
  {
    return getMyLoader().loadClass(name);
  }

  @Nullable
  public URL findResource(final String name)
  {
    return getMyLoader().findResource(name);
  }

  @Nullable
  public InputStream getResourceAsStream(final String name)
  {
    return getMyLoader().getResourceAsStream(name);
  }

  public static void loadPlatformLibrary(@NotNull String libName)
  {
    UrlClassLoader.loadPlatformLibrary(libName);
  }

  @NotNull
  public static UrlClassLoader.CachePool createCachePool()
  {
    return UrlClassLoader.createCachePool();
  }
}
