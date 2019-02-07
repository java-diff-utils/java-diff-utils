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

import com.github.difflib.algorithm.DiffAlgorithmI;
import com.github.difflib.algorithm.DiffAlgorithmListener;
import com.github.difflib.algorithm.DiffException;
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
 * Implements the difference and patching engine
 *
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 */
public final class DiffUtils {

    /**
     * Computes the difference between the original and revised list of elements with default diff algorithm
     *
     * @param original The original text. Must not be {@code null}.
     * @param revised The revised text. Must not be {@code null}.
     * @param progress progress listener
     * @return The patch describing the difference between the original and revised sequences. Never {@code null}.
     * @throws com.github.difflib.algorithm.DiffException
     */
    public static <T> Patch<T> diff(List<T> original, List<T> revised, DiffAlgorithmListener progress) throws DiffException {
        return DiffUtils.diff(original, revised, new MyersDiff<>(), progress);
    }
    
    public static <T> Patch<T> diff(List<T> original, List<T> revised) throws DiffException {
        return DiffUtils.diff(original, revised, new MyersDiff<>(), null);
    }

    /**
     * Computes the difference between the original and revised text.
     */
    public static Patch<String> diff(String sourceText, String targetText,
            DiffAlgorithmListener progress) throws DiffException {
        return DiffUtils.diff(
                 Arrays.asList(sourceText.split("\n")), 
                 Arrays.asList(targetText.split("\n")), progress);
    }

    /**
     * Computes the difference between the original and revised list of elements with default diff algorithm
     *
     * @param source The original text. Must not be {@code null}.
     * @param target The revised text. Must not be {@code null}.
     *
     * @param equalizer the equalizer object to replace the default compare algorithm (Object.equals). If {@code null}
     * the default equalizer of the default algorithm is used..
     * @return The patch describing the difference between the original and revised sequences. Never {@code null}.
     */
    public static <T> Patch<T> diff(List<T> source, List<T> target,
            BiPredicate<T, T> equalizer) throws DiffException {
        if (equalizer != null) {
            return DiffUtils.diff(source, target,
                    new MyersDiff<>(equalizer));
        }
        return DiffUtils.diff(source, target, new MyersDiff<>());
    }

    /**
     * Computes the difference between the original and revised list of elements with default diff algorithm
     *
     * @param original The original text. Must not be {@code null}.
     * @param revised The revised text. Must not be {@code null}.
     * @param algorithm The diff algorithm. Must not be {@code null}.
     * @param progress The diff algorithm listener.
     * @return The patch describing the difference between the original and revised sequences. Never {@code null}.
     */
    public static <T> Patch<T> diff(List<T> original, List<T> revised,
            DiffAlgorithmI<T> algorithm, DiffAlgorithmListener progress) throws DiffException {
        Objects.requireNonNull(original, "original must not be null");
        Objects.requireNonNull(revised, "revised must not be null");
        Objects.requireNonNull(algorithm, "algorithm must not be null");

        return Patch.generate(original, revised, algorithm.computeDiff(original, revised, progress));
    }
    
    /**
     * Computes the difference between the original and revised list of elements with default diff algorithm
     *
     * @param original The original text. Must not be {@code null}.
     * @param revised The revised text. Must not be {@code null}.
     * @param algorithm The diff algorithm. Must not be {@code null}.
     * @return The patch describing the difference between the original and revised sequences. Never {@code null}.
     */
     public static <T> Patch<T> diff(List<T> original, List<T> revised,
            DiffAlgorithmI<T> algorithm) throws DiffException {
         return diff(original, revised, algorithm, null);
     }

    /**
     * Computes the difference between the given texts inline. This one uses the "trick" to make out of texts lists of
     * characters, like DiffRowGenerator does and merges those changes at the end together again.
     *
     * @param original
     * @param revised
     * @return
     */
    public static Patch<String> diffInline(String original, String revised) throws DiffException {
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

    private static List<String> compressLines(List<String> lines, String delimiter) {
        if (lines.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(String.join(delimiter, lines));
    }

    /**
     * Patch the original text with given patch
     *
     * @param original the original text
     * @param patch the given patch
     * @return the revised text
     * @throws PatchFailedException if can't apply patch
     */
    public static <T> List<T> patch(List<T> original, Patch<T> patch)
            throws PatchFailedException {
        return patch.applyTo(original);
    }

    /**
     * Unpatch the revised text for a given patch
     *
     * @param revised the revised text
     * @param patch the given patch
     * @return the original text
     */
    public static <T> List<T> unpatch(List<T> revised, Patch<T> patch) {
        return patch.restore(revised);
    }

    private DiffUtils() {
    }
}
