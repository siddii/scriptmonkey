var timebarPkgs = new JavaImporter(com.intellij.openapi.application, com.intellij.openapi.project, com.intellij.openapi.wm);

// vsch: with() seems to hide engine scope bindings. If uncommented then engine.get throws null has no method get
//with (timebarPkgs)
{
  var project = engine.get('project');

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
    return leftFill(elapsedHrs, 2) + ":" + leftFill(elapsedMins, 2) + ":" + leftFill(elapsedSecs);
  }

  function main() {
    if (project != null) {
      var windowManager = engine.get('windowManager');
      var statusBar = windowManager.getStatusBar(project);
      var uptimeLabel = new javax.swing.JLabel();
      uptimeLabel.setToolTipText("Start Time:" + new Date(intellij.application.startTime));
      statusBar.addCustomIndicationComponent(uptimeLabel);

      var timerLabel = new javax.swing.JLabel();
      var timeFormat = new java.text.SimpleDateFormat("h:mm a");
      var dateFormat = new java.text.SimpleDateFormat("EEEEE, MMM d, yyyy");
      statusBar.addCustomIndicationComponent(timerLabel);
    }

    var r = new java.lang.Runnable()
    {
      run: function() {
        while (true) {
          var now = new java.util.Date();
          // vsch: here we need to manipulate the UI from the AWT Event Thread, so invokeLater will do the trick
          (function () {
              timerLabel.setText(" Current Time: " + timeFormat.format(now));
              timerLabel.setToolTipText(dateFormat.format(now))

              uptimeLabel.setText(" Uptime: " + getUptime(intellij.application.startTime, now.getTime()));
          }).invokeLater();
          java.lang.Thread.sleep(1000);
        }
      }
    };

    new java.lang.Thread(r).start();
    statusBar.setInfo("Time bar initialised!");
  }
}

// vsch: here we need to manipulate the UI from the AWT Event Thread, so invokeLater will do the trick
main.invokeLater();
