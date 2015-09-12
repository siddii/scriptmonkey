"strict";

// leave them global so it ca be re-run with changes without creating more labels on the status bar
/* comment */
var timerLabel;
var uptimeLabel;
var timerVersion;

with (new JavaImporter(com.intellij.openapi.application, com.intellij.openapi.project, com.intellij.openapi.wm))
{
  if (timerVersion == undefined) timerVersion = 0;

  function leftFill(num, places, char) {
      if (arguments.length < 3) char = '0';
      s = "" + num;
      while (s.length < places) s = char + s;
      return s;
  }

  function getUptime(startTime, currentTime) {
    var diffInMillis = currentTime - startTime;
    var elapsedHrs = parseInt(diffInMillis / (1000 * 60 * 60),10);

    diffInMillis = diffInMillis - (1000 * 60 * 60 * elapsedHrs);

    var elapsedMins = parseInt(diffInMillis / (1000 * 60),10);
    diffInMillis = diffInMillis - (1000 * 60 * elapsedMins);

    var elapsedSecs = parseInt(diffInMillis / 1000, 10)
    return leftFill(elapsedHrs, 2) + ":" + leftFill(elapsedMins, 2) + ":" + leftFill(elapsedSecs, 2);
  }

  var Date = Java.type("java.util.Date");
  var JLabel = Java.type("javax.swing.JLabel");
  var Runnable = Java.type("java.lang.Runnable");
  var Thread = Java.type("java.lang.Thread");
  var SimpleDateFormat =  Java.type("java.text.SimpleDateFormat");

  function main() {
    var project = engine.get('project');
    if (project != null) {
      var windowManager = engine.get('windowManager');
      var statusBar = windowManager.getStatusBar(project);

      if (uptimeLabel == undefined) {
        uptimeLabel = new JLabel();
        statusBar.addCustomIndicationComponent(uptimeLabel);
      }
      uptimeLabel.setToolTipText("Start Time:" + new Date(intellij.application.startTime));

      if (timerLabel == undefined) {
        timerLabel = new JLabel();
        statusBar.addCustomIndicationComponent(timerLabel);
      }

      var timeFormat = new SimpleDateFormat("h:mm a");
      var dateFormat = new SimpleDateFormat("EEEEE, MMM d, yyyy");
    }

    // vsch: to make this re-runnable and modifiable we exit when a new timerVersion is loaded
    timerVersion++;
    var myVersion = timerVersion;

    // use the nashorn syntax that converts a function to a SAM - Single Abstract Method
    new Thread(function() {
//       window.println("starting " + myVersion);
       while (myVersion === timerVersion) {
         var now = new Date();

         // vsch: here we need to manipulate the UI from the AWT Event Thread, so invokeLater will do the trick
         (function () {
             timerLabel.setText(" Current Time: " + timeFormat.format(now));
             timerLabel.setToolTipText(dateFormat.format(now));

             uptimeLabel.setText(" Uptime: " + getUptime(intellij.application.startTime, now.getTime()));
         }).invokeLater();
         Thread.sleep(1000);
       }
//       window.println("exiting " + myVersion);
    }).start();
    statusBar.setInfo("Time bar initialised!");
  }
}

// vsch: here we need to manipulate the UI from the AWT Event Thread, so invokeLater will do the trick
main.invokeLater();
