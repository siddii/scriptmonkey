package com.boxysystems.scriptmonkey.intellij.action;

import com.boxysystems.scriptmonkey.intellij.Constants;
import com.boxysystems.scriptmonkey.intellij.AbstractScriptMonkeyTestCase;
import com.intellij.openapi.util.io.FileUtil;
import junit.framework.TestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: siddique
 * Date: Oct 8, 2008
 * Time: 6:41:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class CopyScriptsOnStartupAction_UT extends AbstractScriptMonkeyTestCase {

    public void testCopyResources() throws Exception {

        CopyScriptsOnStartupAction action = new MockCopyScriptsOnStartupAction();
        action.copyScripts(tmpFolder);

        assertTrue(tmpFolder.listFiles().length > 0);

        List<File> jsFiles = new ArrayList<File>();
        collectJSFiles(jsFiles, tmpFolder);
        System.out.println("jsFiles = " + jsFiles);
        assertTrue(jsFiles.size() > 0);
    }

    private void collectJSFiles(List<File> jsFiles, File targetResourceFolder) {
        if (targetResourceFolder != null) {
            File childDirs[] = targetResourceFolder.listFiles();
            if (childDirs != null) {
                for (File childDir : childDirs) {
                    if (childDir.isFile() && childDir.getName().endsWith(".js")) {
                        jsFiles.add(childDir);
                    }
                    collectJSFiles(jsFiles, childDir);
                }
            }
        }
    }

   private class MockCopyScriptsOnStartupAction extends CopyScriptsOnStartupAction {

     protected File getPluginFolder() {
       File currentDir = new File(".");
       return new File(currentDir, "src/main");
     }
   }

}
