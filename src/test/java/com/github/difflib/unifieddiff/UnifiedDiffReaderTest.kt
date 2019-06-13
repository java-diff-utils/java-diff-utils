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
package com.github.difflib.unifieddiff

import com.github.difflib.patch.AbstractDelta
import java.io.IOException
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 *
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
class UnifiedDiffReaderTest {

    @Test
    @Throws(IOException::class)
    fun testSimpleParse() {
        val diff = UnifiedDiffReader.parseUnifiedDiff(UnifiedDiffReaderTest::class.java!!.getResourceAsStream("jsqlparser_patch_1.diff"))

        println(diff)

        assertThat(diff.files.size).isEqualTo(2)

        val file1 = diff.files[0]
        assertThat(file1.fromFile).isEqualTo("src/main/jjtree/net/sf/jsqlparser/parser/JSqlParserCC.jjt")
        assertThat(file1.patch.deltas.size).isEqualTo(3)

        assertThat(diff.tail).isEqualTo("2.17.1.windows.2\n\n")
    }

    @Test
    fun testParseDiffBlock() {
        val files = UnifiedDiffReader.parseFileNames("diff --git a/src/test/java/net/sf/jsqlparser/statement/select/SelectTest.java b/src/test/java/net/sf/jsqlparser/statement/select/SelectTest.java")
        assertThat(files).containsExactly("src/test/java/net/sf/jsqlparser/statement/select/SelectTest.java", "src/test/java/net/sf/jsqlparser/statement/select/SelectTest.java")
    }

    @Test
    fun testChunkHeaderParsing() {
        val pattern = UnifiedDiffReader.UNIFIED_DIFF_CHUNK_REGEXP
        val matcher = pattern.matcher("@@ -189,6 +189,7 @@ TOKEN: /* SQL Keywords. prefixed with K_ to avoid name clashes */")

        assertTrue(matcher.find())
        assertEquals("189", matcher.group(1))
        assertEquals("189", matcher.group(3))
    }

    @Test
    fun testChunkHeaderParsing2() {
        //"^@@\\s+-(?:(\\d+)(?:,(\\d+))?)\\s+\\+(?:(\\d+)(?:,(\\d+))?)\\s+@@.*$"
        val pattern = UnifiedDiffReader.UNIFIED_DIFF_CHUNK_REGEXP
        val matcher = pattern.matcher("@@ -189,6 +189,7 @@")

        assertTrue(matcher.find())
        assertEquals("189", matcher.group(1))
        assertEquals("189", matcher.group(3))
    }

    @Test
    fun testChunkHeaderParsing3() {
        //"^@@\\s+-(?:(\\d+)(?:,(\\d+))?)\\s+\\+(?:(\\d+)(?:,(\\d+))?)\\s+@@.*$"
        val pattern = UnifiedDiffReader.UNIFIED_DIFF_CHUNK_REGEXP
        val matcher = pattern.matcher("@@ -1,27 +1,27 @@")

        assertTrue(matcher.find())
        assertEquals("1", matcher.group(1))
        assertEquals("1", matcher.group(3))
    }

    @Test
    @Throws(IOException::class)
    fun testSimpleParse2() {
        val diff = UnifiedDiffReader.parseUnifiedDiff(UnifiedDiffReaderTest::class.java!!.getResourceAsStream("jsqlparser_patch_1.diff"))

        println(diff)

        assertThat(diff.files.size).isEqualTo(2)

        val file1 = diff.files[0]
        assertThat(file1.fromFile).isEqualTo("src/main/jjtree/net/sf/jsqlparser/parser/JSqlParserCC.jjt")
        assertThat(file1.patch.deltas.size).isEqualTo(3)

        val first = file1.patch.deltas[0]

        assertThat(first.source.size()).isGreaterThan(0)
        assertThat(first.target.size()).isGreaterThan(0)

        assertThat(diff.tail).isEqualTo("2.17.1.windows.2\n\n")
    }

    @Test
    fun testSimplePattern() {
        val pattern = Pattern.compile("^\\+\\+\\+\\s")

        val m = pattern.matcher("+++ revised.txt")
        assertTrue(m.find())
    }

}
