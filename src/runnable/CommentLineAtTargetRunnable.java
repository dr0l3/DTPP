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
package runnable;

import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.codeInsight.actions.MultiCaretCodeInsightAction;
import com.intellij.codeInsight.actions.MultiCaretCodeInsightActionHandler;
import com.intellij.codeInsight.generation.actions.CommentByLineCommentAction;
import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.project.Project;

/**
 * Created by Rune on 05-11-2015.
 */
public class CommentLineAtTargetRunnable implements Runnable {
  private int _offsetToJump;
  private Editor _editor;

  public CommentLineAtTargetRunnable(int _offsetToJump, Editor _editor) {
    this._offsetToJump = _offsetToJump;
    this._editor = _editor;
  }

  @Override
  public void run() {
    final Project project = _editor.getProject();
    final LogicalPosition oldPos = _editor.getCaretModel().getLogicalPosition();
    Runnable runnable1 = new Runnable() {
      @Override
      public void run() {
        Caret caret = _editor.getCaretModel()
          .getCurrentCaret();
        caret.moveToOffset(_offsetToJump);

        CommentByLineCommentAction commentAction = new CommentByLineCommentAction();
        commentAction.actionPerformedImpl(project,_editor);

        caret.moveToLogicalPosition(oldPos);
      }
    };

    WriteCommandAction.runWriteCommandAction(project, runnable1);
  }
}
