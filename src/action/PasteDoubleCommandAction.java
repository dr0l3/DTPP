package action;/*
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
import command.DoubleLayoutCommand;
import command.EditorCommand;
import util.LogicUtil;

import java.util.Stack;

/**
 * Created by Rune on 10-10-2015.
 */
public class PasteDoubleCommandAction extends AnAction {
  public void actionPerformed(AnActionEvent e) {
    Stack<EditorCommand> commandsBeforeAction = LogicUtil.getNullCommandStack();
    Stack<EditorCommand> commandsAfterAction = LogicUtil.getNullCommandStack();
    DoubleLayoutCommand commandToBePerformed = new DoubleLayoutCommand(
      e,commandsBeforeAction, commandsAfterAction,"doublecommandrunnable.InsertDoubleCommandRunnable");
    commandToBePerformed.actionPerformed(e);
  }
}
