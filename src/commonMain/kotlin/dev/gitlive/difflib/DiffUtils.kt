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
package dev.gitlive.difflib

import dev.gitlive.difflib.algorithm.DiffAlgorithmFactory
import dev.gitlive.difflib.algorithm.DiffAlgorithmI
import dev.gitlive.difflib.algorithm.DiffAlgorithmListener
import dev.gitlive.difflib.algorithm.myers.MeyersDiff
import dev.gitlive.difflib.patch.Patch
import dev.gitlive.difflib.patch.PatchFailedException

internal typealias Predicate<T> = (T) -> Boolean
internal typealias BiPredicate<T,R> = (T, R) -> Boolean
internal typealias Consumer<T> = (T) -> Unit
internal typealias Function<T,R> = (T) -> R
internal typealias BiFunction<T, T2,R> = (T, T2) -> R

/**
 * Implements the difference and patching engine
 */
object DiffUtils {
    /**
     * This factory generates the DEFAULT_DIFF algorithm for all these routines.
     */
    var DEFAULT_DIFF: DiffAlgorithmFactory = MeyersDiff.Companion.factory()
    @kotlin.jvm.JvmStatic
    fun withDefaultDiffAlgorithmFactory(factory: DiffAlgorithmFactory) {
        DEFAULT_DIFF = factory
    }

    /**
     * Computes the difference between the original and revised list of elements
     * with default diff algorithm
     *
     * @param <T> types to be diffed
     * @param original The original text. Must not be `null`.
     * @param revised The revised text. Must not be `null`.
     * @param progress progress listener
     * @return The patch describing the difference between the original and
     * revised sequences. Never `null`.
    </T> */
    fun <T> diff(original: List<T>, revised: List<T>, progress: DiffAlgorithmListener?): Patch<T> {
        return diff<T>(original, revised, DEFAULT_DIFF.create(), progress)
    }

    fun <T> diff(original: List<T>, revised: List<T>): Patch<T> {
        return diff<T>(original, revised, DEFAULT_DIFF.create(), null)
    }

    fun <T> diff(original: List<T>, revised: List<T>, includeEqualParts: Boolean): Patch<T> {
        return diff<T>(original, revised, DEFAULT_DIFF.create(), null, includeEqualParts)
    }

    /**
     * Computes the difference between the original and revised text.
     */
    fun diff(
        sourceText: String, targetText: String,
        progress: DiffAlgorithmListener?
    ): Patch<String> {
        return diff(sourceText.trimEnd('\n').lines(), targetText.trimEnd('\n').lines(), progress)
    }

    /**
     * Computes the difference between the original and revised list of elements
     * with default diff algorithm
     *
     * @param source The original text. Must not be `null`.
     * @param target The revised text. Must not be `null`.
     *
     * @param equalizer the equalizer object to replace the default compare
     * algorithm (Object.equals). If `null` the default equalizer of the
     * default algorithm is used..
     * @return The patch describing the difference between the original and
     * revised sequences. Never `null`.
     */
    fun <T> diff(
        source: List<T>, target: List<T>,
        equalizer: BiPredicate<T, T>?
    ): Patch<T> {
        return if (equalizer != null) {
            diff(
                source, target,
                DEFAULT_DIFF.create(equalizer)
            )
        } else diff(source, target, MeyersDiff())
    }

    fun <T> diff(
        original: List<T>, revised: List<T>,
        algorithm: DiffAlgorithmI<T>, progress: DiffAlgorithmListener?
    ): Patch<T> {
        return diff(original, revised, algorithm, progress, false)
    }

    /**
     * Computes the difference between the original and revised list of elements
     * with default diff algorithm
     *
     * @param original The original text. Must not be `null`.
     * @param revised The revised text. Must not be `null`.
     * @param algorithm The diff algorithm. Must not be `null`.
     * @param progress The diff algorithm listener.
     * @param includeEqualParts Include equal data parts into the patch.
     * @return The patch describing the difference between the original and
     * revised sequences. Never `null`.
     */
    fun <T> diff(
        original: List<T>, revised: List<T>,
        algorithm: DiffAlgorithmI<T>, progress: DiffAlgorithmListener?,
        includeEqualParts: Boolean
    ): Patch<T> {
        return Patch.Companion.generate<T>(
            original,
            revised,
            algorithm.computeDiff(original, revised, progress),
            includeEqualParts
        )
    }

    /**
     * Computes the difference between the original and revised list of elements
     * with default diff algorithm
     *
     * @param original The original text. Must not be `null`.
     * @param revised The revised text. Must not be `null`.
     * @param algorithm The diff algorithm. Must not be `null`.
     * @return The patch describing the difference between the original and
     * revised sequences. Never `null`.
     */
    @kotlin.jvm.JvmStatic
    fun <T> diff(original: List<T>, revised: List<T>, algorithm: DiffAlgorithmI<T>): Patch<T> {
        return diff(original, revised, algorithm, null)
    }

    /**
     * Computes the difference between the given texts inline. This one uses the
     * "trick" to make out of texts lists of characters, like DiffRowGenerator
     * does and merges those changes at the end together again.
     *
     * @param original
     * @param revised
     * @return
     */
    @kotlin.jvm.JvmStatic
    fun diffInline(original: String, revised: String): Patch<String> {
        val origList: MutableList<String> = ArrayList()
        val revList: MutableList<String> = ArrayList()
        for (character in original) {
            origList.add(character.toString())
        }
        for (character in revised) {
            revList.add(character.toString())
        }
        val patch: Patch<String> = diff(origList, revList)
        for (delta in patch.getDeltas()) {
            delta.source.lines = compressLines(delta.source.lines, "")
            delta.target.lines = compressLines(delta.target.lines, "")
        }
        return patch
    }

    private fun compressLines(lines: List<String>, delimiter: String): List<String> {
        return if (lines.isEmpty()) {
            emptyList<String>()
        } else listOf<String>(lines.joinToString(delimiter))
    }

    /**
     * Patch the original text with given patch
     *
     * @param original the original text
     * @param patch the given patch
     * @return the revised text
     * @throws PatchFailedException if can't apply patch
     */
    @kotlin.jvm.JvmStatic
//    @Throws(PatchFailedException::class)
    fun <T> patch(original: List<T>, patch: Patch<T>): List<T> {
        return patch.applyTo(original)
    }

    /**
     * Unpatch the revised text for a given patch
     *
     * @param revised the revised text
     * @param patch the given patch
     * @return the original text
     */
    fun <T> unpatch(revised: List<T>, patch: Patch<T>): List<T> {
        return patch.restore(revised)
    }
}