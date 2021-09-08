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
package com.github.difflib.patch;

import java.util.List;
import java.util.Objects;

/**
 * Describes the change-delta between original and revised texts.
 *
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 * @param <T> The type of the compared elements in the data 'lines'.
 */
public final class ChangeDelta<T> extends AbstractDelta<T> {

    /**
     * Creates a change delta with the two given chunks.
     *
     * @param source The source chunk. Must not be {@code null}.
     * @param target The target chunk. Must not be {@code null}.
     */
    public ChangeDelta(Chunk<T> source, Chunk<T> target) {
        super(DeltaType.CHANGE, source, target);
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(target, "target must not be null");
    }

    @Override
    protected void applyTo(List<T> target) throws PatchFailedException {
        int position = getSource().getPosition();
        int size = getSource().size();
        for (int i = 0; i < size; i++) {
            target.remove(position);
        }
        int i = 0;
        for (T line : getTarget().getLines()) {
            target.add(position + i, line);
            i++;
        }
    }

    @Override
    protected void restore(List<T> target) {
        int position = getTarget().getPosition();
        int size = getTarget().size();
        for (int i = 0; i < size; i++) {
            target.remove(position);
        }
        int i = 0;
        for (T line : getSource().getLines()) {
            target.add(position + i, line);
            i++;
        }
    }

    @Override
    public String toString() {
        return "[ChangeDelta, position: " + getSource().getPosition() + ", lines: "
                + getSource().getLines() + " to " + getTarget().getLines() + "]";
    }

    @Override
    public AbstractDelta<T> withChunks(Chunk<T> original, Chunk<T> revised) {
        return new ChangeDelta<T>(original, revised);
    }
}
