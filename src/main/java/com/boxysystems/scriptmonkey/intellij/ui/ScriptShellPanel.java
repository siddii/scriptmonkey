package com.boxysystems.scriptmonkey.intellij.ui;

import com.boxysystems.scriptmonkey.intellij.Constants;
import com.boxysystems.scriptmonkey.intellij.ScriptMonkeySettings;
import com.boxysystems.scriptmonkey.intellij.action.RerunScriptAction;
import com.boxysystems.scriptmonkey.intellij.action.StopScriptAction;
import com.boxysystems.scriptmonkey.intellij.action.CloseScriptConsoleAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ScriptShellPanel extends JPanel {

  private ShellCommandProcessor shellCommandProcessor;
    private AnAction[] actions;
    private JEditorPane editor;

  private final ExecutorService commandExecutor =
    Executors.newSingleThreadExecutor();

  private boolean updating;

  public ScriptShellPanel(ShellCommandProcessor cmdProc, AnAction actions[]) {
    this.shellCommandProcessor = cmdProc;
      this.actions = actions;
      setLayout(new BorderLayout());

    final DefaultActionGroup toolbarGroup = new DefaultActionGroup();

    for (int i = 0; i < actions.length; i++) {
      AnAction action = actions[i];
      toolbarGroup.add(action);
    }

    final ActionManager actionManager = ActionManager.getInstance();
    final ActionToolbar toolbar = actionManager.createActionToolbar(Constants.PLUGIN_ID, toolbarGroup, false);

    add(toolbar.getComponent(), BorderLayout.WEST);

    this.editor = new JEditorPane();

    editor.setDocument(new CommandShellDocument());

    if (!cmdProc.isCommandShell()) {
      editor.setEditable(false);
    }

    JScrollPane scroller = new JScrollPane();
    scroller.getViewport().add(editor);
    add(scroller, BorderLayout.CENTER);

    editor.getDocument().addDocumentListener(new CommandShellDocumentListener(this));

    editor.addCaretListener(new CaretListener() {
      public void caretUpdate(CaretEvent e) {
        int len = editor.getDocument().getLength();
        if (e.getDot() > len) {
          editor.setCaretPosition(len);
        }
      }
    });

    if (shellCommandProcessor.isCommandShell()) {
      clear();
    }
  }

  public void requestFocus() {
    editor.requestFocus();
  }

  public void clear() {
    clear(true);
  }

  public void clear(boolean prompt) {
    CommandShellDocument d = (CommandShellDocument) editor.getDocument();
    d.clear();
    if (prompt) {
      printPrompt();
    }
    setMark();
    editor.requestFocus();
  }

  public void setMark() {
    ((CommandShellDocument) editor.getDocument()).setMark();
  }

  public String getMarkedText() {
    try {
      String s = ((CommandShellDocument) editor.getDocument()).getMarkedText();
      int i = s.length();
      while ((i > 0) && (s.charAt(i - 1) == '\n')) {
        i--;
      }
      return s.substring(0, i);
    } catch (BadLocationException e) {
      e.printStackTrace();
      return null;
    }
  }

  public void print(String s) {
    Document d = editor.getDocument();
    try {
      d.insertString(d.getLength(), s, null);
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
  }

  public void println(String s) {
    Document d = editor.getDocument();
    try {
      d.insertString(d.getLength(), s + "\n", null);
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
  }

  //TODO: Need to implement this
  public void stopScript() {
    commandExecutor.shutdownNow();
    println("Script cancelled!");
  }

  public void beginUpdate() {
    editor.setEditable(false);
    updating = true;
  }

  public void endUpdate() {
    editor.setEditable(true);
    updating = false;
  }

  public void printPrompt() {
    if (shellCommandProcessor.isCommandShell()) {
      print(getPrompt());
    }
  }

  private String getPrompt() {
    return shellCommandProcessor.getPrompt();
  }

  public boolean isUpdating() {
    return updating;
  }

  public String executeCommand(String cmd) {
    return shellCommandProcessor.executeCommand(cmd);
  }

  public ExecutorService getCommandExecutor() {
    return commandExecutor;
  }

  public JEditorPane getEditor() {
    return editor;
  }

  public void applySettings(ScriptMonkeySettings settings) {
    editor.setBackground(settings.getCommandShellBackgroundColor());
    editor.setForeground(settings.getCommandShellForegroundColor());
  }

    public void toggleActions(){
        for (int i = 0; i < actions.length; i++) {
            AnAction action = actions[i];
            if (action instanceof RerunScriptAction) {
                RerunScriptAction rerunScriptAction = (RerunScriptAction) action;
                rerunScriptAction.setEnabled(!rerunScriptAction.isEnabled());
            }

            if (action instanceof StopScriptAction) {
                StopScriptAction stopScriptAction = (StopScriptAction) action;
                stopScriptAction.setEnabled(!stopScriptAction.isEnabled());
            }

            if (action instanceof CloseScriptConsoleAction) {
                CloseScriptConsoleAction closeSctiptConsoleAction = (CloseScriptConsoleAction) action;
                closeSctiptConsoleAction.setEnabled(!closeSctiptConsoleAction.isEnabled());
            }
        }

    }

    public StopScriptAction getStopScriptAction(){
        for (int i = 0; i < actions.length; i++) {
            AnAction action = actions[i];
            if (action instanceof StopScriptAction){
                return (StopScriptAction) action;
            }
        }
        return null;            
    }

}
