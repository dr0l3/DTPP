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
package offsets;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.util.TextRange;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class OffsetsFinder {
    public List<Integer> getOffsets(char key, Editor editor, Document document) {
        TextRange visibleRange = getVisibleTextRange(editor);
        List<Integer> offsets = getOffsetsOfCharIgnoreCase(String.valueOf(key), visibleRange, document, editor);

        if (key == KeyEvent.VK_SPACE) {
            offsets.addAll(getOffsetsOfCharIgnoreCase("\t\r\n", visibleRange, document, editor));
            addStartLineOffsetsTo(offsets, editor);
        } else if (key == ',') {
            offsets.addAll(getOffsetsOfCharIgnoreCase("|`/\\;.{}()[]<>?_=-+'\"!@#$%^&*", visibleRange, document, editor));
        }

        return offsets;
    }

    private void addStartLineOffsetsTo(List<Integer> offsets, Editor editor) {
        ArrayList<Integer> visibleLineStartOffsets = getVisibleLineStartOffsets(editor);
        for (Integer i : visibleLineStartOffsets) {
            if (!offsets.contains(i)) {
                offsets.add(i);
            }
        }
    }

    protected ArrayList<Integer> getOffsetsOfCharIgnoreCase(String charSet, TextRange markerRange, Document document, Editor editor) {
        ArrayList<Integer> offsets = new ArrayList<Integer>();
        String visibleText = document.getText(markerRange);

        for (char charToFind : charSet.toCharArray()) {
            char lowCase = Character.toLowerCase(charToFind);
            char upperCase = Character.toUpperCase(charToFind);

            offsets.addAll(getOffsetsOfChar(markerRange.getStartOffset(), lowCase, visibleText, editor));
            if (upperCase != lowCase) {
                offsets.addAll(getOffsetsOfChar(markerRange.getStartOffset(), upperCase, visibleText, editor));
            }
        }

        return offsets;
    }

    private ArrayList<Integer> getOffsetsOfChar(int startOffset, char c, String visibleText, Editor editor) {
        int caretOffset = editor.getCaretModel().getOffset();

        ArrayList<Integer> offsets = new ArrayList<Integer>();

        int index = visibleText.indexOf(c);
        while (index >= 0) {
            int offset = startOffset + index;

            if (isValidOffset(c, visibleText, index, offset, caretOffset) && (offset != caretOffset)) {
                offsets.add(offset);
            }

            index = visibleText.indexOf(c, index + 1);
        }

        return offsets;
    }

    protected boolean isValidOffset(char c, String visibleText, int index, int offset, int caretOffset) {
        return true;
    }

    public static ArrayList<Integer> getVisibleLineStartOffsets(Editor editor) {
        Document document = editor.getDocument();
        ArrayList<Integer> offsets = new ArrayList<Integer>();

        TextRange visibleTextRange = getVisibleTextRange(editor);
        int startLine = document.getLineNumber(visibleTextRange.getStartOffset());
        int endLine = document.getLineNumber(visibleTextRange.getEndOffset());

        for (int i = startLine; i < endLine; i++) {
            offsets.add(document.getLineStartOffset(i));
        }

        return offsets;
    }

    public static TextRange getVisibleTextRange(Editor editor) {
        Rectangle visibleArea = editor.getScrollingModel().getVisibleArea();

        LogicalPosition startLogicalPosition = editor.xyToLogicalPosition(visibleArea.getLocation());

        Double endVisualX = visibleArea.getX() + visibleArea.getWidth();
        Double endVisualY = visibleArea.getY() + visibleArea.getHeight();
        LogicalPosition endLogicalPosition = editor.xyToLogicalPosition(new Point(endVisualX.intValue(), endVisualY.intValue()));

        return new TextRange(editor.logicalPositionToOffset(startLogicalPosition), editor.logicalPositionToOffset(endLogicalPosition));
    }
}
