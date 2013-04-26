package com.boxysystems.scriptmonkey.intellij.action;

import junit.framework.TestCase;
import com.boxysystems.scriptmonkey.intellij.ui.PluginScript;

/**
 * Created by IntelliJ IDEA.
 * User: siddique
 * Date: Oct 12, 2008
 * Time: 3:27:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class PluginScript_UT extends TestCase {

    public void testRunModeByName() throws Exception{

        PluginScript.RUN_MODE runMode = PluginScript.RUN_MODE.valueOf("INTELLIJ_STARTUP");
        assertEquals(PluginScript.RUN_MODE.INTELLIJ_STARTUP,runMode);
        assertEquals("While IntelliJ Starts",runMode.getValue());

    }

    public void testRunModeByValue() throws Exception{
        String value = "While IntelliJ Shutdowns";

        PluginScript.RUN_MODE[] runModes = PluginScript.RUN_MODE.values();

        PluginScript.RUN_MODE actualRunMode = null;
        for (int i = 0; i < runModes.length; i++) {
            PluginScript.RUN_MODE runMode = runModes[i];
            if (runMode.getValue().equals(value)) {
                actualRunMode = runMode;
                break;
            }
        }
        assertEquals(PluginScript.RUN_MODE.INTELLIJ_SHUTDOWN,actualRunMode);

    }

}
