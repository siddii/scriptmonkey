package com.boxysystems.scriptmonkey.intellij.ui;

import com.boxysystems.scriptmonkey.intellij.icons.Icons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;

/**
 * Created by IntelliJ IDEA.
 * User: siddique
 * Date: Oct 18, 2008
 * Time: 9:41:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScriptMonkeyToolWindow {
    private ToolWindow toolWindow;

    private static final String TOOL_WINDOW_ID = "Script Monkey";
    private Project project;


    public ScriptMonkeyToolWindow(Project project) {
        this.project = project;
        toolWindow = ToolWindowManager.getInstance(project).registerToolWindow(TOOL_WINDOW_ID, false, ToolWindowAnchor.BOTTOM);
        toolWindow.setIcon(Icons.TOOLBAR_ICON);
    }


    public Content addContentPanel(String contentName, ScriptShellPanel scriptShellPanel) {

        ScriptShellTabContent scriptShellTabContent = new ScriptShellTabContent(scriptShellPanel);

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

        Content content = contentFactory.createContent(scriptShellTabContent, contentName, false);
        toolWindow.getContentManager().addContent(content);
        return content;
    }

    public void unregisterToolWindow() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        toolWindowManager.unregisterToolWindow(TOOL_WINDOW_ID);
    }

    public void activate() {
        if (toolWindow != null) {
            toolWindow.show(new Runnable() {
                public void run() {
                    // do nothing
                }
            });
        }
    }

   public ContentManager getContentManager() {
     return toolWindow.getContentManager();
   }
}
