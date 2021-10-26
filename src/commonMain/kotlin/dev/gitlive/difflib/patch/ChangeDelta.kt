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
 * Describes the change-delta between original and revised texts.
 *
 * @author [Dmitry Naumenko](dm.naumenko@gmail.com)
 * @param <T> The type of the compared elements in the data 'lines'.
</T> */
class ChangeDelta<T>(source: Chunk<T>, target: Chunk<T>) : AbstractDelta<T>(DeltaType.CHANGE, source, target) {
//    @Throws(PatchFailedException::class)
    override fun applyTo(target: MutableList<T>) {
        val position = source.position
        val size = source.size()
        for (i in 0 until size) {
            target.removeAt(position)
        }
        var i = 0
        for (line in this.target.lines) {
            target.add(position + i, line)
            i++
        }
    }

    override fun restore(target: MutableList<T>) {
        val position = this.target.position
        val size = this.target.size()
        for (i in 0 until size) {
            target.removeAt(position)
        }
        var i = 0
        for (line in source.lines) {
            target.add(position + i, line)
            i++
        }
    }

    override fun toString(): String {
        return ("[ChangeDelta, position: " + source.position + ", lines: "
                + source.lines + " to " + target.lines + "]")
    }

    override fun withChunks(original: Chunk<T>, revised: Chunk<T>): AbstractDelta<T> {
        return ChangeDelta(original, revised)
    }

}