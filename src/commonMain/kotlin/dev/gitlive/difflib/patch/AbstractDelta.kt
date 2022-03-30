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
    protected fun verifyChunkToFitTarget(target: List<T>): VerifyChunk {
        return source.verifyChunk(target)
    }

//    @Throws(PatchFailedException::class)
    fun verifyAntApplyTo(target: MutableList<T>): VerifyChunk {
        val verify = verifyChunkToFitTarget(target)
        if (verify == VerifyChunk.OK) {
            applyTo(target)
        }
        return verify
    }

//    @Throws(PatchFailedException::class)
    protected abstract fun applyTo(target: MutableList<T>)
    abstract fun restore(target: MutableList<T>)

    /**
     * Create a new delta of the actual instance with customized chunk data.
     */
    abstract fun withChunks(original: Chunk<T>, revised: Chunk<T>): AbstractDelta<T>
    override fun hashCode(): Int {
        return Triple(this.source, this.target, this.type).hashCode()
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
        val other = other as AbstractDelta<*>
        if (source != other.source) {
            return false
        }
        return if (target != other.target) {
            false
        } else type == other.type
    }
}