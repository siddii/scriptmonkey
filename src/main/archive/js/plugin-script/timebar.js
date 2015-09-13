"strict";

// leave them global so it ca be re-run with changes without creating more labels on the status bar
/* comment */
var smTB;

(function () {
    if (smTB == undefined) {
        smTB = {
            timerLabel: null,
            uptimeLabel: null,
            timerVersion: 0,
            startUpdater: false,
            updateThread: null
        };
    }

    var Date = Java.type("java.util.Date");
    var JLabel = Java.type("javax.swing.JLabel");
    var Runnable = Java.type("java.lang.Runnable");
    var Thread = Java.type("java.lang.Thread");
    var SimpleDateFormat = Java.type("java.text.SimpleDateFormat");
    with(new JavaImporter(com.intellij.openapi.application, com.intellij.openapi.project, com.intellij.openapi.wm)) {

        function leftFill(num, places, char) {
            if (arguments.length < 3) char = '0';
            s = "" + num;
            while (s.length < places) s = char + s;
            return s;
        }

        function getUptime(startTime, currentTime) {
            var diffInMillis = currentTime - startTime;
            var elapsedHrs = parseInt(diffInMillis / (1000 * 60 * 60), 10);

            diffInMillis = diffInMillis - (1000 * 60 * 60 * elapsedHrs);

            var elapsedMins = parseInt(diffInMillis / (1000 * 60), 10);
            diffInMillis = diffInMillis - (1000 * 60 * elapsedMins);

            var elapsedSecs = parseInt(diffInMillis / 1000, 10);
            return leftFill(elapsedHrs, 2) + ":" + leftFill(elapsedMins, 2) + ":" + leftFill(elapsedSecs, 2);
        }

        var windowManager;
        var statusBar;
        var timeFormat;
        var dateFormat;

        // vsch: to make this re-runnable and modifiable we exit when a new timerVersion is loaded
        smTB.timerVersion++;
        var myVersion = smTB.timerVersion;
        smTB.updateRunnable = new Runnable(function () {
            window.println("starting " + myVersion);
            while (myVersion === smTB.timerVersion && !Thread.interrupted()) {
                var now = new Date();

                // vsch: here we need to manipulate the UI from the AWT Event Thread, so invokeLater will do the trick
                (function () {
                    smTB.timerLabel.setText(" Current Time: " + timeFormat.format(now));
                    smTB.timerLabel.setToolTipText(dateFormat.format(now));

                    smTB.uptimeLabel.setText(" Uptime: " + getUptime(intellij.application.startTime, now.getTime()));
                }).invokeLater();

                try {
                    Thread.sleep(1000);
                } catch (e) {
                    // this is what Thread.stop() looks like to JS
                    if (e.toString() == "java.lang.ThreadDeath") break;
                    break;
                }
            }
            window.println("exiting " + myVersion);
        });

        smTB.main = function () {
            var project = engine.get('project');
            if (project != null) {
                windowManager = engine.get('windowManager');
                statusBar = windowManager.getStatusBar(project);

                if (smTB.uptimeLabel == null) {
                    smTB.uptimeLabel = new JLabel();
                    statusBar.addCustomIndicationComponent(smTB.uptimeLabel);
                }
                smTB.uptimeLabel.setToolTipText("Start Time:" + new Date(intellij.application.startTime));

                if (smTB.timerLabel == null) {
                    smTB.timerLabel = new JLabel();
                    statusBar.addCustomIndicationComponent(smTB.timerLabel);
                }

                timeFormat = new SimpleDateFormat("h:mm a");
                dateFormat = new SimpleDateFormat("EEEEE, MMM d, yyyy");
            }

            if (smTB.startUpdater)(smTB.updateThread = new Thread(smTB.updateRunnable)).start();
            statusBar.setInfo("Time bar initialised!");
        }
    }
})();

// vsch: here we need to manipulate the UI from the AWT Event Thread, so invokeLater and invokeAndWait will do the trick
if (window.isScriptShell()) {
    // we wait for the UI then run the updater so script is executing and can be stopped with the stop script action
    smTB.main.invokeAndWait();
    smTB.updateRunnable.run();
} else {
    // not in the scripting shell, we just launch and forget
    smTB.startUpdater = true;
    smTB.main.invokeLater();
}