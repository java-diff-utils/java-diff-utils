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

import java.util.List;

/**
 * Describes the delete-delta between original and revised texts.
 * 
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 */
public class DeleteDelta extends Delta {
    
    /**
     * {@inheritDoc}
     */
    public DeleteDelta(Chunk original, Chunk revised) {
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
        int size = getOriginal().getSize();
        for (int i = 0; i < size; i++) {
            target.remove(position);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void restore(List<Object> target) {
        int position = this.getRevised().getPosition();
        List<?> lines = this.getOriginal().getLines();
        for (int i = 0; i < lines.size(); i++) {
            target.add(position + i, lines.get(i));
        }
    }
    
    @Override
    public void verify(List<?> target) throws PatchFailedException {
        getOriginal().verify(target);
    }
    
    @Override
    public String toString() {
        return "[DeleteDelta, position: " + getOriginal().getPosition() + ", lines: "
                + getOriginal().getLines() + "]";
    }
}
