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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import command.CommandAroundJump;
import event.ChainActionEvent;
import marker.MarkerCollection;
import marker.MarkersPanel;
import offsets.CharOffsetsFinder;
import offsets.OffsetsFinder;
import offsets.WordOffsetsFinder;
import runnable.JumpRunnable;
import runnable.ShowMarkersSimpleRunnable;
import util.EditorUtil;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;

/**
 * Created by Rune on 09-10-2015.
 */
public class JumpEasyMotionActionClassName extends AnAction {
  private MarkersPanel _markersPanel;
  private JComponent _contentComponent;
  private Editor _editor;
  private KeyListener _showMarkersKeyListener;
  private KeyListener _jumpToMarkerKeyListener;
  private Stack<CommandAroundJump> _commandsAroundJump;
  private Stack<CommandAroundJump> _commandsBeforeJump;
  private Stack<CommandAroundJump> _commandsAfterJump;
  private OffsetsFinder _offsetsFinder;
  private KeyListener[] _keyListeners;
  private boolean _isStillRunning;
  private MarkerCollection _markers;
  private boolean _isCalledFromOtherAction;
  private Document _document;
  private AnActionEvent _event;

  public void actionPerformed(AnActionEvent e) {
    Project project = e.getData(CommonDataKeys.PROJECT);
    _editor = e.getData(CommonDataKeys.EDITOR);
    _document = _editor.getDocument();
    _event = e;
    _contentComponent = _editor.getContentComponent();
    _offsetsFinder = new CharOffsetsFinder();

    if (!PerformSafetyChecks(e, project)) {
      return;
    }

    if (_isStillRunning) {
      cleanupSetupsInAndBackToNormalEditingMode();
    }

    switchEditorIfNeed(e);
    disableAllExistingKeyListeners();
    initKeyListenersAndMarkerCollection();
    _contentComponent.addKeyListener(_showMarkersKeyListener);
    _markersPanel = new MarkersPanel(_editor, _markers);
    System.out.println("Action started!");
  }

  private void initKeyListenersAndMarkerCollection() {
    _markers = new MarkerCollection();
    _showMarkersKeyListener = createShowMarkersKeyListener();
    _jumpToMarkerKeyListener = createJumpToMarkupKeyListener();
  }

  private KeyListener createJumpToMarkupKeyListener() {
    return new KeyListener() {
      public void keyTyped(KeyEvent keyEvent) {
        System.out.println("Jump to markup key listener running");
        keyEvent.consume();

        boolean jumpFinished = handleJumpToMarkerKey(keyEvent.getKeyChar());
        if (jumpFinished) {
          _contentComponent.removeKeyListener(_jumpToMarkerKeyListener);
          handlePendingActionOnSuccess();
        }
      }

      public void keyPressed(KeyEvent keyEvent) {
        if (KeyEvent.VK_ESCAPE == keyEvent.getKeyChar()) {
          cleanupSetupsInAndBackToNormalEditingMode();
        }
      }

      public void keyReleased(KeyEvent keyEvent) {
      }
    };
  }
  protected void handlePendingActionOnSuccess() {
    if (_event instanceof ChainActionEvent) {
      ChainActionEvent chainActionEvent = (ChainActionEvent) _event;
      chainActionEvent.getPendingAction().run();
    }
  }

  private boolean handleJumpToMarkerKey(char key) {
    if (!_markers.containsKey(key)) {
      key = getCounterCase(key);
    }

    if (EditorUtil.isPrintableChar(key) && _markers.containsKey(key)) {
      if (_markers.keyMappingToMultipleMarkers(key)) {
        ArrayList<Integer> offsets = _markers.get(key).getOffsets();
        _markers.clear();
        runReadAction(new ShowMarkersSimpleRunnable(offsets,
                                                    _editor,
                                                    _markers,
                                                    _markersPanel,
                                                    _contentComponent));
        return false;
      }

      jumpToOffset(_markers.get(key).getOffset());
      return true;
    }

    return false;
  }

  public static char getCounterCase(char c) {
    return Character.isUpperCase(c) ? Character.toLowerCase(c) : Character.toUpperCase(c);
  }

  public void addCommandAroundJump(CommandAroundJump commandAroundJump) {
    _commandsAroundJump.push(commandAroundJump);
  }

  private boolean PerformSafetyChecks(AnActionEvent e, Project project) {
    if (getEditorFrom(e) == null)
      return false;
    if (!ToolWindowManager.getInstance(project).isEditorComponentActive()){
      ToolWindowManager.getInstance(project).activateEditorComponent();
      return true;
    }
    return true;
  }

  public void cleanupSetupsInAndBackToNormalEditingMode() {
    if (_showMarkersKeyListener != null) {
      _contentComponent.removeKeyListener(_showMarkersKeyListener);
      _showMarkersKeyListener = null;
    }

    if (_jumpToMarkerKeyListener != null) {
      _contentComponent.removeKeyListener(_jumpToMarkerKeyListener);
      _showMarkersKeyListener = null;
    }

    if (_markersPanel != null) {
      _contentComponent.remove(_markersPanel);
    }

    //_commandsAroundJump = new Stack<CommandAroundJump>();
    _offsetsFinder = new WordOffsetsFinder();
    restoreOldKeyListeners();
    _contentComponent.repaint();
    _isStillRunning = false;
    System.out.println("Cleanup done. Returning to normal mode.");
  }

  protected void restoreOldKeyListeners() {
    for (KeyListener kl : _keyListeners) {
      _contentComponent.addKeyListener(kl);
    }
  }

  private void switchEditorIfNeed(AnActionEvent e) {
    Editor newEditor = getEditorFrom(e);
    if (_editor != null && _editor != newEditor) {
      cleanupSetupsInAndBackToNormalEditingMode();
    }

    _editor = newEditor;
  }

  protected void disableAllExistingKeyListeners() {
    _keyListeners = _contentComponent.getKeyListeners();
    for (KeyListener kl : _keyListeners) {
      _contentComponent.removeKeyListener(kl);
    }
  }

  private Editor getEditorFrom(AnActionEvent e) {
    if (e instanceof ChainActionEvent) {
      ChainActionEvent chainActionEvent = (ChainActionEvent) e;
      Editor editor = chainActionEvent.getEditor();
      if (editor != null) {
        return editor;
      }
    }

    return e.getData(CommonDataKeys.EDITOR);
  }

  /*
  * State needed to run
  * - _contentComponent
  * - _showMarkersKeyListener
  * - HandleShowMarkersKey()
  * - cleanupSetupsInAndBackToNormalEditingMode();
  * -------------------- Derived------------------
  * _jumpToMarkerKeyListener  <--- cleanupSetupsInAndBackToNormalEditingMode();
  * _isStillRunning           <--- cleanupSetupsInAndBackToNormalEditingMode();
  * jumpToOffset()
  * _markers
  * */
  private KeyListener createShowMarkersKeyListener() {
    return new KeyListener() {
      public void keyTyped(KeyEvent keyEvent) {
        System.out.println("Showmarkerkey keylistener running");
        keyEvent.consume();
        boolean showMarkersFinished = handleShowMarkersKey(keyEvent.getKeyChar());
        if (showMarkersFinished) {
          _contentComponent.removeKeyListener(_showMarkersKeyListener);
        }
      }

      public void keyPressed(KeyEvent keyEvent) {
        if (KeyEvent.VK_ESCAPE == keyEvent.getKeyChar()) {
          cleanupSetupsInAndBackToNormalEditingMode();
        }
      }

      public void keyReleased(KeyEvent keyEvent) {
      }
    };
  }

  private boolean handleShowMarkersKey(char key) {
    if (EditorUtil.isPrintableChar(key)) {
      runReadAction(new ShowMarkersSimpleRunnable(getOffsetsOfCharInVisibleArea(key),
                                                  _editor,
                                                  _markers,
                                                  _markersPanel,
                                                  _contentComponent));

      if (_markers.hasNoPlaceToJump()) {
        cleanupSetupsInAndBackToNormalEditingMode();
        return false;
      }

      if (_isCalledFromOtherAction && _markers.hasOnlyOnePlaceToJump()) {
        jumpToOffset(_markers.getFirstOffset());
        return false;
      }

      _contentComponent.addKeyListener(_jumpToMarkerKeyListener);
      return true;
    }

    return false;
  }

  private void jumpToOffset(final int jumpOffset) {
    /*for (CommandAroundJump cmd : _commandsAroundJump) {
      cmd.beforeJump(jumpOffset);
    }*/

    ApplicationManager.getApplication().runReadAction(
      new JumpRunnable(jumpOffset, this, _editor));

    /*for (CommandAroundJump cmd : _commandsAroundJump) {
      cmd.afterJump(jumpOffset);
    }*/

    cleanupSetupsInAndBackToNormalEditingMode();
  }

  protected void runReadAction(ShowMarkersSimpleRunnable action) {
    ApplicationManager.getApplication().runReadAction(action);
  }

  public Editor getEditor() {
    return _editor;
  }

  private java.util.List<Integer> getOffsetsOfCharInVisibleArea(char key) {
    if (_markers.get(key) != null) {
      return _markers.get(key).getOffsets();
    }

    return _offsetsFinder.getOffsets(key, _editor, _document);
  }
}
