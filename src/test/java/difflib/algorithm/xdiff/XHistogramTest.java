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
package difflib.algorithm.xdiff;

import difflib.DiffUtils;
import static difflib.DiffUtilsTest.readStringListFromInputStream;
import difflib.TestConstants;
import difflib.algorithm.DiffException;
import difflib.algorithm.myers.MyersDiff;
import difflib.patch.Patch;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipFile;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author tw
 */
public class XHistogramTest {
    
    public XHistogramTest() {
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
     * Test of diff method, of class XHistogram.
     */
    @Test
    public void testDiff() throws Exception {
        final Patch<String> patch = new XHistogram<String>().diff(
                Arrays.asList("A","B","C","A","B","B","A"), 
                Arrays.asList("C","B","A","B","A","C"));
        assertNotNull(patch);
        assertEquals(4, patch.getDeltas().size());
        assertEquals("Patch{deltas=[[DeleteDelta, position: 0, lines: [A, B]], [InsertDelta, position: 3, lines: [B]], [DeleteDelta, position: 5, lines: [B]], [InsertDelta, position: 7, lines: [C]]]}", patch.toString());
    }
    
    @Test
    public void testPossibleDiffHangOnLargeDatasetDnaumenkoIssue26() throws IOException, DiffException {
        ZipFile zip = new ZipFile(TestConstants.MOCK_FOLDER + "/large_dataset1.zip");
        
        Patch<String> patch = new XHistogram<String>().diff(
                readStringListFromInputStream(zip.getInputStream(zip.getEntry("ta"))), 
                readStringListFromInputStream(zip.getInputStream(zip.getEntry("tb"))));
        
        System.out.println("done");
    }
}
