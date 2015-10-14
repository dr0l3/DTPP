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
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;

/**
 * Created by Rune on 07-10-2015.
 */
public class ModifyTextAction extends AnAction {
  public void actionPerformed(AnActionEvent e) {
    final Editor editor = e.getData(CommonDataKeys.EDITOR);
    final Project project = e.getData(CommonDataKeys.PROJECT);
    final Document document = editor.getDocument();
    final SelectionModel selectionModel = editor.getSelectionModel();
    final int start = selectionModel.getSelectionStart();
    final int end = selectionModel.getSelectionEnd();

    System.out.println("Start: " + start);
    System.out.println("End: " + end);
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        document.replaceString(start,end, "You have been reoplaced");
      }
    };
    WriteCommandAction.runWriteCommandAction(project, runnable);
    selectionModel.removeSelection();
  }
}
