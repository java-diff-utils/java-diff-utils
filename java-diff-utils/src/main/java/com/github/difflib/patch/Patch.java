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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * Describes the patch holding all deltas between the original and revised
 * texts.
 *
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 * @param <T> The type of the compared elements in the 'lines'.
 */
public final class Patch<T> implements Serializable {

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
            VerifyChunk valid = delta.verifyAntApplyTo(result);
            if (valid != VerifyChunk.OK) {
                conflictOutput.processConflict(valid, delta, result);
            }
        }
        return result;
    }

    /**
     * Standard Patch behaviour to throw an exception for pathching conflicts.
     */
    public final ConflictOutput<T> CONFLICT_PRODUCES_EXCEPTION = (VerifyChunk verifyChunk, AbstractDelta<T> delta, List<T> result) -> {
        throw new PatchFailedException("could not apply patch due to " + verifyChunk.toString());
    };

    /**
     * Git like merge conflict output.
     */
    public static final ConflictOutput<String> CONFLICT_PRODUCES_MERGE_CONFLICT = (VerifyChunk verifyChunk, AbstractDelta<String> delta, List<String> result) -> {
        if (result.size() > delta.getSource().getPosition()) {
            List<String> orgData = new ArrayList<>();

            for (int i = 0; i < delta.getSource().size(); i++) {
                orgData.add(result.get(delta.getSource().getPosition()));
                result.remove(delta.getSource().getPosition());
            }

            orgData.add(0, "<<<<<< HEAD");
            orgData.add("======");
            orgData.addAll(delta.getSource().getLines());
            orgData.add(">>>>>>> PATCH");

            result.addAll(delta.getSource().getPosition(), orgData);

        } else {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };

    private ConflictOutput<T> conflictOutput = CONFLICT_PRODUCES_EXCEPTION;

    /**
     * Alter normal conflict output behaviour to e.g. inclide some conflict
     * statements in the result, like git does it.
     */
    public Patch withConflictOutput(ConflictOutput<T> conflictOutput) {
        this.conflictOutput = conflictOutput;
        return this;
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
        return generate(original, revised, changes, false);
    }

    private static <T> Chunk<T> buildChunk(int start, int end, List<T> data) {
        return new Chunk<>(start, new ArrayList<>(data.subList(start, end)));
    }

    public static <T> Patch<T> generate(List<T> original, List<T> revised, List<Change> _changes, boolean includeEquals) {
        Patch<T> patch = new Patch<>(_changes.size());
        int startOriginal = 0;
        int startRevised = 0;

        List<Change> changes = _changes;

        if (includeEquals) {
            changes = new ArrayList<Change>(_changes);
            Collections.sort(changes, comparing(d -> d.startOriginal));
        }

        for (Change change : changes) {

            if (includeEquals && startOriginal < change.startOriginal) {
                patch.addDelta(new EqualDelta<T>(
                        buildChunk(startOriginal, change.startOriginal, original),
                        buildChunk(startRevised, change.startRevised, revised)));
            }

            Chunk<T> orgChunk = buildChunk(change.startOriginal, change.endOriginal, original);
            Chunk<T> revChunk = buildChunk(change.startRevised, change.endRevised, revised);
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
                default:
            }

            startOriginal = change.endOriginal;
            startRevised = change.endRevised;
        }

        if (includeEquals && startOriginal < original.size()) {
            patch.addDelta(new EqualDelta<T>(
                    buildChunk(startOriginal, original.size(), original),
                    buildChunk(startRevised, revised.size(), revised)));
        }

        return patch;
    }
}
