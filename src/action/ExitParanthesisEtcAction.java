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
package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;

/**
 * Created by Rune on 05-11-2015.
 */
public class ExitParanthesisEtcAction extends AnAction {
  public void actionPerformed(AnActionEvent e) {
    Editor editor = e.getData(CommonDataKeys.EDITOR);
    Document document = editor.getDocument();
    Caret caret = editor.getCaretModel().getCurrentCaret();
    int offset = caret.getOffset();
    TextRange textRange = new TextRange(offset, offset+1);
    String charAtCaret = document.getText(textRange);

    while(!charAtCaret.contains("\"")
       && !charAtCaret.contains("}")
       && !charAtCaret.contains(")")
       && !charAtCaret.contains("]")
       && !charAtCaret.contains("\'")){
      offset += 1;
      caret.moveToOffset(offset);
      textRange = new TextRange(offset, offset+1);
      charAtCaret = document.getText(textRange);
    }
    offset += 1;
    caret.moveToOffset(offset);
  }
}
