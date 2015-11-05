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

import com.intellij.openapi.editor.Editor;

/**
 * Created by Rune on 14-10-2015.
 */
public class CopyFromCurrentToTargetRunnable implements Runnable {
  private int targetOffset;
  private Editor editor;

  public CopyFromCurrentToTargetRunnable(int targetOffset, Editor editor) {
    this.targetOffset = targetOffset;
    this.editor = editor;
  }

  @Override
  public void run() {
    int oldPosition = editor.getCaretModel().getOffset();
    editor.getSelectionModel().setSelection(oldPosition, targetOffset+1);
    editor.getSelectionModel().copySelectionToClipboard();
    editor.getSelectionModel().removeSelection();
  }
}
