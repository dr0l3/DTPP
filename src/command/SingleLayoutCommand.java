package command;/*
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
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import keylisteners.JumpToMarkupKeyListener;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import keylisteners.ShowMarkersKeyListener;
import marker.MarkerCollection;
import marker.MarkersPanel;
import offsets.CharOffsetsFinder;
import offsets.OffsetsFinder;
import offsets.WordOffsetsFinder;
import runnable.JumpRunnable;
import runnable.ShowMarkersSimpleRunnable;
import util.AppUtil;
import util.EditorUtil;

import javax.swing.*;
import java.awt.event.KeyListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by Rune on 10-10-2015.
 */
public class SingleLayoutCommand extends AnAction {
  private final String nameOfRunnable;
  protected MarkersPanel _markersPanel;
  protected JComponent _contentComponent;
  protected Editor _editor;
  protected KeyListener _showMarkersKeyListener;
  protected KeyListener _jumpToMarkerKeyListener;
  protected OffsetsFinder _offsetsFinder;
  protected KeyListener[] _keyListeners;
  protected boolean _isStillRunning;
  protected MarkerCollection _markers;
  protected Document _document;
  protected AnActionEvent _event;
  protected Project _project;

  protected Stack<EditorCommand> commandsBeforeJump_;
  protected Stack<EditorCommand> commandsAfterJump_;

  public SingleLayoutCommand(AnActionEvent e,
                             Stack<EditorCommand> commandsBeforeJump,
                             Stack<EditorCommand> commandsAfterJump,
                             String nameOfRunnable) {
    commandsAfterJump_ = commandsAfterJump;
    commandsBeforeJump_ = commandsBeforeJump;
    this.nameOfRunnable = nameOfRunnable;
    this._event = e;
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

    _offsetsFinder = new WordOffsetsFinder();
    restoreOldKeyListeners();
    _contentComponent.repaint();
    _isStillRunning = false;
  }

  public void restoreOldKeyListeners() {
    for (KeyListener kl : _keyListeners) {
      _contentComponent.addKeyListener(kl);
    }
  }

  public void initKeyListenersAndMarkerCollection() {
    _markers = new MarkerCollection();
    _showMarkersKeyListener = createShowMarkersKeyListener();
    _jumpToMarkerKeyListener = createJumpToMarkupKeyListener();
  }

  protected KeyListener createJumpToMarkupKeyListener() {
    return new JumpToMarkupKeyListener(_contentComponent, this);
  }

  protected KeyListener createShowMarkersKeyListener() {
    return new ShowMarkersKeyListener(_contentComponent, this);
  }

  public boolean handleJumpToMarkerKey(char key) {
    if (!_markers.containsKey(key)) {
      key = EditorUtil.getCounterCase(key);
    }

    if (EditorUtil.isPrintableChar(key) && _markers.containsKey(key)) {
      if (_markers.keyMappingToMultipleMarkers(key)) {
        ArrayList<Integer> offsets = _markers.get(key).getOffsets();
        _markers.clear();
        AppUtil.runReadAction(new ShowMarkersSimpleRunnable(offsets, _editor, _markers, _markersPanel, _contentComponent));
        return false;
      }

      performActionAtOffset(_markers.get(key).getOffset());
      return true;
    }

    return false;
  }

  private void performActionAtOffset(int targetOffset) {
    for (EditorCommand cmd : commandsBeforeJump_) {
      cmd.actionToPerform(_event);
    }

    try{
      ApplicationManager.getApplication().runReadAction(
        (Runnable)Class.forName(nameOfRunnable).getConstructor(int.class, Editor.class).newInstance(targetOffset, _editor));
    } catch (ClassNotFoundException e){
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }

    for (EditorCommand cmd : commandsAfterJump_) {
      cmd.actionToPerform(_event);
    }

    cleanupSetupsInAndBackToNormalEditingMode();
  }

  protected void jumpToOffset(final int jumpOffset) {
    for (EditorCommand cmd : commandsBeforeJump_) {
      cmd.actionToPerform(_event);
    }

    ApplicationManager.getApplication().runReadAction(
      new JumpRunnable(jumpOffset, _editor));

    for (EditorCommand cmd : commandsAfterJump_) {
      cmd.actionToPerform(_event);
    }

    cleanupSetupsInAndBackToNormalEditingMode();
  }



  @Override
  public void actionPerformed(AnActionEvent e) {
    _project = e.getData(CommonDataKeys.PROJECT);
    _editor = e.getData(CommonDataKeys.EDITOR);
    _document = _editor.getDocument();
    _event = e;
    _contentComponent = _editor.getContentComponent();
    _offsetsFinder = new CharOffsetsFinder();

    if (!PerformSafetyChecks(e, _project)) {
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
  }

  protected boolean PerformSafetyChecks(AnActionEvent e, Project project) {
    if (getEditorFrom(e) == null)
      return false;
    if (!ToolWindowManager.getInstance(project).isEditorComponentActive()){
      ToolWindowManager.getInstance(project).activateEditorComponent();
      return true;
    }
    return true;
  }

  protected void switchEditorIfNeed(AnActionEvent e) {
    Editor newEditor = getEditorFrom(e);
    if (_editor != null && _editor != newEditor) {
      cleanupSetupsInAndBackToNormalEditingMode();
    }

    _editor = newEditor;
  }

  protected Editor getEditorFrom(AnActionEvent e) {
    return e.getData(CommonDataKeys.EDITOR);
  }

  protected void disableAllExistingKeyListeners() {
    _keyListeners = _contentComponent.getKeyListeners();
    for (KeyListener kl : _keyListeners) {
      _contentComponent.removeKeyListener(kl);
    }
  }

  public boolean handleShowMarkersKey(char key) {
    if (EditorUtil.isPrintableChar(key)) {
      AppUtil.runReadAction(
        new ShowMarkersSimpleRunnable(getOffsetsOfCharInVisibleArea(key), _editor, _markers, _markersPanel, _contentComponent));

      if (_markers.hasNoPlaceToJump()) {
        cleanupSetupsInAndBackToNormalEditingMode();
        return false;
      }

      _contentComponent.addKeyListener(_jumpToMarkerKeyListener);
      return true;
    }

    return false;
  }

  protected List<Integer> getOffsetsOfCharInVisibleArea(char key) {
    if (_markers.get(key) != null) {
      return _markers.get(key).getOffsets();
    }

    return _offsetsFinder.getOffsets(key, _editor, _document);
  }
}
