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
