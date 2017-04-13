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

import java.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tw
 */
public class XPrepareTest {
    
    public XPrepareTest() {
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
     * Test of prepareEnvironment method, of class XPrepare.
     */
    @Test
    public void testPrepareEnvironment() {
        XDFEnv<String> env = new XDFEnv<>();
        XPrepare.prepareEnvironment(
                Arrays.asList("A","B","C","A","B","B","A"), 
                Arrays.asList("C","B","A","B","A","C"),
                new XPParam(),
                env);
        System.out.println(Arrays.toString(env.xdf1.ha));
        System.out.println(Arrays.toString(env.xdf2.ha));
    }
    
}
