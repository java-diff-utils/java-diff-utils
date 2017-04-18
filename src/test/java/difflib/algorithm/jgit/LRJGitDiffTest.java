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
package difflib.algorithm.jgit;

import static difflib.DiffUtilsTest.readStringListFromInputStream;
import difflib.TestConstants;
import difflib.algorithm.DiffException;
import difflib.patch.Patch;
import difflib.patch.PatchFailedException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipFile;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author toben
 */
public class LRJGitDiffTest {
    
    public LRJGitDiffTest() {
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

    
    @Test
    public void testPossibleDiffHangOnLargeDatasetDnaumenkoIssue26() throws IOException, DiffException, PatchFailedException {
        ZipFile zip = new ZipFile(TestConstants.MOCK_FOLDER + "/large_dataset1.zip");
        List<String> original = readStringListFromInputStream(zip.getInputStream(zip.getEntry("ta")));
        List<String> revised = readStringListFromInputStream(zip.getInputStream(zip.getEntry("tb")));
        
        Patch<String> patch = Patch.generate(original, revised, new JGitDiff().diff(original, revised));
        
        assertEquals(34, patch.getDeltas().size());
        
        List<String> created = patch.applyTo(original);
        assertArrayEquals(revised.toArray(), created.toArray());
    }
    
}
