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
package com.github.difflib.text


internal object StringUtils {

    /**
     * Replaces all opening an closing tags with `<` or `>`.
     *
     * @param str
     * @return
     */
    fun htmlEntites(str: String): String {
        return str.replace("<", "&lt;").replace(">", "&gt;")
    }

    fun normalize(str: String): String {
        return htmlEntites(str).replace("\t", "    ")
    }

    fun wrapText(list: List<String>, columnWidth: Int): List<String> {
        return list.map { line -> wrapText(line, columnWidth) }.toList()
    }

    /**
     * Wrap the text with the given column width
     *
     * @param line the text
     * @param columnWidth the given column
     * @return the wrapped text
     */
    fun wrapText(line: String, columnWidth: Int): String {
        if (columnWidth < 0) {
            throw IllegalArgumentException("columnWidth may not be less 0")
        }
        if (columnWidth == 0) {
            return line
        }
        val length = line.length
        val delimiter = "<br/>".length
        var widthIndex = columnWidth

        val b = StringBuilder(line)

        var count = 0
        while (length > widthIndex) {
            b.append(widthIndex + delimiter * count, "<br/>")
            widthIndex += columnWidth
            count++
        }

        return b.toString()
    }
}
