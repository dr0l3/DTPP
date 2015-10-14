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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import runnable.InsertCaretRunnable;
import runnable.JumpRunnable;
import runnable.ShowMarkersSimpleRunnable;
import util.AppUtil;
import util.EditorUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by Rune on 10-10-2015.
 */
public class StaticCommand extends JumpCommand {
  private Runnable commandToPerform;
  private String nameOfRunnable;

  public StaticCommand(AnActionEvent e, Stack<EditorCommand> commandsBeforeJump, Stack<EditorCommand> commandsAfterJump, String nameOfRunnable) {
    super(e, commandsBeforeJump, commandsAfterJump);
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

      //jumpToOffset(_markers.get(key).getOffset());
      performActionAtOffset(_markers.get(key).getOffset());
      return true;
    }

    return false;
  }

  @SuppressWarnings("CallToPrintStackTrace")
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
}
