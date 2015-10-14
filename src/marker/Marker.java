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
package marker;

import com.intellij.openapi.util.TextRange;

import java.util.ArrayList;
import java.util.Collections;

public class Marker {
  private char _markerChar;
  private ArrayList<Integer> _offsets = new ArrayList<Integer>();

  public Marker(char markerChar, int offset) {
    _markerChar = markerChar;
    addOffsetToMarker(offset);
  }

  public void addOffsetToMarker(int offset) {
    _offsets.add(offset);
  }

  public ArrayList<Integer> getOffsets() {
    return _offsets;
  }

  public char getMarkerChar() {
    return _markerChar;
  }

  public int getOffset() {
    return _offsets.get(0);
  }

  public boolean isMappingToMultipleOffset() {
    return _offsets.size() > 1;
  }

  public TextRange getTextRange() {
    Collections.sort(_offsets);

    Integer startOffset = _offsets.get(0);
    Integer endOffset = _offsets.get(_offsets.size() - 1);

    return new TextRange(startOffset, endOffset);
  }
}