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
package live.git.difflib.patch

/**
 * Describes the add-delta between original and revised texts.
 *
 * @author [Dmitry Naumenko](dm.naumenko@gmail.com)
 * @param <T> The type of the compared elements in the 'lines'.
</T> */
class InsertDelta<T>
/**
 * Creates an insert delta with the two given chunks.
 *
 * @param original The original chunk. Must not be `null`.
 * @param revised The original chunk. Must not be `null`.
 */
(original: Chunk<T>, revised: Chunk<T>) : AbstractDelta<T>(DeltaType.INSERT, original, revised) {

//    @Throws(PatchFailedException::class)
    override fun applyTo(target: MutableList<T>) {
        verifyChunk(target)
        val position = this.source.position
        val lines = this.target.lines
        for (i in lines!!.indices) {
            target.add(position + i, lines[i])
        }
    }

    override fun restore(t: MutableList<T>) {
        val position = target.position
        val size = target.size()
        for (i in 0 until size) {
            t.removeAt(position)
        }
    }

    override fun toString(): String {
        return ("[InsertDelta, position: " + source.position
                + ", lines: " + target.lines + "]")
    }
}
