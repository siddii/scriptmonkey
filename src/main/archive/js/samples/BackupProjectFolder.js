function backupProjectFolder() {
    var projectBaseDir = new java.io.File(project.baseDir.path);
    var outputStream = null;

    var dateFormat = new java.text.SimpleDateFormat("MM-dd-yyyy-HH-mm");
    //target file
    var backupFile = new java.io.File(projectBaseDir.parent, projectBaseDir.getName() + "-" + dateFormat.format(new java.util.Date()) + ".zip");
    try {
        echo("Backing up '" + projectBaseDir + "' to '" + backupFile + "' ...");
        outputStream = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(backupFile));

        var fileFilter = new java.io.FileFilter()
        {
            accept: function (pathname) {
                return true;
            }
        }
        ;

        com.intellij.util.io.ZipUtil.addDirToZipRecursively(outputStream, backupFile, projectBaseDir, projectBaseDir.getName(), fileFilter, new java.util.HashSet());
        echo("Backup completed successfully !");
    }
    catch (e) {
        echo(e);
    }
    finally {
        if (outputStream != null) {
            outputStream.flush();
            outputStream.close();
        }
    }
}

backupProjectFolder();



