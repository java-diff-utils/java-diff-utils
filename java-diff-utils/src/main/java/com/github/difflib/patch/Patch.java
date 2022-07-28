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
     * Creates a new list, the patch is being applied to.
     *
     * @param target The list to apply the changes to.
     * @return A new list containing the applied patch.
     * @throws PatchFailedException if the patch cannot be applied
     */
    public List<T> applyTo(List<T> target) throws PatchFailedException {
        List<T> result = new ArrayList<>(target);
        applyToExisting(result);
        return result;
    }

    /**
     * Applies the patch to the supplied list.
     *
     * @param target The list to apply the changes to. This list has to be modifiable,
     *               otherwise exceptions may be thrown, depending on the used type of list.
     * @throws PatchFailedException if the patch cannot be applied
     * @throws RuntimeException (or similar) if the list is not modifiable.
     */
    public void applyToExisting(List<T> target) throws PatchFailedException {
        ListIterator<AbstractDelta<T>> it = getDeltas().listIterator(deltas.size());
        while (it.hasPrevious()) {
            AbstractDelta<T> delta = it.previous();
            VerifyChunk valid = delta.verifyAndApplyTo(target);
            if (valid != VerifyChunk.OK) {
                conflictOutput.processConflict(valid, delta, target);
            }
        }
    }

    private static class PatchApplyingContext<T> {
        public final List<T> result;
        public final int maxFuzz;

        // the position last patch applied to.
        public int lastPatchEnd = -1;

        ///// passing values from find to apply
        public int currentFuzz = 0;

        public int defaultPosition;
        public boolean beforeOutRange = false;
        public boolean afterOutRange = false;

        private PatchApplyingContext(List<T> result, int maxFuzz) {
            this.result = result;
            this.maxFuzz = maxFuzz;
        }
    }

    public List<T> applyFuzzy(List<T> target, int maxFuzz) throws PatchFailedException {
        PatchApplyingContext<T> ctx = new PatchApplyingContext<>(new ArrayList<>(target), maxFuzz);

        // the difference between patch's position and actually applied position
        int lastPatchDelta = 0;

        for (AbstractDelta<T> delta : getDeltas()) {
            ctx.defaultPosition = delta.getSource().getPosition() + lastPatchDelta;
            int patchPosition = findPositionFuzzy(ctx, delta);
            if (0 <= patchPosition) {
                delta.applyFuzzyToAt(ctx.result, ctx.currentFuzz, patchPosition);
                lastPatchDelta = patchPosition - delta.getSource().getPosition();
                ctx.lastPatchEnd = delta.getSource().last() + lastPatchDelta;
            } else {
                conflictOutput.processConflict(VerifyChunk.CONTENT_DOES_NOT_MATCH_TARGET, delta, ctx.result);
            }
        }

        return ctx.result;
    }

    // negative for not found
    private int findPositionFuzzy(PatchApplyingContext<T> ctx, AbstractDelta<T> delta) throws PatchFailedException {
        for (int fuzz = 0; fuzz <= ctx.maxFuzz; fuzz++) {
            ctx.currentFuzz = fuzz;
            int foundPosition = findPositionWithFuzz(ctx, delta, fuzz);
            if (foundPosition >= 0) {
                return foundPosition;
            }
        }
        return -1;
    }

    // negative for not found
    private int findPositionWithFuzz(PatchApplyingContext<T> ctx, AbstractDelta<T> delta, int fuzz) throws PatchFailedException {
        if (delta.getSource().verifyChunk(ctx.result, fuzz, ctx.defaultPosition) == VerifyChunk.OK) {
            return ctx.defaultPosition;
        }

        ctx.beforeOutRange = false;
        ctx.afterOutRange = false;

        // moreDelta >= 0: just for overflow guard, not a normal condition
        //noinspection OverflowingLoopIndex
        for (int moreDelta = 0; moreDelta >= 0; moreDelta++) {
            int pos = findPositionWithFuzzAndMoreDelta(ctx, delta, fuzz, moreDelta);
            if (pos >= 0) {
                return pos;
            }
            if (ctx.beforeOutRange && ctx.afterOutRange) {
                break;
            }
        }

        return -1;
    }

    // negative for not found
    private int findPositionWithFuzzAndMoreDelta(PatchApplyingContext<T> ctx, AbstractDelta<T> delta, int fuzz, int moreDelta) throws PatchFailedException {
        // range check: can't apply before end of last patch
        if (!ctx.beforeOutRange) {
            int beginAt = ctx.defaultPosition - moreDelta + fuzz;
            // We can't apply patch before end of last patch.
            if (beginAt <= ctx.lastPatchEnd) {
                ctx.beforeOutRange = true;
            }
        }
        // range check: can't apply after end of result
        if (!ctx.afterOutRange) {
            int beginAt = ctx.defaultPosition + moreDelta + delta.getSource().size() - fuzz;
            // We can't apply patch before end of last patch.
            if (ctx.result.size() < beginAt) {
                ctx.afterOutRange = true;
            }
        }

        if (!ctx.beforeOutRange) {
            VerifyChunk before = delta.getSource().verifyChunk(ctx.result, fuzz, ctx.defaultPosition - moreDelta);
            if (before == VerifyChunk.OK) {
                return ctx.defaultPosition - moreDelta;
            }
        }
        if (!ctx.afterOutRange) {
            VerifyChunk after = delta.getSource().verifyChunk(ctx.result, fuzz, ctx.defaultPosition + moreDelta);
            if (after == VerifyChunk.OK) {
                return ctx.defaultPosition + moreDelta;
            }
        }
        return -1;
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
     * Creates a new list, containing the restored state of the given list.
     * Opposite to {@link #applyTo(List)} method.
     *
     * @param target The list to copy and apply changes to.
     * @return A new list, containing the restored state.
     */
    public List<T> restore(List<T> target) {
        List<T> result = new ArrayList<>(target);
        restoreToExisting(result);
        return result;
    }


    /**
     * Restores all changes within the given list.
     * Opposite to {@link #applyToExisting(List)} method.
     *
     * @param target The list to restore changes in. This list has to be modifiable,
     *               otherwise exceptions may be thrown, depending on the used type of list.
     * @throws RuntimeException (or similar) if the list is not modifiable.
     */
    public void restoreToExisting(List<T> target) {
        ListIterator<AbstractDelta<T>> it = getDeltas().listIterator(deltas.size());
        while (it.hasPrevious()) {
            AbstractDelta<T> delta = it.previous();
            delta.restore(target);
        }
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
