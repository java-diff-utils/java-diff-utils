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
package dev.gitlive.difflib.patch

import dev.gitlive.difflib.algorithm.Change
import kotlin.jvm.JvmOverloads

/**
 * Describes the patch holding all deltas between the original and revised
 * texts.
 *
 * @author [Dmitry Naumenko](dm.naumenko@gmail.com)
 * @param <T> The type of the compared elements in the 'lines'.
</T> */
class Patch<T> @JvmOverloads constructor(estimatedPatchSize: Int = 10) {
    private val deltas: MutableList<AbstractDelta<T>>

    /**
     * Apply this patch to the given target
     *
     * @return the patched text
     * @throws PatchFailedException if can't apply patch
     */
//    @Throws(PatchFailedException::class)
    fun applyTo(target: List<T>): List<T> {
        val result: MutableList<T> = ArrayList(target)
        val it = getDeltas().listIterator(deltas.size)
        while (it.hasPrevious()) {
            val delta = it.previous()
            val valid = delta.verifyAntApplyTo(result)
            if (valid != VerifyChunk.OK) {
                conflictOutput.processConflict(valid, delta, result)
            }
        }
        return result
    }

    /**
     * Standard Patch behaviour to throw an exception for pathching conflicts.
     */
    val CONFLICT_PRODUCES_EXCEPTION =
        object : ConflictOutput<T> {
            override fun processConflict(verifyChunk: VerifyChunk?, delta: AbstractDelta<T>, result: MutableList<T>) {
                throw PatchFailedException(
                    "could not apply patch due to $verifyChunk"
                )
            }
        }
    private var conflictOutput = CONFLICT_PRODUCES_EXCEPTION

    /**
     * Alter normal conflict output behaviour to e.g. inclide some conflict
     * statements in the result, like git does it.
     */
    fun withConflictOutput(conflictOutput: ConflictOutput<T>): Patch<*> {
        this.conflictOutput = conflictOutput
        return this
    }

    /**
     * Restore the text to original. Opposite to applyTo() method.
     *
     * @param target the given target
     * @return the restored text
     */
    fun restore(target: List<T>): List<T> {
        val result: MutableList<T> = ArrayList(target)
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
    fun getDeltas(): MutableList<AbstractDelta<T>> {
        deltas.sortBy { d -> d.source.position }
        return deltas
    }

    override fun toString(): String {
        return "Patch{deltas=$deltas}"
    }

    companion object {
        /**
         * Git like merge conflict output.
         */
        @kotlin.jvm.JvmField
        val CONFLICT_PRODUCES_MERGE_CONFLICT =
            object : ConflictOutput<String> {
                override fun processConflict(verifyChunk: VerifyChunk?, delta: AbstractDelta<String>, result: MutableList<String>) {
                    if (result.size > delta.source.position) {
                        val orgData: MutableList<String> = ArrayList()
                        for (i in 0 until delta.source.size()) {
                            orgData.add(result[delta.source.position])
                            result.removeAt(delta.source.position)
                        }
                        orgData.add(0, "<<<<<< HEAD")
                        orgData.add("======")
                        orgData.addAll(delta.source.lines)
                        orgData.add(">>>>>>> PATCH")
                        result.addAll(delta.source.position, orgData)
                    } else {
                        throw UnsupportedOperationException("Not supported yet.") //To change body of generated methods, choose Tools | Templates.
                    }
                }
            }

        fun <T> generate(original: List<T>, revised: List<T>, changes: List<Change>): Patch<T> {
            return generate(original, revised, changes, false)
        }

        private fun <T> buildChunk(start: Int, end: Int, data: List<T>): Chunk<T> {
            return Chunk(start, ArrayList(data.subList(start, end)))
        }

        fun <T> generate(
            original: List<T>,
            revised: List<T>,
            _changes: List<Change>,
            includeEquals: Boolean
        ): Patch<T> {
            val patch = Patch<T>(_changes.size)
            var startOriginal = 0
            var startRevised = 0
            var changes = _changes
            if (includeEquals) {
                changes = ArrayList(_changes)
                changes.sortBy { d -> d.startOriginal }
            }
            for (change in changes) {
                if (includeEquals && startOriginal < change.startOriginal) {
                    patch.addDelta(
                        EqualDelta(
                            buildChunk(startOriginal, change.startOriginal, original),
                            buildChunk(startRevised, change.startRevised, revised)
                        )
                    )
                }
                val orgChunk = buildChunk(change.startOriginal, change.endOriginal, original)
                val revChunk = buildChunk(change.startRevised, change.endRevised, revised)
                when (change.deltaType) {
                    DeltaType.DELETE -> patch.addDelta(DeleteDelta(orgChunk, revChunk))
                    DeltaType.INSERT -> patch.addDelta(InsertDelta(orgChunk, revChunk))
                    DeltaType.CHANGE -> patch.addDelta(ChangeDelta(orgChunk, revChunk))
                    else -> {}
                }
                startOriginal = change.endOriginal
                startRevised = change.endRevised
            }
            if (includeEquals && startOriginal < original.size) {
                patch.addDelta(
                    EqualDelta(
                        buildChunk(startOriginal, original.size, original),
                        buildChunk(startRevised, revised.size, revised)
                    )
                )
            }
            return patch
        }
    }

    init {
        deltas = ArrayList(estimatedPatchSize)
    }
}