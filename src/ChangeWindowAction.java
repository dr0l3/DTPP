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
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.layout.Tab;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.impl.ContentManagerImpl;
import com.intellij.ui.switcher.SwitchManager;

import javax.swing.*;
import java.util.List;

/**
 * Created by Rune on 11-10-2015.
 */
public class ChangeWindowAction extends AnAction {
  public void actionPerformed(AnActionEvent e) {
    Project project = e.getData(CommonDataKeys.PROJECT);
    Editor editor = e.getData(CommonDataKeys.EDITOR);

    WindowManager wm = WindowManager.getInstance();
    IdeFrame[] list = wm.getAllProjectFrames();
    System.out.println(list.length);
    Application app = ApplicationManager.getApplication();
    WindowManager windowManager = WindowManager.getInstance();
    JFrame frame = windowManager.getFrame(project);
    IdeFrame ideFrame = windowManager.getIdeFrame(project);
    DataManager dataManager = DataManager.getInstance();
    SwitchManager switchManager = SwitchManager.getInstance(project);
    if (switchManager != null) {
      System.out.println(switchManager.getComponentName());
    }
    FileEditor[] editorList = FileEditorManager.getInstance(e.getProject()).getAllEditors();
    System.out.println(editorList.length);
    FileEditor fileEditor = editorList[1];
    //fileEditor.selectNotify();

    //Object state = fileEditor.getState(FileEditorStateLevel.FULL);
    FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
    System.out.println(fileEditorManager.getSelectedEditors().toString());
    VirtualFile file = fileEditorManager.getOpenFiles()[1];
    System.out.println(file.toString());
    //fileEditorManager.setSelectedEditor(file, TextEditorProvider.getInstance().getEditorTypeId());
    //FileEditorManager fileEditorManager = FileEditorManager.getInstance(.setSelectedEditor(fileEditor, fileEditor.get);)
    //IdeFocusManager.getGlobalInstance().requestFocus(fileEditor.getComponent(), true);
    //fileEditor.getComponent().requestFocusInWindow();
  }
}
