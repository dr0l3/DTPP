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
package command;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;

import java.util.Stack;

/**
 * Created by Rune on 10-10-2015.
 */
public class RecordPositionCommand implements EditorCommand{
  private int offsetBeforeJump;

  @Override
  public void actionToPerform(AnActionEvent e) {
    Editor editor = e.getData(CommonDataKeys.EDITOR);
    CaretModel caretModel = editor.getCaretModel();
    LogicalPosition logicalPosition = caretModel.getLogicalPosition();
    offsetBeforeJump = caretModel.getOffset();
  }

  public int getOffsetBeforeJump() {
    return offsetBeforeJump;
  }
}
