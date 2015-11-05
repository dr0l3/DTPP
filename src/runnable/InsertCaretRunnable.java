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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.VisualPosition;

/**
 * Created by Rune on 10-10-2015.
 */
public class InsertCaretRunnable implements Runnable {
  private int offset;
  private Editor editor;

  public InsertCaretRunnable(int offset, Editor editor) {
    this.offset = offset;
    this.editor = editor;
  }

  @Override
  public void run() {
    //not exactly the cleanest way, but the API does not support getting visual positions from offsets.
    int oldPosition = editor.getCaretModel().getOffset();
    editor.getCaretModel().moveToOffset(offset);
    VisualPosition posToInsertCaretAt = editor.getCaretModel().getVisualPosition();
    editor.getCaretModel().moveToOffset(oldPosition);
    editor.getCaretModel().addCaret(posToInsertCaretAt);
  }
}
