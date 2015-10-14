package event;/*
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
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

public class ChainActionEvent extends AnActionEvent {
    private Runnable _pendingAction;
    private final Editor _editor;
    private final Project _project;

    public ChainActionEvent(AnActionEvent e, Runnable runnable, Editor _editor, Project _project) {
        super(e.getInputEvent(), e.getDataContext(), e.getPlace(), e.getPresentation(), e.getActionManager(), e.getModifiers());
        this._pendingAction = runnable;
        this._editor = _editor;
        this._project = _project;
    }

    public Runnable getPendingAction() {
        return _pendingAction;
    }

    public Editor getEditor() {
        return _editor;
    }

    public Project getProject() {
        return _project;
    }
}
