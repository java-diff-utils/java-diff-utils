/*
 * Copyright 2018 java-diff-utils.
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

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Abstract delta between a source and a target. 
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
public abstract class AbstractDelta<T> implements Serializable {
    private final Chunk<T> source;
    private final Chunk<T> target;
    private final DeltaType type;
    
    public AbstractDelta(DeltaType type, Chunk<T> source, Chunk<T> target) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);
        Objects.requireNonNull(type);
        this.type = type;
        this.source = source;
        this.target = target;
    }

    public Chunk<T> getSource() {
        return source;
    }

    public Chunk<T> getTarget() {
        return target;
    }

    public DeltaType getType() {
        return type;
    }
    
    /**
     * Verify the chunk of this delta, to fit the target.
     * @param target
     * @throws PatchFailedException 
     */
    protected VerifyChunk verifyChunkToFitTarget(List<T> target) throws PatchFailedException {
        return getSource().verifyChunk(target);
    }
   
    protected VerifyChunk verifyAntApplyTo(List<T> target) throws PatchFailedException {
        final VerifyChunk verify = verifyChunkToFitTarget(target);
        if (verify == VerifyChunk.OK) {
            applyTo(target);
        }
        return verify;
    }
    
    protected abstract void applyTo(List<T> target) throws PatchFailedException;
    
    protected abstract void restore(List<T> target);
    
    /**
     * Create a new delta of the actual instance with customized chunk data.
     */
    public abstract AbstractDelta<T> withChunks(Chunk<T> original, Chunk<T> revised);

    @Override
    public int hashCode() {
        return Objects.hash(this.source, this.target, this.type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractDelta<?> other = (AbstractDelta<?>) obj;
        if (!Objects.equals(this.source, other.source)) {
            return false;
        }
        if (!Objects.equals(this.target, other.target)) {
            return false;
        }
        return this.type == other.type;
    }
}
