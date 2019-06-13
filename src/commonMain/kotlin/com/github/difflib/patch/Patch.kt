/*-
 * #%L
 * java-diff-utils
 * %%
 * Copyright (C) 2009 - 2017 java-diff-utils
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */
package com.github.difflib.patch

import com.github.difflib.algorithm.Change

/**
 * Describes the patch holding all deltas between the original and revised texts.
 *
 * @author [Dmitry Naumenko](dm.naumenko@gmail.com)
 * @param <T> The type of the compared elements in the 'lines'.
</T> */
class Patch<T> constructor(estimatedPatchSize: Int = 10) {

    private val deltas: MutableList<AbstractDelta<T>>

    init {
        deltas = ArrayList(estimatedPatchSize)
    }

    /**
     * Apply this patch to the given target
     *
     * @return the patched text
     * @throws PatchFailedException if can't apply patch
     */
//    @Throws(PatchFailedException::class)
    fun applyTo(target: List<T>): List<T> {
        val result = ArrayList(target)
        val it = getDeltas().listIterator(deltas.size)
        while (it.hasPrevious()) {
            val delta = it.previous()
            delta.applyTo(result)
        }
        return result
    }

    /**
     * Restore the text to original. Opposite to applyTo() method.
     *
     * @param target the given target
     * @return the restored text
     */
    fun restore(target: List<T>): List<T> {
        val result = ArrayList(target)
        val it = getDeltas().listIterator(deltas.size)
        while (it.hasPrevious()) {
            val delta = it.previous()
            delta.restore(result)
        }
        return result
    }

    /**
     * Add the given delta to this patch
     *
     * @param delta the given delta
     */
    fun addDelta(delta: AbstractDelta<T>) {
        deltas.add(delta)
    }

    /**
     * Get the list of computed deltas
     *
     * @return the deltas
     */
    fun getDeltas(): List<AbstractDelta<T>> {
        deltas.sortBy { d -> d.source.position }
        return deltas
    }

    override fun toString(): String {
        return "Patch{deltas=$deltas}"
    }

    companion object {

        fun <T> generate(original: List<T>, revised: List<T>, changes: List<Change>): Patch<T> {
            val patch = Patch<T>(changes.size)
            for (change in changes) {
                val orgChunk = Chunk(change.startOriginal, ArrayList(original.subList(change.startOriginal, change.endOriginal)))
                val revChunk = Chunk(change.startRevised, ArrayList(revised.subList(change.startRevised, change.endRevised)))
                when (change.deltaType) {
                    DeltaType.DELETE -> patch.addDelta(DeleteDelta(orgChunk, revChunk))
                    DeltaType.INSERT -> patch.addDelta(InsertDelta(orgChunk, revChunk))
                    DeltaType.CHANGE -> patch.addDelta(ChangeDelta(orgChunk, revChunk))
                }
            }
            return patch
        }
    }
}
