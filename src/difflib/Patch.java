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
 * Describes the patch holding all deltas between the original and revised texts.
 * 
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 */
public class Patch {
    private List<Delta> deltas = new LinkedList<Delta>();
    private boolean isSorted = false;
    
    /**
     * Apply this patch to the given target
     * @param target
     * @return the patched text
     * @throws PatchFailedException if can't apply patch
     */
    public List<?> applyTo(List<?> target) throws PatchFailedException {
        List<Object> result = new LinkedList<Object>(target);
        ListIterator<Delta> it = getDeltas().listIterator(deltas.size());
        while (it.hasPrevious()) {
            Delta delta = (Delta) it.previous();
            delta.applyTo(result);
        }
        return result;
    }
    
    /**
     * Restore the text to original. Opposite to applyTo() method.
     * @param target the given target
     * @return the restored text
     */
    public List<?> restore(List<?> target) {
        List<Object> result = new LinkedList<Object>(target);
        ListIterator<Delta> it = getDeltas().listIterator(deltas.size());
        while (it.hasPrevious()) {
            Delta delta = (Delta) it.previous();
            delta.restore(result);
        }
        return result;
    }
    
    /**
     * Add the given delta to this patch
     * @param delta the given delta
     */
    public void addDelta(Delta delta) {
        deltas.add(delta);
        isSorted = false;
    }
    
    /**
     * @param deltas the deltas to set
     */
    public void setDeltas(List<Delta> deltas) {
        this.deltas = deltas;
        isSorted = false;
    }
    
    /**
     * Get the list of computed deltas
     * @return the deltas
     */
    public List<Delta> getDeltas() {
        if (!this.isSorted) {
            Collections.sort(deltas, new Comparator<Delta>() {
                public int compare(Delta d1, Delta d2) {
                    if (d1.getOriginal().getPosition() > d2.getOriginal().getPosition()) {
                        return 1;
                    } else if (d1.getOriginal().getPosition() > d2.getOriginal().getPosition()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
            isSorted = true;
        }
        return deltas;
    }
    
    /**
     * Get the specific delta from patch deltas
     * @param index the index of delta
     * @return the needed delta
     */
    public Delta getDelta(int index) {
        return deltas.get(index);
    }
}
