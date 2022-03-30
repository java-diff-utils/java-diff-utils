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
package dev.gitlive.difflib.patch

/**
 * Holds the information about the part of text involved in the diff process
 *
 *
 *
 * Text is represented as `Object[]` because the diff engine is
 * capable of handling more than plain ascci. In fact, arrays or lists of any
 * type that implements [hashCode()][java.lang.Object.hashCode] and
 * [equals()][java.lang.Object.equals] correctly can be subject to
 * differencing using this library.
 *
 *
 * @author [](dm.naumenko@gmail.com>Dmitry Naumenko</a>
@param <T> The type of the compared elements in the 'lines'.
) */
class Chunk<T> {

    /**
     * @return the start position of chunk in the text
     */
    val position: Int

    /**
     * @return the affected lines
     */
    var lines: List<T>

    /**
     * @return the positions of changed lines of chunk in the text
     */
    val changePosition: List<Int>?
    /**
     * Creates a chunk and saves a copy of affected lines
     *
     * @param position the start position
     * @param lines the affected lines
     * @param changePosition the positions of changed lines
     */
    /**
     * Creates a chunk and saves a copy of affected lines
     *
     * @param position the start position
     * @param lines the affected lines
     */
//    @JvmOverloads
    constructor(position: Int, lines: List<T>, changePosition: List<Int>? = null) {
        this.position = position
        this.lines = ArrayList(lines)
        this.changePosition = if (changePosition != null) ArrayList(changePosition) else null
    }
    /**
     * Creates a chunk and saves a copy of affected lines
     *
     * @param position the start position
     * @param lines the affected lines
     * @param changePosition the positions of changed lines
     */
    /**
     * Creates a chunk and saves a copy of affected lines
     *
     * @param position the start position
     * @param lines the affected lines
     */
//    @JvmOverloads
    constructor(position: Int, lines: Array<T>, changePosition: List<Int>? = null) {
        this.position = position
        this.lines = lines.toList()
        this.changePosition = if (changePosition != null) ArrayList(changePosition) else null
    }

    /**
     * Verifies that this chunk's saved text matches the corresponding text in
     * the given sequence.
     *
     * @param target the sequence to verify against.
     * @throws dev.gitlive.difflib.patch.PatchFailedException
     */
//    @Throws(PatchFailedException::class)
    fun verifyChunk(target: List<T>): VerifyChunk {
        if (position > target.size || last() > target.size) {
            return VerifyChunk.POSITION_OUT_OF_TARGET
        }
        for (i in 0 until size()) {
            if (target[position + i] != lines[i]) {
                return VerifyChunk.CONTENT_DOES_NOT_MATCH_TARGET
            }
        }
        return VerifyChunk.OK
    }

    fun size(): Int {
        return lines.size
    }

    /**
     * Returns the index of the last line of the chunk.
     */
    fun last(): Int {
        return position + size() - 1
    }

    override fun hashCode(): Int {
        return Triple(lines, position, size()).hashCode()
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
        val other = other as Chunk<*>?
        if (lines != other!!.lines) {
            return false
        }
        return position == other.position
    }

    override fun toString(): String {
        return "[position: " + position + ", size: " + size() + ", lines: " + lines + "]"
    }
}