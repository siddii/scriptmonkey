package com.boxysystems.scriptmonkey.intellij.util;

import com.boxysystems.scriptmonkey.intellij.AbstractScriptMonkeyTestCase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: siddique
 * Date: Oct 26, 2008
 * Time: 10:41:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScriptMonkeyFileUtil_UT extends AbstractScriptMonkeyTestCase {

    public void testCopyDir_TargetFolderCreated() throws Exception {
        File sourceFolder = createSourceFolderWithFile("test.txt");
        File targetFolder = new File(tmpFolder, "target");
        assertTrue(targetFolder.mkdir());
        ScriptMonkeyFileUtil.copyDir(sourceFolder, targetFolder);

        File[] files = targetFolder.listFiles();
        assertEquals(1, files.length);
        for (File file : files) {
            assertTrue(file.canWrite());
        }
    }

    public void testCopyDir_TargetFolderNotAlreadyCreated() throws Exception {
        File sourceFolder = createSourceFolderWithFile("test.txt");
        File targetFolder = new File(tmpFolder, "target");
        ScriptMonkeyFileUtil.copyDir(sourceFolder, targetFolder);

        File[] files = targetFolder.listFiles();
        assertEquals(1, files.length);
        for (File file : files) {
            assertTrue(file.canWrite());
        }
    }

    public void testCopyDir_CopyMultipleFiles() throws Exception {
        File sourceFolder = createSourceFolderWithFile("test.txt");
        createSourceFolderWithFile("test1.txt");

        File targetFolder = new File(tmpFolder, "target");
        ScriptMonkeyFileUtil.copyDir(sourceFolder, targetFolder);

        File[] files = targetFolder.listFiles();
        assertEquals(2, files.length);
        for (File file : files) {
            assertTrue(file.canWrite());
        }
    }

    public void testCopyDir_CopyFileOnlyIfItsNew() throws Exception {
        File sourceFolder = createSourceFolderWithFile("test.txt");

        File targetFolder = new File(tmpFolder, "target");
        ScriptMonkeyFileUtil.copyDir(sourceFolder, targetFolder);
        File[] targetFolderFiles = targetFolder.listFiles();

        assertEquals(1, targetFolderFiles.length);

        File[] sourceFolderFiles = sourceFolder.listFiles();

        assertEquals(sourceFolderFiles[0].lastModified(), targetFolderFiles[0].lastModified());

        String targetFileContent = new String(ScriptMonkeyFileUtil.loadFileBytes(targetFolderFiles[0]));
        assertNotNull(targetFileContent);

        assertEquals("Some file content", targetFileContent);

        Thread.sleep(2000);
        addFileContent(targetFolderFiles[0], " adding more content");

        assertTrue(targetFolderFiles[0].lastModified() > sourceFolderFiles[0].lastModified());

        ScriptMonkeyFileUtil.copyDir(sourceFolder, targetFolder);

        targetFolderFiles = targetFolder.listFiles();

        assertEquals(1, targetFolderFiles.length);

        targetFileContent = new String(ScriptMonkeyFileUtil.loadFileBytes(targetFolderFiles[0]));
        assertNotNull(targetFileContent);

        assertEquals("Some file content adding more content", targetFileContent);
    }

    private void addFileContent(File file, String content) throws IOException {
        FileWriter fw = null;
        try {

            fw = new FileWriter(file, true);
            fw.append(content);
            fw.flush();
        } finally {
            if (fw != null) {
                fw.close();
            }
        }
    }

    private File createSourceFolderWithFile(String fileName) throws IOException {

        File sourceFolder = new File(tmpFolder, "source");
        if (!sourceFolder.exists()) {
            sourceFolder.mkdir();
        }
        File testFile = new File(sourceFolder, fileName);
        FileWriter fw = null;
        try {

            fw = new FileWriter(testFile);
            fw.append("Some file content");
            fw.flush();
            return sourceFolder;
        } finally {
            if (fw != null) {
                fw.close();
            }

        }
    }
}
