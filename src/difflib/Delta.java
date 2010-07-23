/*
    Copyright 2009 Dmitry Naumenko (dm.naumenko@gmail.com)
    
    This file is part of Java Diff Utills Library.

    Java Diff Utills Library is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Java Diff Utills Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Java Diff Utills Library.  If not, see <http://www.gnu.org/licenses/>.
 */
package difflib;

import java.util.*;

/**
 * Describes the delta between original and revised texts.
 * 
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 */
public abstract class Delta {
    private Chunk original;
    private Chunk revised;
    
    /**
     * Construct the delta for original and revised chunks
     * 
     * @param original chunk describes the original text
     * @param revised chunk describes the revised text
     */
    public Delta(Chunk original, Chunk revised) {
        this.original = original;
        this.revised = revised;
    }
    
    /**
     * Verifies that this delta can be used to patch the given text.
     * 
     * @param target the text to patch.
     * @throws PatchFailedException if the patch cannot be applied.
     */
    public abstract void verify(List<?> target) throws PatchFailedException;
    
    /**
     * Applies this delta as the patch for a given target
     * 
     * @param target the given target
     * @throws PatchFailedException
     */
    public abstract void applyTo(List<Object> target) throws PatchFailedException;
    
    /**
     * Cancel this delta for a given revised text. The action is opposite to
     * patch.
     * 
     * @param target the given revised text
     */
    public abstract void restore(List<Object> target);
    
    /**
     * @return the Chunk describing the original text
     */
    public Chunk getOriginal() {
        return original;
    }
    
    /**
     * @param original the Chunk describing the original text to set
     */
    public void setOriginal(Chunk original) {
        this.original = original;
    }
    
    /**
     * @return the Chunk describing the revised text
     */
    public Chunk getRevised() {
        return revised;
    }
    
    /**
     * @param revised the Chunk describing the revised text to set
     */
    public void setRevised(Chunk revised) {
        this.revised = revised;
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
        result = prime * result + ((original == null) ? 0 : original.hashCode());
        result = prime * result + ((revised == null) ? 0 : revised.hashCode());
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
        Delta other = (Delta) obj;
        if (original == null) {
            if (other.original != null)
                return false;
        } else if (!original.equals(other.original))
            return false;
        if (revised == null) {
            if (other.revised != null)
                return false;
        } else if (!revised.equals(other.revised))
            return false;
        return true;
    }
    
}
