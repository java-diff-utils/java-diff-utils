/*
 * Copyright 2018 java-diff-utils.
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
 * Abstract delta between a source and a target.
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
abstract class AbstractDelta<T>(val type: DeltaType, val source: Chunk<T>, val target: Chunk<T>) {

    /**
     * Verify the chunk of this delta, to fit the target.
     * @param target
     * @throws PatchFailedException
     */
//    @Throws(PatchFailedException::class)
    protected fun verifyChunk(target: List<T>) {
        source.verify(target)
    }

//    @Throws(PatchFailedException::class)
    abstract fun applyTo(target: MutableList<T>)

    abstract fun restore(target: MutableList<T>)

    override fun hashCode(): Int {
        return Triple(this.source, this.target, this.type).hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (this::class != obj::class) {
            return false
        }
        val other = obj as AbstractDelta<*>?
        if (this.source != other!!.source) {
            return false
        }
        if (this.target != other.target) {
            return false
        }
        return if (this.type != other.type) {
            false
        } else true
    }
}
