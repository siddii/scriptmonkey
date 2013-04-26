package com.boxysystems.scriptmonkey.intellij;

import com.intellij.openapi.util.io.FileUtil;
import junit.framework.TestCase;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: siddique
 * Date: Oct 26, 2008
 * Time: 10:43:52 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractScriptMonkeyTestCase extends TestCase {

    protected File tmpFolder;

    protected void setUp() throws Exception {
        super.setUp();
        if (!Constants.TEMP_FOLDER.exists()) {
            assertTrue(Constants.TEMP_FOLDER.mkdir());
        }
        tmpFolder = new File(Constants.TEMP_FOLDER, this.getClass().getName());
        if (!tmpFolder.exists()) {
            assertTrue(tmpFolder.mkdir());
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        if (Constants.TEMP_FOLDER.exists()) {
            FileUtil.delete(Constants.TEMP_FOLDER);
        }
    }

}
