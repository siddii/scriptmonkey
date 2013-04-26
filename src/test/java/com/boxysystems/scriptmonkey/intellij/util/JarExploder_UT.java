package com.boxysystems.scriptmonkey.intellij.util;

import com.boxysystems.scriptmonkey.intellij.AbstractScriptMonkeyTestCase;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: siddique
 * Date: Oct 12, 2008
 * Time: 8:45:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class JarExploder_UT extends AbstractScriptMonkeyTestCase {


    public void testExplodeTo() throws Exception {
        assertTrue(tmpFolder.exists());
        File log4jJarFile = new File("./src/test/java/log4j.jar");
        assertTrue(log4jJarFile.exists());

        JarExploder.explodeJar(tmpFolder, log4jJarFile);

        assertTrue(tmpFolder.listFiles().length > 0);

        assertEquals(2, tmpFolder.listFiles().length);
    }

}
