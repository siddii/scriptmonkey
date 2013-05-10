package com.boxysystems.scriptmonkey.intellij.ui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class CommandShellDocument extends PlainDocument {
  private int mark;

  public void insertString(int offset, String text, AttributeSet a)
    throws BadLocationException {
    super.insertString(getLength(), text, a);
  }

  public void remove(int offs, int len) throws BadLocationException {
    int start = offs;
    int end = offs + len;

    int markStart = mark;
    int markEnd = getLength();

    if ((end < markStart) || (start > markEnd)) {
      return;
    }

    int cutStart = Math.max(start, markStart);
    int cutEnd = Math.min(end, markEnd);
    super.remove(cutStart, cutEnd - cutStart);
  }

  public void setMark() {
    mark = getLength();
  }

  public String getMarkedText() throws BadLocationException {
    return getText(mark, getLength() - mark);
  }

  public void clear() {
    try {
      super.remove(0, getLength());
      setMark();
    } catch (BadLocationException e) {
    }
  }
}
