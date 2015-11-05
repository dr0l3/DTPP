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

import com.intellij.codeInsight.generation.actions.CommentByLineCommentAction;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.editor.actions.SplitLineAction;
import com.intellij.openapi.editor.actions.StartNewLineAction;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;

import java.awt.datatransfer.DataFlavor;

/**
 * Created by Rune on 04-11-2015.
 */
public class CreateNewlineAndJumpRunnable implements Runnable {
  private int _offsetToJump;
  private Editor _editor;

  public CreateNewlineAndJumpRunnable(int _offsetToJump, Editor _editor) {
    this._offsetToJump = _offsetToJump;
    this._editor = _editor;
  }


  @Override
  public void run() {
    final Project project = _editor.getProject();
    Runnable runnable1 = new Runnable() {
      @Override
      public void run() {
        Caret caret = _editor.getCaretModel()
          .getCurrentCaret();
        caret.moveToOffset(_offsetToJump);
        DataContext dataContext = DataManager.getInstance()
          .getDataContext(_editor.getComponent());
        EditorActionManager.getInstance()
          .getActionHandler(IdeActions.ACTION_EDITOR_START_NEW_LINE)
          .execute(_editor, caret, dataContext);
      }
    };

    WriteCommandAction.runWriteCommandAction(project,runnable1);
  }
}
