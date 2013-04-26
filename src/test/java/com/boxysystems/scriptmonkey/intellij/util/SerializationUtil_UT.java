package com.boxysystems.scriptmonkey.intellij.util;

import com.boxysystems.scriptmonkey.intellij.ui.PluginScript;
import com.boxysystems.scriptmonkey.intellij.util.SerializationUtil;
import com.boxysystems.scriptmonkey.intellij.Constants;
import com.boxysystems.scriptmonkey.intellij.ScriptMonkeySettings;
import com.boxysystems.scriptmonkey.intellij.AbstractScriptMonkeyTestCase;
import com.intellij.openapi.util.io.FileUtil;
import junit.framework.TestCase;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: shameed
 * Date: Oct 16, 2008
 * Time: 4:35:42 PM
 */
public class SerializationUtil_UT extends AbstractScriptMonkeyTestCase {

    public void testSerializeAndDeserialize() throws Exception {
        ScriptMonkeySettings settings = new ScriptMonkeySettings();
        settings.setHomeFolder(tmpFolder.getAbsolutePath());

        PluginScript script1 = new PluginScript(true, "File 1", PluginScript.RUN_MODE.INTELLIJ_STARTUP);
        PluginScript script2 = new PluginScript(false, "File 2", PluginScript.RUN_MODE.PROJECT_OPEN);
        PluginScript script3 = new PluginScript(false, "File 3", PluginScript.RUN_MODE.PROJECT_CLOSE);
        PluginScript script4 = new PluginScript(false, "File 4", PluginScript.RUN_MODE.INTELLIJ_SHUTDOWN);

        List<PluginScript> pluginScripts = Arrays.asList(script1, script2, script3, script4);
        settings.setPluginScripts(pluginScripts);

        File tempXML = new File(tmpFolder, "test.xml");

        SerializationUtil.toXml(tempXML.getAbsolutePath(), settings);

        ScriptMonkeySettings actualSettings = (ScriptMonkeySettings) SerializationUtil.fromXml(tempXML.getAbsolutePath());
        actualSettings.setHomeFolder(tmpFolder.getAbsolutePath());
        assertEquals(tmpFolder.getAbsolutePath(), actualSettings.getHomeFolder());
        assertEquals(4, actualSettings.getPluginScripts().size());


        assertTrue(actualSettings.getPluginScripts().contains(script1));
        assertTrue(actualSettings.getPluginScripts().contains(script2));
        assertTrue(actualSettings.getPluginScripts().contains(script3));
        assertTrue(actualSettings.getPluginScripts().contains(script4));
    }
}
