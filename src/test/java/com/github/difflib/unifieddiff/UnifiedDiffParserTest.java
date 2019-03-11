/*
 * Copyright 2019 java-diff-utils.
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
package com.github.difflib.unifieddiff;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.assertj.core.api.Java6Assertions.assertThat;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
public class UnifiedDiffParserTest {

    public UnifiedDiffParserTest() {
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
    public void testSimpleParse() throws IOException {
        UnifiedDiff diff = UnifiedDiffParser.parseUnifiedDiff(UnifiedDiffParserTest.class.getResourceAsStream("jsqlparser_patch_1.diff"));

        System.out.println(diff);

        assertThat(diff.getFiles().size()).isEqualTo(2);

        UnifiedDiffFile file1 = diff.getFiles().get(0);
        assertThat(file1.getFromFile()).isEqualTo("src/main/jjtree/net/sf/jsqlparser/parser/JSqlParserCC.jjt");
        assertThat(file1.getPatch().getDeltas().size()).isEqualTo(3);

        assertThat(diff.getTail()).isEqualTo("2.17.1.windows.2\n\n");
    }

    @Test
    public void testParseDiffBlock() {
        String[] files = UnifiedDiffParser.parseFileNames("diff --git a/src/test/java/net/sf/jsqlparser/statement/select/SelectTest.java b/src/test/java/net/sf/jsqlparser/statement/select/SelectTest.java");
        assertThat(files).containsExactly("src/test/java/net/sf/jsqlparser/statement/select/SelectTest.java", "src/test/java/net/sf/jsqlparser/statement/select/SelectTest.java");
    }

    @Test
    public void testChunkHeaderParsing() {
        Pattern pattern = UnifiedDiffParser.UNIFIED_DIFF_CHUNK_REGEXP;
        Matcher matcher = pattern.matcher("@@ -189,6 +189,7 @@ TOKEN: /* SQL Keywords. prefixed with K_ to avoid name clashes */");

        assertTrue(matcher.find());
        assertEquals("189", matcher.group(1));
        assertEquals("189", matcher.group(3));
    }

    @Test
    public void testChunkHeaderParsing2() {
        //"^@@\\s+-(?:(\\d+)(?:,(\\d+))?)\\s+\\+(?:(\\d+)(?:,(\\d+))?)\\s+@@.*$"
        Pattern pattern = UnifiedDiffParser.UNIFIED_DIFF_CHUNK_REGEXP;
        Matcher matcher = pattern.matcher("@@ -189,6 +189,7 @@");

        assertTrue(matcher.find());
        assertEquals("189", matcher.group(1));
        assertEquals("189", matcher.group(3));
    }

}
