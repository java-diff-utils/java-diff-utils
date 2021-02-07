/*
 * Copyright 2020 java-diff-utils.
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
 * This delta contains equal lines of data. Therefore nothing is to do in applyTo and restore.
 * @author tobens
 */
public class EqualDelta<T> extends AbstractDelta<T> {

    public EqualDelta(Chunk<T> source, Chunk<T> target) {
        super(DeltaType.EQUAL, source, target);
    }

    @Override
    protected void applyTo(List<T> target) throws PatchFailedException {
    }

    @Override
    protected void restore(List<T> target) {
    }

    @Override
    public String toString() {
        return "[EqualDelta, position: " + getSource().getPosition() + ", lines: "
                + getSource().getLines() + "]";
    }
    
    @Override
    public AbstractDelta<T> withChunks(Chunk<T> original, Chunk<T> revised) {
        return new EqualDelta<T>(original, revised);
    }
}
