var timebarPkgs = new JavaImporter(com.intellij.openapi.application, com.intellij.openapi.project, com.intellij.openapi.wm);

with (timebarPkgs) {

  var project = engine.get('project');


  function getUptime(startTime, currentTime) {
    var diffInMillis = currentTime - startTime;
    var elapsedHrs = parseInt(diffInMillis / (1000 * 60 * 60),10);

    diffInMillis = diffInMillis - (1000 * 60 * 60 * elapsedHrs);

    var elapsedMins = parseInt(diffInMillis / (1000 * 60),10);
    return elapsedHrs + " hrs," + elapsedMins + " mins";
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
          timerLabel.setText("Current Time: " + timeFormat.format(now));
          timerLabel.setToolTipText(dateFormat.format(now))

          uptimeLabel.setText("Uptime:" + getUptime(intellij.application.startTime, now.getTime()));
          java.lang.Thread.sleep(1000);
        }
      }
    };

    new java.lang.Thread(r).start();
    statusBar.setInfo("Time bar initialised!");
  }
}
main();