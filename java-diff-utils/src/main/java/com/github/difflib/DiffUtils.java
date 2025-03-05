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
package com.github.difflib;

import com.github.difflib.algorithm.DiffAlgorithmFactory;
import com.github.difflib.algorithm.DiffAlgorithmI;
import com.github.difflib.algorithm.DiffAlgorithmListener;
import com.github.difflib.algorithm.myers.MyersDiff;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Utility class to implement the difference and patching engine.
 */
public final class DiffUtils {

    /**
     * This factory generates the DEFAULT_DIFF algorithm for all these routines.
     */
    static DiffAlgorithmFactory DEFAULT_DIFF = MyersDiff.factory();

    /**
     * Sets the default diff algorithm factory to be used by all diff routines.
     *
     * @param factory a {@link DiffAlgorithmFactory} representing the new default diff algorithm factory.
     */
    public static void withDefaultDiffAlgorithmFactory(DiffAlgorithmFactory factory) {
        DEFAULT_DIFF = factory;
    }

    /**
     * Computes the difference between two sequences of elements using the default diff algorithm.
     *
     * @param <T> a generic representing the type of the elements to be compared.
     * @param original a {@link List} representing the original sequence of elements. Must not be {@code null}.
     * @param revised a {@link List} representing the revised sequence of elements. Must not be {@code null}.
     * @param progress a {@link DiffAlgorithmListener} representing the progress listener. Can be {@code null}.
     * @return The patch describing the difference between the original and revised sequences. Never {@code null}.
     */
    public static <T> Patch<T> diff(List<T> original, List<T> revised, DiffAlgorithmListener progress) {
        return DiffUtils.diff(original, revised, DEFAULT_DIFF.create(), progress);
    }

    /**
     * Computes the difference between two sequences of elements using the default diff algorithm.
     *
     * @param <T> a generic representing the type of the elements to be compared.
     * @param original a {@link List} representing the original sequence of elements. Must not be {@code null}.
     * @param revised a {@link List} representing the revised sequence of elements. Must not be {@code null}.
     * @return The patch describing the difference between the original and revised sequences. Never {@code null}.
     */
    public static <T> Patch<T> diff(List<T> original, List<T> revised) {
        return DiffUtils.diff(original, revised, DEFAULT_DIFF.create(), null);
    }

    /**
     * Computes the difference between two sequences of elements using the default diff algorithm.
     *
     * @param <T> a generic representing the type of the elements to be compared.
     * @param original a {@link List} representing the original sequence of elements. Must not be {@code null}.
     * @param revised a {@link List} representing the revised sequence of elements. Must not be {@code null}.
     * @param includeEqualParts a {@link boolean} representing whether to include equal parts in the resulting patch.
     * @return The patch describing the difference between the original and revised sequences. Never {@code null}.
     */
    public static <T> Patch<T> diff(List<T> original, List<T> revised, boolean includeEqualParts) {
        return DiffUtils.diff(original, revised, DEFAULT_DIFF.create(), null, includeEqualParts);
    }

    /**
     * Computes the difference between two strings using the default diff algorithm.
     *
     * @param sourceText a {@link String} representing the original string. Must not be {@code null}.
     * @param targetText a {@link String} representing the revised string. Must not be {@code null}.
     * @param progress a {@link DiffAlgorithmListener} representing the progress listener. Can be {@code null}.
     * @return The patch describing the difference between the original and revised strings. Never {@code null}.
     */
    public static Patch<String> diff(String sourceText, String targetText,
                                     DiffAlgorithmListener progress) {
        return DiffUtils.diff(
                Arrays.asList(sourceText.split("\n")),
                Arrays.asList(targetText.split("\n")), progress);
    }

    /**
     * Computes the difference between the original and revised list of elements
     * with default diff algorithm
     *
     * @param source a {@link List} representing the original text. Must not be {@code null}.
     * @param target a {@link List} representing the revised text. Must not be {@code null}.
     * @param equalizer a {@link BiPredicate} representing the equalizer object to replace the default compare
     * algorithm (Object.equals). If {@code null} the default equalizer of the
     * default algorithm is used.
     * @return The patch describing the difference between the original and
     * revised sequences. Never {@code null}.
     */
    public static <T> Patch<T> diff(List<T> source, List<T> target,
                                    BiPredicate<T, T> equalizer) {
        if (equalizer != null) {
            return DiffUtils.diff(source, target,
                    DEFAULT_DIFF.create(equalizer));
        }
        return DiffUtils.diff(source, target, new MyersDiff<>());
    }

    public static <T> Patch<T> diff(List<T> original, List<T> revised,
                                    DiffAlgorithmI<T> algorithm, DiffAlgorithmListener progress) {
        return diff(original, revised, algorithm, progress, false);
    }

    /**
     * Computes the difference between the original and revised list of elements
     * with default diff algorithm
     *
     * @param original a {@link List} representing the original text. Must not be {@code null}.
     * @param revised a {@link List} representing the revised text. Must not be {@code null}.
     * @param algorithm a {@link DiffAlgorithmI} representing the diff algorithm. Must not be {@code null}.
     * @param progress a {@link DiffAlgorithmListener} representing the diff algorithm listener.
     * @param includeEqualParts Include equal data parts into the patch.
     * @return The patch describing the difference between the original and
     * revised sequences. Never {@code null}.
     */
    public static <T> Patch<T> diff(List<T> original, List<T> revised,
                                    DiffAlgorithmI<T> algorithm, DiffAlgorithmListener progress,
                                    boolean includeEqualParts) {
        Objects.requireNonNull(original, "original must not be null");
        Objects.requireNonNull(revised, "revised must not be null");
        Objects.requireNonNull(algorithm, "algorithm must not be null");

        return Patch.generate(original, revised, algorithm.computeDiff(original, revised, progress), includeEqualParts);
    }


    /**
     * Computes the difference between the original and revised list of elements
     * with default diff algorithm
     *
     * @param original a {@link List} representing the original text. Must not be {@code null}.
     * @param revised a {@link List} representing the revised text. Must not be {@code null}.
     * @param algorithm a {@link DiffAlgorithmI} representing the diff algorithm. Must not be {@code null}.
     * @return The patch describing the difference between the original and
     * revised sequences. Never {@code null}.
     */
    public static <T> Patch<T> diff(List<T> original, List<T> revised, DiffAlgorithmI<T> algorithm) {
        return diff(original, revised, algorithm, null);
    }

    /**
     * Computes the difference between the given texts inline. This one uses the
     * "trick" to make out of texts lists of characters, like DiffRowGenerator
     * does and merges those changes at the end together again.
     *
     * @param original a {@link String} representing the original text. Must not be {@code null}.
     * @param revised a {@link String} representing the revised text. Must not be {@code null}.
     * @return The patch describing the difference between the original and
     * revised sequences. Never {@code null}.
     */
    public static Patch<String> diffInline(String original, String revised) {
        List<String> origList = new ArrayList<>();
        List<String> revList = new ArrayList<>();
        for (Character character : original.toCharArray()) {
            origList.add(character.toString());
        }
        for (Character character : revised.toCharArray()) {
            revList.add(character.toString());
        }
        Patch<String> patch = DiffUtils.diff(origList, revList);
        for (AbstractDelta<String> delta : patch.getDeltas()) {
            delta.getSource().setLines(compressLines(delta.getSource().getLines(), ""));
            delta.getTarget().setLines(compressLines(delta.getTarget().getLines(), ""));
        }
        return patch;
    }

    /**
     * Applies the given patch to the original list and returns the revised list.
     *
     * @param original a {@link List} representing the original list.
     * @param patch a {@link List} representing the patch to apply.
     * @return the revised list.
     * @throws PatchFailedException if the patch cannot be applied.
     */
    public static <T> List<T> patch(List<T> original, Patch<T> patch)
            throws PatchFailedException {
        return patch.applyTo(original);
    }

    /**
     * Applies the given patch to the revised list and returns the original list.
     *
     * @param revised a {@link List} representing the revised list.
     * @param patch a {@link Patch} representing the patch to apply.
     * @return the original list.
     * @throws PatchFailedException if the patch cannot be applied.
     */
    public static <T> List<T> unpatch(List<T> revised, Patch<T> patch) {
        return patch.restore(revised);
    }

    private static List<String> compressLines(List<String> lines, String delimiter) {
        if (lines.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(String.join(delimiter, lines));
    }

    private DiffUtils() {
    }
}
