/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package doublecommandrunnable;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

/**
 * Created by Rune on 10-10-2015.
 */
public class InsertDoubleCommandRunnable implements Runnable {
  private int startOffset;
  private int endOffset;
  private Editor editor;

  public InsertDoubleCommandRunnable(int startOffset, int endOffset, Editor editor) {
    this.startOffset = startOffset;
    this.endOffset = endOffset;
    this.editor = editor;
  }

  @Override
  public void run() {
    if(startOffset< endOffset)
      endOffset++;
    else
      startOffset++;
    final int oldPosition = editor.getCaretModel().getOffset();
    int newPosition = oldPosition + Math.abs(endOffset-startOffset);
    final Document document = editor.getDocument();
    final Project project = editor.getProject();
    editor.getSelectionModel().setSelection(startOffset,endOffset);
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        document.replaceString(
          oldPosition,oldPosition, editor.getSelectionModel().getSelectedText());
      }
    };
    WriteCommandAction.runWriteCommandAction(project, runnable);
    editor.getSelectionModel().removeSelection();
    editor.getCaretModel().moveToOffset(newPosition);
  }
}
