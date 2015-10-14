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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import marker.MarkersPanel;
import offsets.CharOffsetsFinder;
import runnable.JumpRunnable;
import runnable.ShowMarkersSimpleRunnable;
import util.AppUtil;
import util.EditorUtil;

import java.awt.event.KeyListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by Rune on 10-10-2015.
 */
public class DoubleCommand extends JumpCommand {
  private int startOffset;
  private boolean firstJumpDone;
  private String nameOfRunnable;
  public DoubleCommand(
    AnActionEvent e, Stack<EditorCommand> commandsBeforeJump,
    Stack<EditorCommand> commandsAfterJump, String nameOfRunnable) {
    super(e, commandsBeforeJump, commandsAfterJump);
    firstJumpDone = false;
    this.nameOfRunnable = nameOfRunnable;
  }

  @Override
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

      int offset = _markers.get(key).getOffset();
      if(firstJumpDone){
        //handle the action
        executeDoubleAction(offset);
      } else {
        firstJumpDone = true;
        startOffset = offset;
        cleanupSetupsInAndBackToNormalEditingMode();
        performSecondAction(_event);
      }
      return true;
    }

    return false;
  }

  @SuppressWarnings("CallToPrintStackTrace")
  private void executeDoubleAction(int endOffset) {
    for (EditorCommand cmd : commandsBeforeJump_) {
      cmd.actionToPerform(_event);
    }

    try{
      ApplicationManager.getApplication().runReadAction(
        (Runnable)Class.forName(nameOfRunnable).getConstructor(int.class, int.class, Editor.class)
          .newInstance(startOffset, endOffset, _editor));
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

  @Override
  public boolean handleShowMarkersKey(char key) {
    if (EditorUtil.isPrintableChar(key)) {
      AppUtil.runReadAction(
        new ShowMarkersSimpleRunnable(getOffsetsOfCharInVisibleArea(key), _editor, _markers, _markersPanel, _contentComponent));

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

  public void performSecondAction(AnActionEvent e){
    _offsetsFinder = new CharOffsetsFinder();


    if (_isStillRunning) {
      cleanupSetupsInAndBackToNormalEditingMode();
    }


    disableAllExistingKeyListeners();
    initKeyListenersAndMarkerCollection();
    _contentComponent.addKeyListener(_showMarkersKeyListener);
    _markersPanel = new MarkersPanel(_editor, _markers);
  }
}
