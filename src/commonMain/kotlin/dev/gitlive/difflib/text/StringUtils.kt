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
package dev.gitlive.difflib.text

internal object StringUtils {
    /**
     * Replaces all opening and closing tags with `<` or `>`.
     *
     * @param str
     * @return str with some HTML meta characters escaped.
     */
    @kotlin.jvm.JvmStatic
    fun htmlEntites(str: String): String {
        return str.replace("<", "&lt;").replace(">", "&gt;")
    }

    @kotlin.jvm.JvmStatic
    fun normalize(str: String): String {
        return htmlEntites(str).replace("\t", "    ")
    }

    fun wrapText(list: List<String>, columnWidth: Int): List<String> {
        return list.asSequence()
            .map { line: String -> wrapText(line, columnWidth) }
            .toList()
    }

    /**
     * Wrap the text with the given column width
     *
     * @param line the text
     * @param columnWidth the given column
     * @return the wrapped text
     */
    @OptIn(ExperimentalStdlibApi::class)
    @kotlin.jvm.JvmStatic
    fun wrapText(line: String, columnWidth: Int): String {
        require(columnWidth >= 0) { "columnWidth may not be less 0" }
        if (columnWidth == 0) {
            return line
        }
        val length = line.length
        val delimiter = "<br/>".length
        var widthIndex = columnWidth
        val b = StringBuilder(line)
        var count = 0
        while (length > widthIndex) {
            var breakPoint = widthIndex + delimiter * count
            if (b[breakPoint - 1].isHighSurrogate() &&
                b[breakPoint].isLowSurrogate()
            ) {
                // Shift a breakpoint that would split a supplemental code-point.
                breakPoint += 1
                if (breakPoint == b.length) {
                    // Break before instead of after if this is the last code-point.
                    breakPoint -= 2
                }
            }
            b.insert(breakPoint, "<br/>")
            widthIndex += columnWidth
            count++
        }
        return b.toString()
    }
}