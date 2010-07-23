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
 * The general interface for computing diffs between two texts
 * 
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 */
public interface DiffAlgorithm {
    
    /**
     * Computes the difference between the original sequence and the revised
     * sequence and returns it as a {@link difflib.Patch Patch} object.
     * 
     * @param original the original text
     * @param revised the revised text
     * @return the patch
     */
    public Patch diff(Object[] original, Object[] revised);
    
    /**
     * Computes the difference between the original sequence and the revised
     * sequence and returns it as a {@link difflib.Patch Patch} object.
     * 
     * @param original the original text
     * @param revised the revised text
     * @return the patch
     */
    public Patch diff(List<?> original, List<?> revised);
}
