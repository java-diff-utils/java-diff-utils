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

/**
 * Describes the add-delta between original and revised texts.
 *
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 * @param <T> The type of the compared elements in the 'lines'.
 */
public final class InsertDelta<T> extends AbstractDelta<T> {

    /**
     * Creates an insert delta with the two given chunks.
     *
     * @param original The original chunk. Must not be {@code null}.
     * @param revised The original chunk. Must not be {@code null}.
     */
    public InsertDelta(Chunk<T> original, Chunk<T> revised) {
        super(DeltaType.INSERT, original, revised);
    }

    @Override
    protected void applyTo(List<T> target) throws PatchFailedException {
        int position = this.getSource().getPosition();
        List<T> lines = this.getTarget().getLines();
        for (int i = 0; i < lines.size(); i++) {
            target.add(position + i, lines.get(i));
        }
    }

    @Override
    protected void restore(List<T> target) {
        int position = getTarget().getPosition();
        int size = getTarget().size();
        for (int i = 0; i < size; i++) {
            target.remove(position);
        }
    }

    @Override
    public String toString() {
        return "[InsertDelta, position: " + getSource().getPosition()
                + ", lines: " + getTarget().getLines() + "]";
    }
    
    @Override
    public AbstractDelta<T> withChunks(Chunk<T> original, Chunk<T> revised) {
        return new InsertDelta<T>(original, revised);
    }
}
