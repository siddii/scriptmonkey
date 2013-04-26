package com.boxysystems.scriptmonkey.intellij.util;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;

/**
 * Created by IntelliJ IDEA.
 * User: shameed
 * Date: Nov 14, 2008
 * Time: 9:41:07 AM
 */
public class ProjectUtil {

  public static Project getProject(AnActionEvent anActionEvent) {
    return DataKeys.PROJECT.getData(anActionEvent.getDataContext());
  }

  public static Project getProject() {
    DataContext dataContext = DataManager.getInstance().getDataContext();
    return DataKeys.PROJECT.getData(dataContext);
  }
}
