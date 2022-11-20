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
package com.github.difflib.algorithm.myers;

import com.github.difflib.algorithm.DiffAlgorithmListener;
import com.github.difflib.patch.Patch;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

/**
 *
 * @author tw
 */
public class MyersDiffTest {

    @Test
    public void testDiffMyersExample1Forward() {
        List<String> original = Arrays.asList("A", "B", "C", "A", "B", "B", "A");
        List<String> revised = Arrays.asList("C", "B", "A", "B", "A", "C");
        final Patch<String> patch = Patch.generate(original, revised, new MyersDiff<String>().computeDiff(original, revised, null));
        assertNotNull(patch);
        assertEquals(4, patch.getDeltas().size());
        assertEquals("Patch{deltas=[[DeleteDelta, position: 0, lines: [A, B]], [InsertDelta, position: 3, lines: [B]], [DeleteDelta, position: 5, lines: [B]], [InsertDelta, position: 7, lines: [C]]]}", patch.toString());
    }
    
    @Test
    public void testDiffMyersExample1ForwardWithListener() {
        List<String> original = Arrays.asList("A", "B", "C", "A", "B", "B", "A");
        List<String> revised = Arrays.asList("C", "B", "A", "B", "A", "C");
        
        List<String> logdata = new ArrayList<>();
        final Patch<String> patch = Patch.generate(original, revised, 
                new MyersDiff<String>().computeDiff(original, revised, new DiffAlgorithmListener() {
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
        assertNotNull(patch);
        assertEquals(4, patch.getDeltas().size());
        assertEquals("Patch{deltas=[[DeleteDelta, position: 0, lines: [A, B]], [InsertDelta, position: 3, lines: [B]], [DeleteDelta, position: 5, lines: [B]], [InsertDelta, position: 7, lines: [C]]]}", patch.toString());
        System.out.println(logdata);
        assertEquals(8, logdata.size());
    }
}
