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

/**
 * Describes the diff row in form [tag, oldLine, newLine) for showing the difference between two texts
 *
 * @author [Dmitry Naumenko](dm.naumenko@gmail.com)
 */
class DiffRow(
    /**
     * @param tag the tag to set
     */
    var tag: Tag,
    /**
     * @return the oldLine
     */
    val oldLine: String,
    /**
     * @return the newLine
     */
    val newLine: String
) {
    /**
     * @return the tag
     */

    enum class Tag {
        INSERT, DELETE, CHANGE, EQUAL
    }

    override fun hashCode(): Int {
        return Triple(newLine, oldLine, tag).hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null) {
            return false
        }
        if (this::class != other::class) {
            return false
        }
        val other = other as DiffRow
        if (newLine != other.newLine) {
            return false
        }
        if (oldLine != other.oldLine) {
            return false
        }
        if (tag != other.tag) {
            return false
        }
        return true
    }

    override fun toString(): String {
        return "[" + tag + "," + oldLine + "," + newLine + "]"
    }
}