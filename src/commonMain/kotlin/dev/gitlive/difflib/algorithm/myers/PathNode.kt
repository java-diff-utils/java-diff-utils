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
package dev.gitlive.difflib.algorithm.myers

/**
 * A node in a diffpath.
 *
 * @author [Juanco Anez](mailto:juanco@suigeneris.org)
 */
class PathNode(
    /**
     * Position in the original sequence.
     */
    val i: Int,
    /**
     * Position in the revised sequence.
     */
    val j: Int, snake: Boolean,
    /**
     * Is this a bootstrap node?
     *
     *
     * In bottstrap nodes one of the two corrdinates is less than zero.
     *
     * @return tru if this is a bootstrap node.
     */
    val isBootstrap: Boolean, prev: PathNode?
) {
    /**
     * The previous node in the path.
     */
    var prev: PathNode? = null
    val isSnake: Boolean

    /**
     * Skips sequences of [PathNodes][PathNode] until a snake or bootstrap node is found, or the end of the
     * path is reached.
     *
     * @return The next first [PathNode] or bootstrap node in the path, or `null` if none found.
     */
    fun previousSnake(): PathNode? {
        if (isBootstrap) {
            return null
        }
        return if (!isSnake && prev != null) {
            prev!!.previousSnake()
        } else this
    }

    /**
     * {@inheritDoc}
     */
    override fun toString(): String {
        val buf = StringBuilder("[")
        var node: PathNode? = this
        while (node != null) {
            buf.append("(")
            buf.append(node.i)
            buf.append(",")
            buf.append(node.j)
            buf.append(")")
            node = node.prev
        }
        buf.append("]")
        return buf.toString()
    }

    /**
     * Concatenates a new path node with an existing diffpath.
     *
     * @param i The position in the original sequence for the new node.
     * @param j The position in the revised sequence for the new node.
     * @param prev The previous node in the path.
     */
    init {
        if (snake) {
            this.prev = prev
        } else {
            this.prev = prev?.previousSnake()
        }
        isSnake = snake
    }
}