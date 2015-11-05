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

import java.util.HashMap;

public class MarkerCollection extends HashMap<Character, Marker> {

  public boolean keyMappingToMultipleMarkers(char key) {
    Marker marker = this.get(key);
    return marker != null && marker.isMappingToMultipleOffset();
  }

  public void addMarker(char key, Integer offset) {
    Marker marker = this.get(key);
    if (marker == null) {
      this.put(key, new Marker(key, offset));
      return;
    }

    marker.addOffsetToMarker(offset);
  }

  public int getFirstOffset() {
    return this.values().iterator().next().getOffset();
  }

  public boolean hasOnlyOnePlaceToJump() {
    return this.size() == 1;
  }

  public boolean hasNoPlaceToJump() {
    return this.isEmpty();
  }
}