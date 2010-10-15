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
