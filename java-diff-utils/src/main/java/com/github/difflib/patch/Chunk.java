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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Holds the information about the part of text involved in the diff process
 *
 * <p>
 * Text is represented as <code>Object[]</code> because the diff engine is
 * capable of handling more than plain ascci. In fact, arrays or lists of any
 * type that implements {@link java.lang.Object#hashCode hashCode()} and
 * {@link java.lang.Object#equals equals()} correctly can be subject to
 * differencing using this library.
 * </p>
 *
 * @author <a href="dm.naumenko@gmail.com>Dmitry Naumenko</a>
 * @param <T> The type of the compared elements in the 'lines'.
 */
public final class Chunk<T> implements Serializable {

    private final int position;
    private List<T> lines;
    private final List<Integer> changePosition;

    /**
     * Creates a chunk and saves a copy of affected lines
     *
     * @param position the start position
     * @param lines the affected lines
     * @param changePosition the positions of changed lines
     */
    public Chunk(int position, List<T> lines, List<Integer> changePosition) {
        this.position = position;
        this.lines = new ArrayList<>(lines);
        this.changePosition = changePosition != null ? new ArrayList<>(changePosition) : null;
    }

    /**
     * Creates a chunk and saves a copy of affected lines
     *
     * @param position the start position
     * @param lines the affected lines
     */
    public Chunk(int position, List<T> lines) {
        this(position, lines, null);
    }

    /**
     * Creates a chunk and saves a copy of affected lines
     *
     * @param position the start position
     * @param lines the affected lines
     * @param changePosition the positions of changed lines
     */
    public Chunk(int position, T[] lines, List<Integer> changePosition) {
        this.position = position;
        this.lines = Arrays.asList(lines);
        this.changePosition = changePosition != null ? new ArrayList<>(changePosition) : null;
    }

    /**
     * Creates a chunk and saves a copy of affected lines
     *
     * @param position the start position
     * @param lines the affected lines
     */
    public Chunk(int position, T[] lines) {
        this(position, lines, null);
    }

    /**
     * Verifies that this chunk's saved text matches the corresponding text in
     * the given sequence.
     *
     * @param target the sequence to verify against.
     * @throws com.github.difflib.patch.PatchFailedException
     */
    public VerifyChunk verifyChunk(List<T> target) throws PatchFailedException {
        if (position > target.size() || last() > target.size()) {
            return VerifyChunk.POSITION_OUT_OF_TARGET;
        }
        for (int i = 0; i < size(); i++) {
            if (!target.get(position + i).equals(lines.get(i))) {
                return VerifyChunk.CONTENT_DOES_NOT_MATCH_TARGET;
            }
        }
        return VerifyChunk.OK;
    }

    /**
     * @return the start position of chunk in the text
     */
    public int getPosition() {
        return position;
    }

    public void setLines(List<T> lines) {
        this.lines = lines;
    }

    /**
     * @return the affected lines
     */
    public List<T> getLines() {
        return lines;
    }

    /**
     * @return the positions of changed lines of chunk in the text
     */
    public List<Integer> getChangePosition() {
        return changePosition;
    }

    public int size() {
        return lines.size();
    }

    /**
     * Returns the index of the last line of the chunk.
     */
    public int last() {
        return getPosition() + size() - 1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lines, position, size());
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
        Chunk<?> other = (Chunk<?>) obj;
        if (lines == null) {
            if (other.lines != null) {
                return false;
            }
        } else if (!lines.equals(other.lines)) {
            return false;
        }
        return position == other.position;
    }

    @Override
    public String toString() {
        return "[position: " + position + ", size: " + size() + ", lines: " + lines + "]";
    }

}
