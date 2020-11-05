/*
 * Copyright 2009-2017 java-diff-utils.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.difflib.text;

import java.util.List;
import static java.util.stream.Collectors.toList;

final class StringUtils {

    /**
     * Replaces all opening and closing tags with <code>&lt;</code> or <code>&gt;</code>.
     *
     * @param str
     * @return str with some HTML meta characters escaped.
     */
    public static String htmlEntites(String str) {
        return str.replace("<", "&lt;").replace(">", "&gt;");
    }

    public static String normalize(String str) {
        return htmlEntites(str).replace("\t", "    ");
    }

    public static List<String> wrapText(List<String> list, int columnWidth) {
        return list.stream()
                .map(line -> wrapText(line, columnWidth))
                .collect(toList());
    }

    /**
     * Wrap the text with the given column width
     *
     * @param line the text
     * @param columnWidth the given column
     * @return the wrapped text
     */
    public static String wrapText(String line, int columnWidth) {
        if (columnWidth < 0) {
            throw new IllegalArgumentException("columnWidth may not be less 0");
        }
        if (columnWidth == 0) {
            return line;
        }
        int length = line.length();
        int delimiter = "<br/>".length();
        int widthIndex = columnWidth;

        StringBuilder b = new StringBuilder(line);

        for (int count = 0; length > widthIndex; count++) {
            int breakPoint = widthIndex + delimiter * count;
            if (Character.isHighSurrogate(b.charAt(breakPoint - 1)) &&
                Character.isLowSurrogate(b.charAt(breakPoint))) {
              // Shift a breakpoint that would split a supplemental code-point.
              breakPoint += 1;
              if (breakPoint == b.length()) {
                // Break before instead of after if this is the last code-point.
                breakPoint -= 2;
              }
            }
            b.insert(breakPoint, "<br/>");
            widthIndex += columnWidth;
        }

        return b.toString();
    }

    private StringUtils() {
    }
}
