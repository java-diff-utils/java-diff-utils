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
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import static java.util.stream.Collectors.toList;
import java.util.zip.ZipFile;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author toben
 */
public class LRHistogramDiffTest {

    @Test
    public void testPossibleDiffHangOnLargeDatasetDnaumenkoIssue26() throws IOException, PatchFailedException {
        ZipFile zip = new ZipFile("target/test-classes/mocks/large_dataset1.zip");
        List<String> original = readStringListFromInputStream(zip.getInputStream(zip.getEntry("ta")));
        List<String> revised = readStringListFromInputStream(zip.getInputStream(zip.getEntry("tb")));

        List<String> logdata = new ArrayList<>();
        Patch<String> patch = Patch.generate(original, revised, new HistogramDiff<String>().computeDiff(original, revised, new DiffAlgorithmListener() {
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

        assertEquals(34, patch.getDeltas().size());

        List<String> created = patch.applyTo(original);
        assertArrayEquals(revised.toArray(), created.toArray());
        
        assertEquals(246579, logdata.size());
    }

    public static List<String> readStringListFromInputStream(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName(StandardCharsets.UTF_8.name())))) {

            return reader.lines().collect(toList());
        }
    }
}
