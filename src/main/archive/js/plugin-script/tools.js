var toolsPkgs = new JavaImporter(com.intellij.openapi.application,
                                 com.intellij.openapi.project,
                                 com.intellij.openapi.wm,
                                 com.intellij.openapi.actionSystem,
                                 com.intellij.openapi.fileEditor,
                                 com.intellij.openapi.vfs);

with (toolsPkgs) {

    var editors = new Array("editplus", "textpad", "notepad", "gedit", "kedit");

    var firefoxActionCallableObj = new Object();
    firefoxActionCallableObj.actionPerformed = function(anActionEvent) {
        exec("firefox");
    }

    var editorActionCallableObj = new Object();
    editorActionCallableObj.actionPerformed = function(anActionEvent) {
        for (var i = 0; i < editors.length; i++) {
            var command = commandExist(editors[i]);
            if (command != "") {
                var currentProject = getProject(anActionEvent);
                var editor = FileEditorManager.getInstance(currentProject).getSelectedTextEditor();
                var file = FileDocumentManager.getInstance().getFile(editor.getDocument());
                exec(command + " " + file.getPath());
                return;
            }
        }
        alert("No suitable editors found!");
    }

    editorActionCallableObj.update = function(anActionEvent) {
        var presentation = anActionEvent.getPresentation();

        var currentProject = getProject(anActionEvent);
        var editor = FileEditorManager.getInstance(currentProject).getSelectedTextEditor();
        if (editor != null) {
            var file = FileDocumentManager.getInstance().getFile(editor.getDocument());
            presentation.setEnabled(file != null && file.isValid() && file.exists());
        }
        else {
            presentation.setEnabled(false);
        }
    }

    var backupActionCallableObj = new Object();
    backupActionCallableObj.actionPerformed = function(anActionEvent) {
        var currentProject = getProject(anActionEvent);
        backupProjectFolder(currentProject);
    }

    function commandExist(cmd) {
        var st = new java.util.StringTokenizer(env.PATH, File.pathSeparator);
        while (st.hasMoreTokens()) {
            var file = new File(st.nextToken(), cmd);
            if (file.exists()) {
                return(file.getAbsolutePath());
            }
        }
        return "";
    }


    function getProject(anActionEvent) {
        return DataKeys.PROJECT.getData(anActionEvent.getDataContext());
    }

    function backupProjectFolder(project) {
        try
        {
            var windowManager = WindowManager.instance;
            var statusBar = windowManager.getStatusBar(project);

            var projectBaseDir = new java.io.File(project.baseDir.path);
            var outputStream = null;

            var dateFormat = new java.text.SimpleDateFormat("MM-dd-yyyy-HH-mm");
            //target file
            var backupFile = new java.io.File(projectBaseDir.parent, projectBaseDir.getName() + "-" + dateFormat.format(new java.util.Date()) + ".zip");

            statusBar.setInfo("Backing up '" + projectBaseDir + "' to '" + backupFile + "' ...");
            outputStream = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(backupFile));

            var fileFilter = new java.io.FileFilter()
            {
                accept: function(pathname) {
                    return true;
                }
            };

            com.intellij.util.io.ZipUtil.addDirToZipRecursively(outputStream, backupFile, projectBaseDir, projectBaseDir.getName(), fileFilter, new java.util.HashSet());
            alert("Project backed up successfully at '" + backupFile + "'");
        }
        catch(e) {
            alert("Error while backing up project : " + e);
        }
        finally {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        }
    }


    function main() {

        var projectManager = ProjectManager.instance;

        // do not add menu more than one time
        if (projectManager.getOpenProjects().length == 1) {

            var firefoxAction = intellij.createAction(plugin, "ScriptMonkey.MainMenu.Tools.Firefox", "Firefox", firefoxActionCallableObj);
            var editorAction = intellij.createAction(plugin, "ScriptMonkey.MainMenu.Tools.Editor", "Open with editor", editorActionCallableObj);
            var backupAction = intellij.createAction(plugin, "ScriptMonkey.MainMenu.Tools.Backup", "Backup Project", backupActionCallableObj);

            var toolsActionGroup = new DefaultActionGroup("Tools", true);
            toolsActionGroup.add(firefoxAction);
            toolsActionGroup.add(editorAction);
            toolsActionGroup.add(backupAction);

            var actionManager = ActionManager.instance;
            var mainMenu = actionManager.getAction("ScriptMonkey.MainMenu");

            var constraints = new Constraints(Anchor.BEFORE, "ScriptMonkey.MainMenu.Help");

            mainMenu.add(toolsActionGroup, constraints);

            engine.put("ScriptMonkeyToolsMenu", this);
        }
    }

}
var tools = main();
