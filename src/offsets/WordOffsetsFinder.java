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

public class WordOffsetsFinder extends OffsetsFinder {
    @Override
    protected boolean isValidOffset(char c, String visibleText, int index, int offset, int caretOffset) {
        if (!Character.isLetterOrDigit(c)) {
            return true;
        }

        boolean isBeginningChar = index == 0;
        if (isBeginningChar) {
            return true;
        }

        char charBeforeOffset = visibleText.charAt(index - 1);
        if (isCharInDifferentCategory(c, charBeforeOffset)) {
            return true;
        }

        char charAtOffset = visibleText.charAt(index);
        if (Character.isUpperCase(charAtOffset) && Character.isLowerCase(charBeforeOffset)) {
            return true;
        }

        return false;
    }

    private boolean isCharInDifferentCategory(char c, char charBeforeOffset) {
        return Character.isLetter(c) ^ Character.isLetter(charBeforeOffset)
                || Character.isDigit(c) ^ Character.isDigit(charBeforeOffset);
    }
}
