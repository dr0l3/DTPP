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
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.project.Project;

/**
 * Created by Rune on 07-10-2015.
 */
public class MoveCaretAction extends AnAction {
  public void actionPerformed(AnActionEvent e) {
    //final Project project = e.getData(CommonDataKeys.PROJECT);
    //final Editor editor = e.getData(CommonDataKeys.EDITOR);
    final Editor editor = e.getData(CommonDataKeys.EDITOR);

    //e.getPresentation().setVisible((project != null
    //                                && editor != null
    //                                && editor.getSelectionModel().hasSelection()));

    CaretModel caretModel = editor.getCaretModel();
    LogicalPosition logicalPosition = caretModel.getLogicalPosition();
    int offset = caretModel.getOffset();
    //prints the current offset to the terminal in the dev environment
    System.out.println(offset);
    //moves the caret 1 step forward.
    caretModel.moveToOffset(offset+1);
  }
}
