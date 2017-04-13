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
package difflib.algorithm.myers;

import difflib.algorithm.DiffAlgorithm;
import difflib.algorithm.DiffException;
import difflib.patch.ChangeDelta;
import difflib.patch.Chunk;
import difflib.patch.DeleteDelta;
import difflib.patch.Delta;
import difflib.patch.Equalizer;
import difflib.patch.InsertDelta;
import difflib.patch.Patch;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A clean-room implementation of <a href="http://www.cs.arizona.edu/people/gene/">
 * Eugene Myers</a> differencing algorithm.
 *
 * <p>
 * See the paper at <a href="http://www.cs.arizona.edu/people/gene/PAPERS/diff.ps">
 * http://www.cs.arizona.edu/people/gene/PAPERS/diff.ps</a></p>
 *
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 * @param T The type of the compared elements in the 'lines'.
 */
public final class MyersDiff<T> implements DiffAlgorithm<T> {

    /**
     * Default equalizer.
     */
    private final Equalizer<T> DEFAULT_EQUALIZER = Object::equals;

    /**
     * The equalizer.
     */
    private final Equalizer<T> equalizer;

    /**
     * Constructs an instance of the Myers differencing algorithm.
     */
    public MyersDiff() {
        equalizer = DEFAULT_EQUALIZER;
    }

    /**
     * Constructs an instance of the Myers differencing algorithm.
     *
     * @param equalizer Must not be {@code null}.
     */
    public MyersDiff(final Equalizer<T> equalizer) {
        Objects.requireNonNull(equalizer, "equalizer must not be null");
        this.equalizer = equalizer;
    }

    /**
     * {@inheritDoc}
     *
     * Return empty diff if get the error while procession the difference.
     */
    @Override
    public Patch<T> diff(final List<T> original, final List<T> revised) throws DiffException {
        Objects.requireNonNull(original, "original list must not be null");
        Objects.requireNonNull(revised, "revised list must not be null");

        PathNode path = buildPath(original, revised);
        return buildRevision(path, original, revised);
    }

    /**
     * Computes the minimum diffpath that expresses de differences between the original and revised
     * sequences, according to Gene Myers differencing algorithm.
     *
     * @param orig The original sequence.
     * @param rev The revised sequence.
     * @return A minimum {@link PathNode Path} accross the differences graph.
     * @throws DifferentiationFailedException if a diff path could not be found.
     */
    private PathNode buildPath(final List<T> orig, final List<T> rev)
            throws DifferentiationFailedException {
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
            for (int k = -d; k <= d; k += 2) {
                final int kmiddle = middle + k;
                final int kplus = kmiddle + 1;
                final int kminus = kmiddle - 1;
                PathNode prev = null;

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

                while (i < N && j < M && equalizer.equals(orig.get(i), rev.get(j))) {
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
        throw new DifferentiationFailedException("could not find a diff path");
    }

    /**
     * Constructs a {@link Patch} from a difference path.
     *
     * @param path The path.
     * @param orig The original sequence.
     * @param rev The revised sequence.
     * @return A {@link Patch} script corresponding to the path.
     * @throws DifferentiationFailedException if a {@link Patch} could not be built from the given
     * path.
     */
    private Patch<T> buildRevision(PathNode actualPath, List<T> orig, List<T> rev) {
        Objects.requireNonNull(actualPath, "path is null");
        Objects.requireNonNull(orig, "original sequence is null");
        Objects.requireNonNull(rev, "revised sequence is null");

        PathNode path = actualPath;
        Patch<T> patch = new Patch<>();
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

            Chunk<T> original = new Chunk<>(ianchor, copyOfRange(orig, ianchor, i));
            Chunk<T> revised = new Chunk<>(janchor, copyOfRange(rev, janchor, j));
            Delta<T> delta = null;
            if (original.size() == 0 && revised.size() != 0) {
                delta = new InsertDelta<>(original, revised);
            } else if (original.size() > 0 && revised.size() == 0) {
                delta = new DeleteDelta<>(original, revised);
            } else {
                delta = new ChangeDelta<>(original, revised);
            }

            patch.addDelta(delta);
            if (path.isSnake()) {
                path = path.prev;
            }
        }
        return patch;
    }

    /**
     * Creates a new list containing the elements returned by {@link List#subList(int, int)}.
     *
     * @param original The original sequence. Must not be {@code null}.
     * @param fromIndex low endpoint (inclusive) of the subList.
     * @param to high endpoint (exclusive) of the subList.
     * @return A new list of the specified range within the original list.
     *
     */
    private List<T> copyOfRange(final List<T> original, final int fromIndex, final int to) {
        return new ArrayList<>(original.subList(fromIndex, to));
    }
}
