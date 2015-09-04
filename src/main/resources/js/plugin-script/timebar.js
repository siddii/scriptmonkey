"strict";
var timebarPkgs = new JavaImporter(com.intellij.openapi.application, com.intellij.openapi.project, com.intellij.openapi.wm);

// leave them global so it can be re-run with changes without creating more labels on the status bar
var timerLabel;
var uptimeLabel;
var timerVersion;

// vsch: with() seems to hide engine scope bindings but not global scope bindings.
//var smEngine = engine;
//var smWindow = window;
//smWindow.println("Out of with: engine = " + engine);
//smWindow.println("Out of with: window = " + window);
//smWindow.println("Out of with: windowManager = " + windowManager);

with (timebarPkgs)
{
//  smWindow.println("In with: engine = " + engine);
//  smWindow.println("In with: window = " + window);
//  smWindow.println("In with: windowManager = " + windowManager);

  if (typeof(timerVersion) === 'undefined') timerVersion = 0;

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

  function main() {
    var project = engine.get('project');
    if (project != null) {
      var windowManager = engine.get('windowManager');
      var statusBar = windowManager.getStatusBar(project);
      if (typeof(uptimeLabel) === 'undefined') {
        uptimeLabel = new javax.swing.JLabel();
        statusBar.addCustomIndicationComponent(uptimeLabel);
      }
      uptimeLabel.setToolTipText("Start Time:" + new Date(intellij.application.startTime));

      if (typeof(timerLabel) === 'undefined') {
        timerLabel = new javax.swing.JLabel();
        statusBar.addCustomIndicationComponent(timerLabel);
      }
      var timeFormat = new java.text.SimpleDateFormat("h:mm a");
      var dateFormat = new java.text.SimpleDateFormat("EEEEE, MMM d, yyyy");
    }

    // vsch: to make this rerunnable and modifiable we exit when a new timerVersion is loaded
    timerVersion++;
    var myVersion = timerVersion;
    var r = new java.lang.Runnable()
    {
      run: function() {
        //window.println("starting " + myVersion);
        while (myVersion === timerVersion) {
          var now = new java.util.Date();

          // vsch: here we need to manipulate the UI from the AWT Event Thread, so invokeLater will do the trick
          (function () {
              timerLabel.setText(" Current Time: " + timeFormat.format(now));
              timerLabel.setToolTipText(dateFormat.format(now));

              uptimeLabel.setText(" Uptime: " + getUptime(intellij.application.startTime, now.getTime()));
          }).invokeLater();
          java.lang.Thread.sleep(1000);
        }
        //window.println("exiting " + myVersion);
      }
    };

    new java.lang.Thread(r).start();
    statusBar.setInfo("Time bar initialised!");
  }
}

// vsch: here we need to manipulate the UI from the AWT Event Thread, so invokeLater will do the trick
main.invokeLater();
