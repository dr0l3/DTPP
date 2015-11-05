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

import com.intellij.codeInsight.generation.actions.CommentByBlockCommentAction;
import com.intellij.codeInsight.generation.actions.CommentByLineCommentAction;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.project.Project;

/**
 * Created by Rune on 05-11-2015.
 */
public class CommentBlockDoubleCommandRunnable implements Runnable {
  private int startOffset;
  private int endOffset;
  private Editor editor;

  public CommentBlockDoubleCommandRunnable(int startOffset, int endOffset, Editor editor) {
    this.startOffset = startOffset;
    this.endOffset = endOffset;
    this.editor = editor;
  }

  @Override
  public void run() {
    final Project project = editor.getProject();
    final LogicalPosition oldPos = editor.getCaretModel().getLogicalPosition();
    Runnable runnable1 = new Runnable() {
      @Override
      public void run() {
        Caret caret = editor.getCaretModel()
          .getCurrentCaret();
        caret.setSelection(startOffset,endOffset+1);

        CommentByBlockCommentAction commentAction = new CommentByBlockCommentAction();
        commentAction.actionPerformedImpl(project,editor);

        caret.removeSelection();
        caret.moveToLogicalPosition(oldPos);
      }
    };

    WriteCommandAction.runWriteCommandAction(project, runnable1);
  }
}
