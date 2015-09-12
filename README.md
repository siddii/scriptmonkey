scriptmonkey
============

Introduction
------------
**Script Monkey** is an [IntelliJ plugin](http://plugins.jetbrains.com/plugin?pr=idea&pluginId=3674) that helps achieve the power & flexibilities of 
rhino scripting in a *plugin* environment.
In other words, it helps Java achieve the best of both worlds.

Thanks to the embedded Nashorn & Scripting API for being part of Java since v1.7. It's an all-in-one plugin which means, anything that was possible only by writing a plugin can be done using _plugin-scripts_(simple javascript code). And, any tasks that makes more sense to be scripted can be implemented using this tool.

There are some differences between Rhino and Nashorn, see the [Rhino Migration Guide](https://wiki.openjdk.java.net/display/Nashorn/Rhino+Migration+Guide).

Demos
-----
Instead of explaining what this plugin can do in words, we wanted to show some in action. 
* [Say 'Hello' to Rhino](http://scriptmonkey.boxysystems.com/demos/HelloRhino/HelloRhino.htm)
* [Let's do some command shell scripting](http://scriptmonkey.boxysystems.com/demos/CommandShell/CommandShell.htm)
* [Timebar plugin script](http://scriptmonkey.boxysystems.com/demos/TimebarPluginScript/TimebarPluginScript.htm)

Where is all the old code?
--------------------------
If you are looking for older code of this project, please head to the project's [Google code](https://code.google.com/p/scriptmonkey/) repository

Version 1.2.x WIP switch to Nashorn engine
---------------------
The source is being reworked to be Nashorn compatible so this version is experimental. It works on IDEA CE 14 and 15 EAP. 

**You no longer will need to run your IDEA under jdk 1.8** to do this you need to refer to the instructions [Selecting the JDK version the IDE will run under](https://intellij-support.jetbrains.com/hc/en-us/articles/206827547-Selecting-the-JDK-version-the-IDE-will-run-under).

As of build **1.2.7** the plugin **works** with the **bundled** OpenJdk on OS X. So if you have the a release with bundled jdk 1.8 you should be able to use the plugin without modifying the boot jdk.  

To get this version you can download the `ScriptMonkey_1.2.0.zip` from the root of the project to get the following enhancements:

-   IDEA Editor in the JS Shell toolwindow instead of JEdtiorPane. You get IDEA keymaps, advanced editing functions, Application UI theme and JS syntax highlighting.

    ![Default](https://raw.githubusercontent.com/vsch/scriptmonkey/develop/assets/ScreenShot_toolwindow_default.png)
    
    ![Darcula](https://raw.githubusercontent.com/vsch/scriptmonkey/develop/assets/ScreenShot_toolwindow_darcula.png)    

    The switchover is not complete. Hitting return when the cursor is after the last prompt position will `eval()` all text between the last prompt and the end of text.
    Otherwise, return will just insert a new line (as per your keymap) in the text.
        
    You can edit any text in the pane, but to execute it you will need to copy it to the bottom of the file, after the last prompt. If the pasted text ends in \n then it will be immediately executed, otherwise it will execute when you hit return at the end of text.
    
#### Version 1.2.8

- Fix missing scripts jar for component initialization of the scripts directory under build 142.x of IDEs.

- Change script panels now re-use command processor between runs and enable the interactive prompt so that globals can be examined after script is evaluated to ease debugging.

- Change result printing in interactive shell to do some formatting based on result type:

    - print null for commands that are not all white space, don't output anything via window.print or window.println and which return null or void. That way void functions that print will not have a trailing null after their output but null variables will show a value instead of nothing.  
    
    - strings are escaped according to Java String rules and if they contain embedded \n they are split into multiple lines with `"\n..." +` for each line to make it easier to see the structure of the resulting text while being able to cut/paste it as a string value.
    
#### Version 1.2.7

- Fix EditorImpl not released, again. Finally figured out that it was the Script panels that were causing this not the JS Shell panel.

- Change code to modify classloader to find nashorn.jar the same way that is used in vsch/idea-multimarkdown plugin.

- Change ant build of source zip to put the zip under project root instead of under src so it does not complain about including itself.

#### Version 1.2.6

-   Fixed Stop Script Action did not work on scripts that were in a tight loop without sleep(). This was done by setting the thread interrupt and waiting a maximum of 2 seconds for it to terminate. Failing that the thread is rudely stopped. So now `while(true) {}` no longer hangs forever refusing to stop.  

    Polite, long running scripts should periodically check `java.lang.Thread.interrupted()` and if it returns true then they should terminate their processing.

    There is a caveat with trying to stop a runaway script. This code will not stop because it catches `java.lang.ThreadDeath` and continues running:

    ```javascript
    while(true) { 
        try { 
            java.lang.Thread.sleep(100); 
        } catch (e) { 
           var i = "" + e; 
        } 
    }
    ```    

    For tricky code like that we have two consecutive thread stops that make even this code give up the ghost by having the second stop catch it in its exception handler. However, this code is unstoppable when running the IDEA under debugger (when debugging the plugin), no matter how many stops and cancels are issued:
    
    ```javascript
    while(true) { 
        try { 
            java.lang.Thread.sleep(100); 
        } catch (e) { 
        } 
    }
    ```    

    It does stop *most of the time* when running non-debug session. It is a timing issue since debugging slows down execution and makes the multiple stops not as effective.
    
-   Add Stop Script Action button to JS Shell so that runaway shell scripts could be interrupted.     

-   JS Shell pane now flushes accumulated text from a running script every 100ms instead of waiting for it to terminate before outputing it.

#### Version 1.2.4

-   Fix EditorImpl was not released exception on closing of the IDEA when debugging other plugins with ScriptMonkey plugin installed.

#### Version 1.2.2

-   Exception in scripts now properly reflect the source file name

-   Exceptions in JS Shell scripts reflect the actual line and column of the source in the shell pane.

-   Fixed with() was hiding all engine scope bindings, now using global scope instead. The way engines are created global scopes are not shared anyway.

-   Fixed text printed through window.println via timer would be interpreted as user typed command

-   Change timebar.js to be reloadable without adding new labels and `timebar.js` in `resources/js/plugin-script` is now working under nashorn. Add it in settings:

    ![Timebar Settings](https://raw.githubusercontent.com/vsch/scriptmonkey/develop/assets/ScreenShot_toolwindow_timebar_plugin.png)    
    
    or run as `load("scriptMonkey/js/plugin-script/timebar.js")' in the shell but provide the full path to the script and get this added to your status bar:
    
    ![Timebar Status](https://raw.githubusercontent.com/vsch/scriptmonkey/develop/assets/ScreenShot_toolwindow_timebar_statusbar.png)    
    
    All Implemented in JavaScript.
    
-   Clean-up of the hacking and experimentation is WIP.
