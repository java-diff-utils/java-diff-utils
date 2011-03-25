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

import java.util.List;

/**
 * Describes the change-delta between original and revised texts.
 * 
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 */
public class ChangeDelta extends Delta {
    
    /**
     * {@inheritDoc}
     */
    public ChangeDelta(Chunk original, Chunk revised) {
        super(original, revised);
    }
    
    /**
     * {@inheritDoc}
     * 
     * @throws PatchFailedException
     */
    @Override
    public void applyTo(List<Object> target) throws PatchFailedException {
        verify(target);
        int position = getOriginal().getPosition();
        int size = getOriginal().size();
        for (int i = 0; i < size; i++) {
            target.remove(position);
        }
        int i = 0;
        for (Object line : getRevised().getLines()) {
            target.add(position + i, line);
            i++;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void restore(List<Object> target) {
        int position = getRevised().getPosition();
        int size = getRevised().size();
        for (int i = 0; i < size; i++) {
            target.remove(position);
        }
        int i = 0;
        for (Object line : getOriginal().getLines()) {
            target.add(position + i, line);
            i++;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void verify(List<?> target) throws PatchFailedException {
        getOriginal().verify(target);
        if (getOriginal().getPosition() > target.size()) {
            throw new PatchFailedException("Incorrect patch for delta: "
                    + "delta original position > target size");
        }
    }
    
    @Override
    public String toString() {
        return "[ChangeDelta, position: " + getOriginal().getPosition() + ", lines: "
                + getOriginal().getLines() + " to " + getRevised().getLines() + "]";
    }

    @Override
    public TYPE getType() {
        return Delta.TYPE.CHANGE;
    }
}
