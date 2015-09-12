var pkgs = new JavaImporter(com.intellij.openapi.application, com.intellij.ide.plugins, com.intellij.openapi.extensions, com.intellij.openapi.actionSystem, com.boxysystems.scriptmonkey.intellij);

with (pkgs) {

    // vsch: ScriptMonkeyLogger is all statics, no new, just the class
    var logger = ScriptMonkeyLogger;

    function intellijVersion() {
        echo("VersionName = " + intellij.applicationInfo.versionName + ", Build date = " + intellij.applicationInfo.buildDate.getTime() + ", Build No. = " + intellij.applicationInfo.buildNumber);
    }
    intellijVersion.docString = "Print intellij version details";

    function listPlugins() {
        var plugins = intellij.application.getPlugins();
        for (var i = 0; i < plugins.length; i++) {
            echo("Name = " + plugins[i].name + ", Vendor = " + plugins[i].vendor + ", Version = " + plugins[i].version);
        }
    }
    listPlugins.docString = "List intellij plugins";

    function createAction(plugin, actionID, text, callableObject) {
        var actionManager = ActionManager.instance;
        var action = actionManager.getAction(actionID);
        if (action == null) {
            action = plugin.getClass().getClassLoader().loadClass("com.boxysystems.scriptmonkey.intellij.action.ScriptMonkeyIntelliJPluginAction", true).newInstance();
            actionManager.registerAction(actionID, action);
            action.setScriptingEngine(engine);
        }
        var templatePresentation = action.getTemplatePresentation();
        templatePresentation.setText(text);

        action.setCallableObject(callableObject);
        return action;
    }

    function info(message) {
        logger.info(message);
    }

    function error(message, t) {
        if (!t){
            t = null;
        }
        logger.error(message, t);
    }

    function warn(message) {
        logger.warn(message);
    }

    function debug(message) {
        logger.debug(message);
    }

    function main() {
        this.application = ApplicationManager.application;
        this.applicationInfo = ApplicationInfo.instance;
        return this;
    }
}

var intellij = main();
engine.put("intellij", intellij);

