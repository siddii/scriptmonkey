package com.boxysystems.scriptmonkey.intellij.action;

import java.io.FileFilter;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: siddique
 * Date: Oct 8, 2008
 * Time: 7:29:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class JSFileFilter implements FileFilter {
    public boolean accept(File file) {
        return file != null && file.exists() && file.getName().endsWith(".js");
    }
}
