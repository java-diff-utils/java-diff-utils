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
package com.github.difflib.algorithm.myers;

import com.github.difflib.algorithm.Change;
import com.github.difflib.algorithm.DiffAlgorithmFactory;
import com.github.difflib.algorithm.DiffAlgorithmI;
import com.github.difflib.algorithm.DiffAlgorithmListener;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * A clean-room implementation of Eugene Meyers greedy differencing algorithm.
 */
public final class MeyersDiff<T> implements DiffAlgorithmI<T> {

    private final BiPredicate<T, T> equalizer;

    public MeyersDiff() {
        equalizer = Object::equals;
    }

    public MeyersDiff(final BiPredicate<T, T> equalizer) {
        Objects.requireNonNull(equalizer, "equalizer must not be null");
        this.equalizer = equalizer;
    }

    /**
     * {@inheritDoc}
     *
     * Return empty diff if get the error while procession the difference.
     */
    @Override
    public List<Change> computeDiff(final List<T> source, final List<T> target, DiffAlgorithmListener progress) {
        Objects.requireNonNull(source, "source list must not be null");
        Objects.requireNonNull(target, "target list must not be null");

        if (progress != null) {
            progress.diffStart();
        }
        PathNode path = buildPath(source, target, progress);
        List<Change> result = buildRevision(path, source, target);
        if (progress != null) {
            progress.diffEnd();
        }
        return result;
    }

    /**
     * Computes the minimum diffpath that expresses de differences between the
     * original and revised sequences, according to Gene Myers differencing
     * algorithm.
     *
     * @param orig The original sequence.
     * @param rev The revised sequence.
     * @return A minimum {@link PathNode Path} accross the differences graph.
     * @throws DifferentiationFailedException if a diff path could not be found.
     */
    private PathNode buildPath(final List<T> orig, final List<T> rev, DiffAlgorithmListener progress) {
        Objects.requireNonNull(orig, "original sequence is null");
        Objects.requireNonNull(rev, "revised sequence is null");

        // these are local constants
        final int N = orig.size();
        final int M = rev.size();

        final int MAX = N + M + 1;
        final int size = 1 + 2 * MAX;
        final int middle = size / 2;
        final PathNode diagonal[] = new PathNode[size];

        diagonal[middle + 1] = new PathNode(0, -1, true, true, null);
        for (int d = 0; d < MAX; d++) {
            if (progress != null) {
                progress.diffStep(d, MAX);
            }
            for (int k = -d; k <= d; k += 2) {
                final int kmiddle = middle + k;
                final int kplus = kmiddle + 1;
                final int kminus = kmiddle - 1;
                PathNode prev;
                int i;

                if ((k == -d) || (k != d && diagonal[kminus].i < diagonal[kplus].i)) {
                    i = diagonal[kplus].i;
                    prev = diagonal[kplus];
                } else {
                    i = diagonal[kminus].i + 1;
                    prev = diagonal[kminus];
                }

                diagonal[kminus] = null; // no longer used

                int j = i - k;

                PathNode node = new PathNode(i, j, false, false, prev);

                while (i < N && j < M && equalizer.test(orig.get(i), rev.get(j))) {
                    i++;
                    j++;
                }

                if (i != node.i) {
                    node = new PathNode(i, j, true, false, node);
                }

                diagonal[kmiddle] = node;

                if (i >= N && j >= M) {
                    return diagonal[kmiddle];
                }
            }
            diagonal[middle + d - 1] = null;
        }
        // According to Myers, this cannot happen
        throw new IllegalStateException("could not find a diff path");
    }

    /**
     * Constructs a {@link Patch} from a difference path.
     *
     * @param actualPath The path.
     * @param orig The original sequence.
     * @param rev The revised sequence.
     * @return A {@link Patch} script corresponding to the path.
     * @throws DifferentiationFailedException if a {@link Patch} could not be
     * built from the given path.
     */
    private List<Change> buildRevision(PathNode actualPath, List<T> orig, List<T> rev) {
        Objects.requireNonNull(actualPath, "path is null");
        Objects.requireNonNull(orig, "original sequence is null");
        Objects.requireNonNull(rev, "revised sequence is null");

        PathNode path = actualPath;
        List<Change> changes = new ArrayList<>();
        if (path.isSnake()) {
            path = path.prev;
        }
        while (path != null && path.prev != null && path.prev.j >= 0) {
            if (path.isSnake()) {
                throw new IllegalStateException("bad diffpath: found snake when looking for diff");
            }
            int i = path.i;
            int j = path.j;

            path = path.prev;
            int ianchor = path.i;
            int janchor = path.j;

            if (ianchor == i && janchor != j) {
                changes.add(new Change(DeltaType.INSERT, ianchor, i, janchor, j));
            } else if (ianchor != i && janchor == j) {
                changes.add(new Change(DeltaType.DELETE, ianchor, i, janchor, j));
            } else {
                changes.add(new Change(DeltaType.CHANGE, ianchor, i, janchor, j));
            }

            if (path.isSnake()) {
                path = path.prev;
            }
        }
        return changes;
    }

    /**
     * Factory to create instances of this specific diff algorithm.
     */
    public static DiffAlgorithmFactory factory() {
        return new DiffAlgorithmFactory() {
            @Override
            public <T> DiffAlgorithmI<T> 
            create() {
                return new MeyersDiff<T>();
            }

            @Override
            public <T> DiffAlgorithmI<T> 
            create(BiPredicate < T, T > equalizer) {
                return new MeyersDiff<T>(equalizer);
            }
        };
    }
}
