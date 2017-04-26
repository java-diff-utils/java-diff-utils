/*
 * Copyright 2017 java-diff-utils.
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
package com.github.difflib.algorithm.jgit;

import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author toben
 */
public class JGitDiffTest {

    @Test
    public void testDiff() throws DiffException, PatchFailedException {
        List<String> orgList = Arrays.asList("A","B","C","A","B","B","A");
        List<String> revList = Arrays.asList("C","B","A","B","A","C");
        final Patch<String> patch = Patch.generate(orgList, revList, new JGitDiff().diff(orgList, revList));
        System.out.println(patch);
        assertNotNull(patch);
        assertEquals(3, patch.getDeltas().size());
        assertEquals("Patch{deltas=[[DeleteDelta, position: 0, lines: [A, B]], [DeleteDelta, position: 3, lines: [A, B]], [InsertDelta, position: 7, lines: [B, A, C]]]}", patch.toString());
        
        List<String> patched = patch.applyTo(orgList);
        assertEquals(revList, patched);
    }
}
