package com.boxysystems.scriptmonkey.intellij.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.editor.ex.EditReadOnlyListener;
import com.intellij.openapi.editor.ex.LineIterator;
import com.intellij.openapi.editor.ex.RangeMarkerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.UserDataHolderEx;
import com.intellij.util.Processor;
import com.intellij.util.Time;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommandShellDocument implements DocumentEx, UserDataHolderEx {
    private static final Logger logger = Logger.getLogger(CommandShellDocumentListener.class);
    private int markOffset;

    private final DocumentEx delegateDocumentEx;
    private final UserDataHolderEx delegateUserDataHolderEx;
    private final Project project;
    private final ScriptShellPanel scriptShellPanel;
    private int updating;
    private StringBuilder batchedText;
    private boolean batchedReplace;
    private boolean batchHadOutput;
    private boolean hadOutput;
    private ArrayList<Boolean> batchHadOutputStack = new ArrayList<Boolean>(10);
    private long lastOutputTime;

    public CommandShellDocument(Document delegate, ScriptShellPanel scriptShellPanel) {
        this.delegateDocumentEx = (DocumentEx) delegate;
        this.delegateUserDataHolderEx = (UserDataHolderEx) delegate;
        this.scriptShellPanel = scriptShellPanel;
        this.project = scriptShellPanel.getProject();
        this.markOffset = 0;
        this.updating = 0;
        this.batchedText = null;
        this.batchedReplace = true;
        this.batchHadOutput = false;
        this.hadOutput = false;
        this.lastOutputTime = System.currentTimeMillis();
    }

    public void beginUpdate() {
        // TODO: make document updates handle setting readonly
        //        editor.getDocument().setReadOnly(false);
        if (updating == 0) {
            batchedReplace = false;
            lastOutputTime = System.currentTimeMillis();
            hadOutput = false;
        }
        else {
            // save old value
            batchHadOutputStack.add(batchHadOutput);
        }
        batchHadOutput = false;
        updating++;
    }

    public void endUpdate() {
        endUpdate(false);
    }

    public void endUpdate(boolean forceEOL) {
        // TODO: make document updates handle setting readonly
        //        editor.getDocument().setReadOnly(true);
        if (updating != 0) {
            if (forceEOL && batchHadOutput) {
                // see if output needs to end in EOL
                if (batchedText.length() > 0 && batchedText.charAt(batchedText.length() - 1) != '\n') {
                    batchedText.append('\n');

                    // reset all parents, we now have a \n
                    int iMax = batchHadOutputStack.size();
                    for (int i = 0; i < iMax; i++) {
                        batchHadOutputStack.set(i, false);
                    }

                    batchHadOutput = false;
                }
            }

            if (updating == 1) {
                applyBatchedText(true);
            }
            else {
                // recall the previous level hadOutput flag and combine with this one if it was not reset
                batchHadOutput |= batchHadOutputStack.remove(batchHadOutputStack.size() - 1);
                updating--;
            }
        }
    }

    public boolean isUpdating() {
        return updating != 0;
    }

    public boolean hadOutput() {
        return hadOutput;
    }

    private void applyBatchedText(final boolean decrementUpdating) {
        if (batchedText != null) {
            final StringBuilder text = batchedText;
            final boolean isReplace = batchedReplace;
            final int offset = delegateDocumentEx.getTextLength();
            final boolean hadEOL = batchedText.length() > 0 && batchedText.charAt(batchedText.length() - 1) == '\n';
            batchedText = null;

            runWriteAction(new Runnable() {
                @Override
                public void run() {
                    if (isReplace) delegateDocumentEx.replaceString(0, offset, text);
                    else delegateDocumentEx.insertString(offset, text);
                    int textLength = delegateDocumentEx.getTextLength();
                    if (hadEOL) {
                        int lineStartOffset = delegateDocumentEx.getLineStartOffset(delegateDocumentEx.getLineNumber(textLength));
                        if (lineStartOffset < textLength) {
                            // delete inserted indentation at the end, we don't want it
                            delegateDocumentEx.deleteString(lineStartOffset, textLength);
                        }
                        scriptShellPanel.getEditor().getCaretModel().moveToOffset(lineStartOffset);
                    }
                    else {
                        scriptShellPanel.getEditor().getCaretModel().moveToOffset(textLength);
                    }
                    scriptShellPanel.getEditor().getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
                    setRawMark();
                    if (decrementUpdating) updating--;
                }
            });
        }
    }

    private void appendBatchedText(@NotNull String text) {
        if (batchedText == null) batchedText = new StringBuilder(text);
        else batchedText.append(text);
        if (text.length() > 0)
            hadOutput = batchHadOutput = true;
    }

    private void setBatchedText(@NotNull String text) {
        batchedText = new StringBuilder(text);
        batchedReplace = true;
        if (text.length() > 0)
            hadOutput = batchHadOutput = true;
    }

    public void safeInsertString(final int offset, String text) {
        // vsch: replace all \r\n and \r by \n, otherwise we get assertion failures
        if (text.contains("\r")) {
            text = text.replace("\r\n", "\n");
            text = text.replace('\r', '\n');
        }
        final String normalizedText = text;

        // TODO: can loosen the condition if batchedReplace then we can insert into the batchedText at the offset
        if (isUpdating() && offset >= getTextLength()) {
            // can batch it but don't wait forever, see when the last update took place and flush the batch
            appendBatchedText(text);
            if (lastOutputTime + 100 < System.currentTimeMillis()) {
                // time to flush the batch
                lastOutputTime = System.currentTimeMillis();
                applyBatchedText(false);
                batchedText = new StringBuilder();
            }
        }
        else {
            runWriteAction(new Runnable() {
                @Override
                public void run() {
                    int caretOffset = getCaretOffset();
                    delegateDocumentEx.insertString(offset, normalizedText);
                    if (offset <= caretOffset) {
                        // advance caret by amount inserted
                        setCaretOffset(caretOffset + normalizedText.length());
                    }
                }
            });
        }
    }

    public int getCaretOffset() {
        return scriptShellPanel.getEditor().getCaretModel().getPrimaryCaret().getOffset();
    }

    public int getMarkOffset() {
        int textLength = delegateDocumentEx.getTextLength();

        if (markOffset > textLength) {
            markOffset = textLength;
        }
        return markOffset;
    }

    public int getMarkLine() {
        return delegateDocumentEx.getLineNumber(getMarkOffset());
    }

    public int getMarkColumn() {
        return getMarkOffset() - delegateDocumentEx.getLineStartOffset(getMarkLine()) + 1;
    }

    //    public void setSafeCaretOffset(final int caretOffset) {
    //        setCaretOffset(caretOffset);
    //        runWriteAction(new Runnable() {
    //            @Override
    //            public void run() {
    //                setCaretOffset(caretOffset);
    //            }
    //        });
    //    }

    public void setCaretOffset(int caretOffset) {
        if (caretOffset > delegateDocumentEx.getTextLength()) caretOffset = delegateDocumentEx.getTextLength();
        scriptShellPanel.getEditor().getCaretModel().getPrimaryCaret().moveToOffset(caretOffset);
    }

    public void appendString(final String text) {
        safeInsertString(delegateDocumentEx.getTextLength(), text);
    }

    public void remove(final int offs, final int len) {
        runWriteAction(new Runnable() {
            @Override
            public void run() {
                int end = offs + len;

                int markStart = getMarkOffset();
                int markEnd = getTextLength();

                if ((end < markStart) || (offs > markEnd)) {
                    return;
                }

                int cutStart = Math.max(offs, markStart);
                int cutEnd = Math.min(end, markEnd);
                delegateDocumentEx.replaceString(cutStart, cutEnd, "");
            }
        });
    }

    protected void setRawMark() {
        //logger.error("before setRawMark() mark: " +  markOffset + ", textLength: "+ getTextLength());
        markOffset = getTextLength();
        //logger.error("after setRawMark() mark: " +  markOffset + ", textLength: "+ getTextLength());
    }

    public void setMark() {
        assert !isUpdating();
        setRawMark();
    }

    public boolean isMarkValid() {
        assert !isUpdating();
        return markOffset <= delegateDocumentEx.getTextLength();
    }

    public String getMarkedText() {
        assert !isUpdating();

        if (!isMarkValid()) return "";
        return getText(new TextRange(markOffset, getTextLength()));
    }

    public void clear() {
        setText("");
    }

    public void setText(@Nullable final String text) {
        if (isUpdating()) {
            setBatchedText(text == null ? "" : text);
        }
        else {
            runWriteAction(new Runnable() {
                @Override
                public void run() {
                    delegateDocumentEx.replaceString(0, delegateDocumentEx.getTextLength(), text == null ? "" : text);
                    final CaretModel caretModel = scriptShellPanel.getEditor().getCaretModel();
                    if (caretModel.getOffset() >= getTextLength()) {
                        caretModel.moveToOffset(getTextLength());
                    }
                    setMark();
                }
            });
        }
    }

    public void runWriteAction(@NotNull final Runnable writeAction) {
        final Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) {
            application.runWriteAction(new Runnable() {
                @Override
                public void run() {
                    CommandProcessor.getInstance().executeCommand(project, writeAction, null, null, UndoConfirmationPolicy.DEFAULT, delegateDocumentEx);
                }
            });
        }
        else {
            application.invokeLater(new Runnable() {
                @Override
                public void run() {
                    application.runWriteAction(new Runnable() {
                        @Override
                        public void run() {
                            CommandProcessor.getInstance().executeCommand(project, writeAction, null, null, UndoConfirmationPolicy.DEFAULT, delegateDocumentEx);
                        }
                    });
                }
            });
        }
    }

    /*
     * delegated methods so we can pretend to be a document
     */

    @Override
    @NotNull
    @Contract(pure = true)
    public String getText() {return delegateDocumentEx.getText();}

    @Override
    @NotNull
    @Contract(pure = true)
    public String getText(@NotNull TextRange range) {return delegateDocumentEx.getText(range);}

    @Override
    @Contract(pure = true)
    @NotNull
    public CharSequence getCharsSequence() {return delegateDocumentEx.getCharsSequence();}

    @Override
    @NotNull
    @Contract(pure = true)
    public CharSequence getImmutableCharSequence() {return delegateDocumentEx.getImmutableCharSequence();}

    @Override
    @Deprecated
    @NotNull
    public char[] getChars() {return delegateDocumentEx.getChars();}

    @Override
    @Contract(pure = true)
    public int getTextLength() {return delegateDocumentEx.getTextLength();}

    @Override
    @Contract(pure = true)
    public int getLineCount() {return delegateDocumentEx.getLineCount();}

    @Override
    @Contract(pure = true)
    public int getLineNumber(int offset) {return delegateDocumentEx.getLineNumber(offset);}

    @Override
    @Contract(pure = true)
    public int getLineStartOffset(int line) {return delegateDocumentEx.getLineStartOffset(line);}

    @Override
    @Contract(pure = true)
    public int getLineEndOffset(int line) {return delegateDocumentEx.getLineEndOffset(line);}

    @Override
    public void insertString(int offset, @NotNull CharSequence s) {delegateDocumentEx.insertString(offset, s);}

    @Override
    public void deleteString(int startOffset, int endOffset) {delegateDocumentEx.deleteString(startOffset, endOffset);}

    @Override
    public void replaceString(int startOffset, int endOffset, @NotNull CharSequence s) {delegateDocumentEx.replaceString(startOffset, endOffset, s);}

    @Override
    @Contract(pure = true)
    public boolean isWritable() {return delegateDocumentEx.isWritable();}

    @Override
    @Contract(pure = true)
    public long getModificationStamp() {return delegateDocumentEx.getModificationStamp();}

    @Override
    public void fireReadOnlyModificationAttempt() {delegateDocumentEx.fireReadOnlyModificationAttempt();}

    @Override
    public void addDocumentListener(@NotNull DocumentListener listener) {delegateDocumentEx.addDocumentListener(listener);}

    @Override
    public void addDocumentListener(@NotNull DocumentListener listener, @NotNull Disposable parentDisposable) {delegateDocumentEx.addDocumentListener(listener, parentDisposable);}

    @Override
    public void removeDocumentListener(@NotNull DocumentListener listener) {delegateDocumentEx.removeDocumentListener(listener);}

    @Override
    @NotNull
    public RangeMarker createRangeMarker(int startOffset, int endOffset) {return delegateDocumentEx.createRangeMarker(startOffset, endOffset);}

    @Override
    @NotNull
    public RangeMarker createRangeMarker(int startOffset, int endOffset, boolean surviveOnExternalChange) {return delegateDocumentEx.createRangeMarker(startOffset, endOffset, surviveOnExternalChange);}

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {delegateDocumentEx.addPropertyChangeListener(listener);}

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {delegateDocumentEx.removePropertyChangeListener(listener);}

    @Override
    public void setReadOnly(boolean isReadOnly) {delegateDocumentEx.setReadOnly(isReadOnly);}

    @Override
    @NotNull
    public RangeMarker createGuardedBlock(int startOffset, int endOffset) {return delegateDocumentEx.createGuardedBlock(startOffset, endOffset);}

    @Override
    public void removeGuardedBlock(@NotNull RangeMarker block) {delegateDocumentEx.removeGuardedBlock(block);}

    @Override
    @Nullable
    public RangeMarker getOffsetGuard(int offset) {return delegateDocumentEx.getOffsetGuard(offset);}

    @Override
    @Nullable
    public RangeMarker getRangeGuard(int start, int end) {return delegateDocumentEx.getRangeGuard(start, end);}

    @Override
    public void startGuardedBlockChecking() {delegateDocumentEx.startGuardedBlockChecking();}

    @Override
    public void stopGuardedBlockChecking() {delegateDocumentEx.stopGuardedBlockChecking();}

    @Override
    public void setCyclicBufferSize(int bufferSize) {delegateDocumentEx.setCyclicBufferSize(bufferSize);}

    @Override
    public void setText(@NotNull CharSequence text) {delegateDocumentEx.setText(text);}

    @Override
    @NotNull
    public RangeMarker createRangeMarker(@NotNull TextRange textRange) {return delegateDocumentEx.createRangeMarker(textRange);}

    @Override
    @Contract(pure = true)
    public int getLineSeparatorLength(int line) {return delegateDocumentEx.getLineSeparatorLength(line);}

    @Override
    @Nullable
    public <T> T getUserData(@NotNull Key<T> key) {return delegateDocumentEx.getUserData(key);}

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {delegateDocumentEx.putUserData(key, value);}

    @Override
    public void setStripTrailingSpacesEnabled(boolean isEnabled) {delegateDocumentEx.setStripTrailingSpacesEnabled(isEnabled);}

    @Override
    @NotNull
    public LineIterator createLineIterator() {return delegateDocumentEx.createLineIterator();}

    @Override
    public void setModificationStamp(long modificationStamp) {delegateDocumentEx.setModificationStamp(modificationStamp);}

    @Override
    public void addEditReadOnlyListener(@NotNull EditReadOnlyListener listener) {delegateDocumentEx.addEditReadOnlyListener(listener);}

    @Override
    public void removeEditReadOnlyListener(@NotNull EditReadOnlyListener listener) {delegateDocumentEx.removeEditReadOnlyListener(listener);}

    @Override
    public void replaceText(@NotNull CharSequence chars, long newModificationStamp) {delegateDocumentEx.replaceText(chars, newModificationStamp);}

    @Override
    public void moveText(int srcStart, int srcEnd, int dstOffset) {delegateDocumentEx.moveText(srcStart, srcEnd, dstOffset);}

    @Override
    public int getListenersCount() {return delegateDocumentEx.getListenersCount();}

    @Override
    public void suppressGuardedExceptions() {delegateDocumentEx.suppressGuardedExceptions();}

    @Override
    public void unSuppressGuardedExceptions() {delegateDocumentEx.unSuppressGuardedExceptions();}

    @Override
    public boolean isInEventsHandling() {return delegateDocumentEx.isInEventsHandling();}

    @Override
    public void clearLineModificationFlags() {delegateDocumentEx.clearLineModificationFlags();}

    @Override
    public boolean removeRangeMarker(@NotNull RangeMarkerEx rangeMarker) {return delegateDocumentEx.removeRangeMarker(rangeMarker);}

    @Override
    public void registerRangeMarker(@NotNull RangeMarkerEx rangeMarker, int start, int end, boolean greedyToLeft, boolean greedyToRight, int layer) {delegateDocumentEx.registerRangeMarker(rangeMarker, start, end, greedyToLeft, greedyToRight, layer);}

    @Override
    public boolean isInBulkUpdate() {return delegateDocumentEx.isInBulkUpdate();}

    @Override
    public void setInBulkUpdate(boolean value) {delegateDocumentEx.setInBulkUpdate(value);}

    @Override
    @NotNull
    public List<RangeMarker> getGuardedBlocks() {return delegateDocumentEx.getGuardedBlocks();}

    @Override
    public boolean processRangeMarkers(@NotNull Processor<RangeMarker> processor) {return delegateDocumentEx.processRangeMarkers(processor);}

    @Override
    public boolean processRangeMarkersOverlappingWith(int start, int end, @NotNull Processor<RangeMarker> processor) {return delegateDocumentEx.processRangeMarkersOverlappingWith(start, end, processor);}

    @Override
    @NotNull
    public <T> T putUserDataIfAbsent(@NotNull Key<T> key, @NotNull T value) {return delegateUserDataHolderEx.putUserDataIfAbsent(key, value);}

    @Override
    public <T> boolean replace(@NotNull Key<T> key, @Nullable T oldValue, @Nullable T newValue) {return delegateUserDataHolderEx.replace(key, oldValue, newValue);}
}
