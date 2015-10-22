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
package keylisteners;

import command.SingleLayoutCommand;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Created by Rune on 10-10-2015.
 */
public class JumpToMarkupKeyListener implements KeyListener {
  private JComponent contentComponent;
  private SingleLayoutCommand callingCommand;

  public JumpToMarkupKeyListener(JComponent contentComponent, SingleLayoutCommand callingCommmand) {
    this.contentComponent = contentComponent;
    this.callingCommand = callingCommmand;
  }

  @Override
  public void keyTyped(KeyEvent keyEvent) {
    keyEvent.consume();

    boolean jumpFinished = callingCommand.handleJumpToMarkerKey(keyEvent.getKeyChar());
    if (jumpFinished) {
      contentComponent.removeKeyListener(this);
    }
  }

  @Override
  public void keyPressed(KeyEvent keyEvent) {
    if (KeyEvent.VK_ESCAPE == keyEvent.getKeyChar()) {
      callingCommand.cleanupSetupsInAndBackToNormalEditingMode();
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {

  }
}
