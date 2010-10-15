/*
   Copyright 2010 Dmitry Naumenko (dm.naumenko@gmail.com)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package difflib;

import java.util.*;

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
 */
public class Chunk {
    private int position;
    private int size;
    private List<?> lines;
    
    /**
     * Creates a chunk and saves a copy of affected lines
     * 
     * @param position
     *            the start position
     * @param size
     *            the size of a Chunk
     * @param lines
     *            the affected lines
     */
    public Chunk(int position, int size, List<?> lines) {
        this.position = position;
        this.size = size;
        this.lines = lines;
    }
    
    /**
     * Creates a chunk and saves a copy of affected lines
     * 
     * @param position
     *            the start position
     * @param size
     *            the size of a Chunk
     * @param lines
     *            the affected lines
     */
    public Chunk(int position, int size, Object[] lines) {
        this.position = position;
        this.size = size;
        this.lines = Arrays.asList(lines);
    }
    
    /**
     * Verifies that this chunk's saved text matches the corresponding text in
     * the given sequence.
     * 
     * @param target
     *            the sequence to verify against.
     */
    public void verify(List<?> target) throws PatchFailedException {
        if (last() > target.size()) {
            throw new PatchFailedException("Incorrect Chunk: the position of chunk > target size");
        }
        for (int i = 0; i < size; i++) {
            if (!target.get(position + i).equals(lines.get(i))) {
                throw new PatchFailedException(
                        "Incorrect Chunk: the chunk content doesn't match the target");
            }
        }
    }
    
    /**
     * @return the start position of chunk in the text
     */
    public int getPosition() {
        return position;
    }
    
    /**
     * @param position
     *            the start position to set
     */
    public void setPosition(int position) {
        this.position = position;
    }
    
    /**
     * @return the size of Chunk (size of affected lines)
     */
    public int getSize() {
        return size;
    }
    
    /**
     * @param size
     *            the size of affected lines to set
     */
    public void setSize(int size) {
        this.size = size;
    }
    
    /**
     * @return the affected lines
     */
    public List<?> getLines() {
        return lines;
    }
    
    /**
     * @param lines
     *            the affected lines to set
     */
    public void setLines(List<?> lines) {
        this.lines = lines;
    }
    
    /**
     * Returns the index of the last line of the chunk.
     */
    public int last() {
        return getPosition() + getSize() - 1;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((lines == null) ? 0 : lines.hashCode());
        result = prime * result + position;
        result = prime * result + size;
        return result;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Chunk other = (Chunk) obj;
        if (lines == null) {
            if (other.lines != null)
                return false;
        } else if (!lines.equals(other.lines))
            return false;
        if (position != other.position)
            return false;
        if (size != other.size)
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "[position: " + position + ", size: " + size + ", lines: " + lines + "]";
    }
    
}
