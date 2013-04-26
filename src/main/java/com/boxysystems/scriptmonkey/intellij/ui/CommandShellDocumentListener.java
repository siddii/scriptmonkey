package com.boxysystems.scriptmonkey.intellij.ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

/**
 * Created by IntelliJ IDEA.
 * User: shameed
 * Date: Oct 20, 2008
 * Time: 12:38:17 PM
 */
public class CommandShellDocumentListener implements DocumentListener {
  private ScriptShellPanel scriptShellPanel;

  public CommandShellDocumentListener(ScriptShellPanel scriptShellPanel) {
    this.scriptShellPanel = scriptShellPanel;
  }

  public void changedUpdate(DocumentEvent e) {
  }

  public void insertUpdate(DocumentEvent e) {
    if (scriptShellPanel.isUpdating()) {
      return;
    }
    scriptShellPanel.beginUpdate();
    scriptShellPanel.getEditor().setCaretPosition(scriptShellPanel.getEditor().getDocument().getLength());
    if (insertContains(e, '\n')) {
      String cmd = scriptShellPanel.getMarkedText();
      // Handle multi-line input
      if ((cmd.length() == 0) ||
        (cmd.charAt(cmd.length() - 1) != '\\')) {
        // Trim "\\n" combinations
        final String cmd1 = trimContinuations(cmd);
        scriptShellPanel.getCommandExecutor().execute(new Runnable() {
          public void run() {
            final String result = scriptShellPanel.executeCommand(cmd1);

            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                if (result != null) {
                  scriptShellPanel.print(result + "\n");
                }
                scriptShellPanel.printPrompt();
                scriptShellPanel.setMark();
                scriptShellPanel.endUpdate();
              }
            });
          }
        });
      } else {
        scriptShellPanel.endUpdate();
      }
    } else {
      scriptShellPanel.endUpdate();
    }
  }

  public void removeUpdate(DocumentEvent e) {
  }

  private boolean insertContains(DocumentEvent e, char c) {
    String s;
    try {
      s = scriptShellPanel.getEditor().getText(e.getOffset(), e.getLength());
      for (int i = 0; i < e.getLength(); i++) {
        if (s.charAt(i) == c) {
          return true;
        }
      }
    } catch (BadLocationException ex) {
      ex.printStackTrace();
    }
    return false;
  }

  private String trimContinuations(String text) {
    int i;
    while ((i = text.indexOf("\\\n")) >= 0) {
      text = text.substring(0, i) + text.substring(i + 1, text.length());
    }
    return text;
  }


}
