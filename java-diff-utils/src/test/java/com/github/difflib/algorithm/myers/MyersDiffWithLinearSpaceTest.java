/*
 * Copyright 2021 java-diff-utils.
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

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffAlgorithmListener;
import com.github.difflib.patch.Patch;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static java.util.stream.Collectors.toList;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author tw
 */
public class MyersDiffWithLinearSpaceTest {
    
    @Test
    public void testDiffMyersExample1Forward() {
        List<String> original = Arrays.asList("A", "B", "C", "A", "B", "B", "A");
        List<String> revised = Arrays.asList("C", "B", "A", "B", "A", "C");
        final Patch<String> patch = Patch.generate(original, revised, new MyersDiffWithLinearSpace<String>().computeDiff(original, revised, null));
        assertNotNull(patch);
        System.out.println(patch);
        assertEquals(5, patch.getDeltas().size());
        assertEquals("Patch{deltas=[[InsertDelta, position: 0, lines: [C]], [DeleteDelta, position: 0, lines: [A]], [DeleteDelta, position: 2, lines: [C]], [DeleteDelta, position: 5, lines: [B]], [InsertDelta, position: 7, lines: [C]]]}", patch.toString());
    }
    
    @Test
    public void testDiffMyersExample1ForwardWithListener() {
        List<String> original = Arrays.asList("A", "B", "C", "A", "B", "B", "A");
        List<String> revised = Arrays.asList("C", "B", "A", "B", "A", "C");
        
        List<String> logdata = new ArrayList<>();
        final Patch<String> patch = Patch.generate(original, revised, 
                new MyersDiffWithLinearSpace<String>().computeDiff(original, revised, new DiffAlgorithmListener() {
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
        System.out.println(patch);
        assertEquals(5, patch.getDeltas().size());
        assertEquals("Patch{deltas=[[InsertDelta, position: 0, lines: [C]], [DeleteDelta, position: 0, lines: [A]], [DeleteDelta, position: 2, lines: [C]], [DeleteDelta, position: 5, lines: [B]], [InsertDelta, position: 7, lines: [C]]]}", patch.toString());
        System.out.println(logdata);
        assertEquals(11, logdata.size());
    }
    
    
    @Test
    public void testPerformanceProblemsIssue124() {
         List<String> old = Arrays.asList("abcd");
         List<String> newl = IntStream.range(0, 90000)
                    .boxed()
                    .map(i -> i.toString())
                    .collect(toList());
         
        long start = System.currentTimeMillis();
        Patch<String> diff = DiffUtils.diff(old, newl, new MyersDiffWithLinearSpace<String>());
        long end = System.currentTimeMillis();
        System.out.println("Finished in " + (end - start) + "ms and resulted " + diff.getDeltas().size() + " deltas");
    }
}
