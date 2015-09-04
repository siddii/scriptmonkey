var JStringArray = Java.type("String[]");
var validExtensions = new JStringArray(".java", ".class", ".jar", ".xml");

function runFileStatistics() {
    var projectBaseDir = new java.io.File(project.baseDir.path);

    var fileStatisticsMap = new java.util.HashMap();
    countFiles(projectBaseDir, fileStatisticsMap);
    var fileStatisticsReportStr = "File Statistics\n";
    fileStatisticsReportStr    += "---------------\n";

    var keySetIter = fileStatisticsMap.keySet().iterator();

    while (keySetIter.hasNext()) {
        var ext = keySetIter.next();
        fileStatisticsReportStr = fileStatisticsReportStr + ext + " files - " + parseInt(fileStatisticsMap.get(ext), 10) + "\n";
    }
    alert(fileStatisticsReportStr,"File Statistics");
}

function getFileExtension(fileName) {
    var idx = fileName.lastIndexOf(".");

    if (idx > -1) {
        return fileName.substring(idx);
    }
    return "";
}


function countFiles(currentFolder, fileStatisticsMap) {
    var files = currentFolder.listFiles();
    for (var i = 0; i < files.length; i++) {
        if (files[i].isDirectory()) {
            countFiles(files[i], fileStatisticsMap)
        }
        else if (!files[i].isHidden()) {
            var extension = getFileExtension(files[i].path);
            if (validExtensions.contains(extension)) {
                var fileCount = new java.lang.Integer(0);
                if (fileStatisticsMap.get(extension) != null) {
                    fileCount = fileStatisticsMap.get(extension);
                }
                fileCount++;
                fileStatisticsMap.put(extension, fileCount);
            }
        }
    }
}

runFileStatistics();

