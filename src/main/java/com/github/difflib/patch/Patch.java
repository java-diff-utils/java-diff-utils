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
package com.github.difflib.patch;

import static java.util.Comparator.comparing;
import com.github.difflib.algorithm.Change;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Describes the patch holding all deltas between the original and revised texts.
 *
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 * @param <T> The type of the compared elements in the 'lines'.
 */
public final class Patch<T> {

    private final List<AbstractDelta<T>> deltas;

    public Patch() {
        this(10);
    }

    public Patch(int estimatedPatchSize) {
        deltas = new ArrayList<>(estimatedPatchSize);
    }

    /**
     * Apply this patch to the given target
     *
     * @return the patched text
     * @throws PatchFailedException if can't apply patch
     */
    public List<T> applyTo(List<T> target) throws PatchFailedException {
        List<T> result = new ArrayList<>(target);
        ListIterator<AbstractDelta<T>> it = getDeltas().listIterator(deltas.size());
        while (it.hasPrevious()) {
            AbstractDelta<T> delta = it.previous();
            delta.applyTo(result);
        }
        return result;
    }

    /**
     * Restore the text to original. Opposite to applyTo() method.
     *
     * @param target the given target
     * @return the restored text
     */
    public List<T> restore(List<T> target) {
        List<T> result = new ArrayList<>(target);
        ListIterator<AbstractDelta<T>> it = getDeltas().listIterator(deltas.size());
        while (it.hasPrevious()) {
            AbstractDelta<T> delta = it.previous();
            delta.restore(result);
        }
        return result;
    }

    /**
     * Add the given delta to this patch
     *
     * @param delta the given delta
     */
    public void addDelta(AbstractDelta<T> delta) {
        deltas.add(delta);
    }

    /**
     * Get the list of computed deltas
     *
     * @return the deltas
     */
    public List<AbstractDelta<T>> getDeltas() {
        deltas.sort(comparing(d -> d.getSource().getPosition()));
        return deltas;
    }

    @Override
    public String toString() {
        return "Patch{" + "deltas=" + deltas + '}';
    }

    public static <T> Patch<T> generate(List<T> original, List<T> revised, List<Change> changes) {
        Patch<T> patch = new Patch<>(changes.size());
        for (Change change : changes) {
            Chunk<T> orgChunk = new Chunk<>(change.startOriginal, new ArrayList<>(original.subList(change.startOriginal, change.endOriginal)));
            Chunk<T> revChunk = new Chunk<>(change.startRevised, new ArrayList<>(revised.subList(change.startRevised, change.endRevised)));
            switch (change.deltaType) {
                case DELETE:
                    patch.addDelta(new DeleteDelta<>(orgChunk, revChunk));
                    break;
                case INSERT:
                    patch.addDelta(new InsertDelta<>(orgChunk, revChunk));
                    break;
                case CHANGE:
                    patch.addDelta(new ChangeDelta<>(orgChunk, revChunk));
                    break;
            }
        }
        return patch;
    }
}
