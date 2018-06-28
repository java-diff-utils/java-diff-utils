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

import com.github.difflib.algorithm.DiffAlgorithmListener;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author toben
 */
public class HistogramDiffTest {

    public HistogramDiffTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of diff method, of class HistogramDiff.
     */
    @Test
    public void testDiff() throws DiffException, PatchFailedException {
        List<String> orgList = Arrays.asList("A", "B", "C", "A", "B", "B", "A");
        List<String> revList = Arrays.asList("C", "B", "A", "B", "A", "C");
        final Patch<String> patch = Patch.generate(orgList, revList, new HistogramDiff().computeDiff(orgList, revList, null));
        System.out.println(patch);
        assertNotNull(patch);
        assertEquals(3, patch.getDeltas().size());
        assertEquals("Patch{deltas=[[DeleteDelta, position: 0, lines: [A, B]], [DeleteDelta, position: 3, lines: [A, B]], [InsertDelta, position: 7, lines: [B, A, C]]]}", patch.toString());

        List<String> patched = patch.applyTo(orgList);
        assertEquals(revList, patched);
    }
    
    @Test
    public void testDiffWithListener() throws DiffException, PatchFailedException {
        List<String> orgList = Arrays.asList("A", "B", "C", "A", "B", "B", "A");
        List<String> revList = Arrays.asList("C", "B", "A", "B", "A", "C");
        
        List<String> logdata = new ArrayList<>();
        final Patch<String> patch = Patch.generate(orgList, revList, new HistogramDiff().computeDiff(orgList, revList, new DiffAlgorithmListener() {
            @Override
            public void diffStart() {
                logdata.add("start");
            }

            @Override
            public void diffStep(int value, int max) {
                logdata.add(value + " - " + max);
            }

            @Override
            public void diffEnd() {
                logdata.add("end");
            }
        }));
        System.out.println(patch);
        assertNotNull(patch);
        assertEquals(3, patch.getDeltas().size());
        assertEquals("Patch{deltas=[[DeleteDelta, position: 0, lines: [A, B]], [DeleteDelta, position: 3, lines: [A, B]], [InsertDelta, position: 7, lines: [B, A, C]]]}", patch.toString());

        List<String> patched = patch.applyTo(orgList);
        assertEquals(revList, patched);
        
        System.out.println(logdata);
        assertEquals(17, logdata.size());
    }
}
