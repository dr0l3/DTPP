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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import marker.MarkerCollection;
import marker.MarkersPanel;

import javax.swing.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Rune on 09-10-2015.
 */
public class ShowMarkersSimpleRunnable implements Runnable{
  public static final char INFINITE_JUMP_CHAR = 'f';
  private static final String _markerCharSet = "ABCDEFGHIJKLMNOPQRSTUVWZ1234567890+,"/*"abcdeghiyklmnopqrstuvwj,"*/; // / .
  private static final String _fullMarkerCharSet = "abcdeghiyklmnopqrstuvwj,"; // / .
  private final List<Integer> _offsets;
  private final Editor _editor;
  private MarkerCollection _markerCollection;
  private JComponent _contentComponent;
  private MarkersPanel _markersPanel;

  public ShowMarkersSimpleRunnable(List<Integer> _offsets,
                                   Editor editor,
                                   MarkerCollection markerCollection,
                                   MarkersPanel markersPanel,
                                   JComponent contentComponent) {
    this._offsets = _offsets;
    this._editor = editor;
    this._markerCollection = markerCollection;
    this._contentComponent = contentComponent;
    this._markersPanel = markersPanel;
  }

  @Override
  public void run() {
    if (_offsets.isEmpty()) {
      return;
    }

    int caretOffset = _editor.getCaretModel().getOffset();
    sortOffsetsByDistanceToCaret(caretOffset);
    sortOffsetsToImprovePriorityOfLineEnd(caretOffset);

    int twiceJumpGroupCount = calcTwiceJumpGroupCount();
    int singleJumpCount = Math.min(getMarkerSet().length() - twiceJumpGroupCount, _offsets.size());

    createSingleJumpMarkers(singleJumpCount);
    if (twiceJumpGroupCount > 0) {
      createMultipleJumpMarkers(singleJumpCount, twiceJumpGroupCount);
    }

    showNewMarkersPanel(_markersPanel);
  }

  public void showNewMarkersPanel(MarkersPanel markersPanel) {

    if (_markersPanel != null) {
      _contentComponent.remove(_markersPanel);
      _contentComponent.repaint();
    }

    //_markersPanel = markersPanel;
    _contentComponent.add(markersPanel);
    _contentComponent.repaint();
  }

  private String getMarkerSet() {
    return _markerCharSet;
    //return _action.isCalledFromOtherAction() ? _fullMarkerCharSet : _markerCharSet;
  }

  private void createSingleJumpMarkers(int singleJumpCount) {
    for (int i = 0; i < singleJumpCount; i++) {
      _markerCollection.addMarker(getMarkerSet().charAt(i), _offsets.get(i));
    }
  }

  private void createMultipleJumpMarkers(int singleJumpCount, int groupsNeedsTwiceJump) {
    int i = singleJumpCount;

    int maxMarkersCountNeedsTwiceJump = Math.min(_offsets.size(), groupsNeedsTwiceJump * getMarkerSet().length());
    for (; i < maxMarkersCountNeedsTwiceJump; i++) {
      int group = (i - singleJumpCount) / getMarkerSet().length();
      char markerChar = getMarkerSet().charAt(singleJumpCount + group);
      _markerCollection.addMarker(markerChar, _offsets.get(i));
    }

    boolean hasMarkersNeedMoreJumps = i < _offsets.size();
    if (hasMarkersNeedMoreJumps) {
      for (; i < _offsets.size(); i++) {
        _markerCollection.addMarker(INFINITE_JUMP_CHAR, _offsets.get(i));
      }
    }
  }

  private void sortOffsetsByDistanceToCaret(final int caretOffset) {
    Collections.sort(_offsets, new Comparator<Integer>() {
      @Override
      public int compare(Integer oA, Integer oB) {
        return Math.abs(oA - caretOffset) - Math.abs(oB - caretOffset);
      }
    });
  }

  private void sortOffsetsToImprovePriorityOfLineEnd(final int caretOffset) {
    Collections.sort(_offsets, new Comparator<Integer>() {
      @Override
      public int compare(Integer oA, Integer oB) {
        Document document = _editor.getDocument();
        boolean oAIsLineEndOffset = isLineEndOffset(oA, document);
        boolean oBIsLineEndOffset = isLineEndOffset(oB, document);

        if (!(oAIsLineEndOffset ^ oBIsLineEndOffset)) {
          return 0;
        }

        return oAIsLineEndOffset ? -1 : 1;
      }

      private boolean isLineEndOffset(Integer oA, Document document) {
        int lineA = document.getLineNumber(oA);
        int lineEndOffset = document.getLineEndOffset(lineA);
        return oA == lineEndOffset;
      }
    });
  }

  private int calcTwiceJumpGroupCount() {
    int makerCharSetSize = getMarkerSet().length();

    for (int groupsNeedMultipleJump = 0; groupsNeedMultipleJump < makerCharSetSize; groupsNeedMultipleJump++) {
      int oneJumpMarkerCount = makerCharSetSize - groupsNeedMultipleJump;
      if (groupsNeedMultipleJump * makerCharSetSize + oneJumpMarkerCount > _offsets.size()) {
        return groupsNeedMultipleJump;
      }
    }

    return makerCharSetSize;
  }
}
