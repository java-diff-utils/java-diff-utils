package dev.gitlive.difflib.unifieddiff


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
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun runTest(test: suspend () -> Unit) = runBlocking { test() }

/**
 *
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
class UnifiedDiffReaderTest {
    
    @Test
    fun testSimpleParse() = runTest {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("jsqlparser_patch_1.diff")!!
        )
        assertEquals(2, diff.getFiles().size)
        val file1 = diff.getFiles()[0]
        assertEquals("src/main/jjtree/net/sf/jsqlparser/parser/JSqlParserCC.jjt", file1.fromFile)
        assertEquals(3, file1.patch.getDeltas().size)
        assertEquals("2.17.1.windows.2\n", diff.tail)
    }

    @Test
    fun testParseDiffBlock() {
        val files =
            UnifiedDiffReader.parseFileNames("diff --git a/src/test/java/net/sf/jsqlparser/statement/select/SelectTest.java b/src/test/java/net/sf/jsqlparser/statement/select/SelectTest.java")
        assertEquals(2, files.size)
        assertEquals(files[0], "src/test/java/net/sf/jsqlparser/statement/select/SelectTest.java")
        assertEquals(files[1], "src/test/java/net/sf/jsqlparser/statement/select/SelectTest.java")
    }

    @Test
    fun testChunkHeaderParsing() {
        val pattern: Regex = UnifiedDiffReader.UNIFIED_DIFF_CHUNK_REGEXP
        val matcher = pattern.find("@@ -189,6 +189,7 @@ TOKEN: /* SQL Keywords. prefixed with K_ to avoid name clashes */")!!
        assertTrue(pattern.containsMatchIn("@@ -189,6 +189,7 @@ TOKEN: /* SQL Keywords. prefixed with K_ to avoid name clashes */"))
        assertEquals("189", matcher.groups[1]!!.value)
        assertEquals("189", matcher.groups[3]!!.value)
    }

    @Test
    fun testChunkHeaderParsing2() {
        //"^@@\\s+-(?:(\\d+)(?:,(\\d+))?)\\s+\\+(?:(\\d+)(?:,(\\d+))?)\\s+@@.*$"
        val pattern: Regex = UnifiedDiffReader.UNIFIED_DIFF_CHUNK_REGEXP
        val matcher = pattern.find("@@ -189,6 +189,7 @@")!!
        assertTrue(pattern.containsMatchIn("@@ -189,6 +189,7 @@"))
        assertEquals("189", matcher.groups[1]!!.value)
        assertEquals("189", matcher.groups[3]!!.value)
    }

    @Test
    fun testChunkHeaderParsing3() {
        //"^@@\\s+-(?:(\\d+)(?:,(\\d+))?)\\s+\\+(?:(\\d+)(?:,(\\d+))?)\\s+@@.*$"
        val pattern: Regex = UnifiedDiffReader.UNIFIED_DIFF_CHUNK_REGEXP
        val matcher = pattern.find("@@ -1,27 +1,27 @@")!!
        assertTrue(pattern.containsMatchIn("@@ -1,27 +1,27 @@"))
        assertEquals("1", matcher.groups[1]!!.value)
        assertEquals("1", matcher.groups[3]!!.value)
    }

    @Test
    fun testSimpleParse2() = runTest {
        val diff =
            UnifiedDiffReader.parseUnifiedDiff(
                UnifiedDiffReaderTest::class.java.getResourceAsStream("jsqlparser_patch_1.diff")!!
            )
        assertEquals(2, diff.getFiles().size)
        val file1 = diff.getFiles()[0]
        assertEquals("src/main/jjtree/net/sf/jsqlparser/parser/JSqlParserCC.jjt", file1.fromFile)
        assertEquals(3, file1.patch.getDeltas().size)
        val first = file1.patch.getDeltas()[0]
        assertEquals(6, first.source.size())
        assertEquals(7, first.target.size())
        assertEquals("2.17.1.windows.2\n", diff.tail)
    }

    @Test
    fun testSimplePattern() {
        val pattern = """^\+\+\+\s""".toRegex()
        assertTrue(pattern.containsMatchIn("+++ revised.txt"))
    }

    @Test
    fun testParseIssue46() = runTest {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue46.diff")!!
        )
        assertEquals(1, diff.getFiles().size)
        val file1 = diff.getFiles()[0]
        assertEquals(".vhd", file1.fromFile)
        assertEquals(1, file1.patch.getDeltas().size)
        assertEquals(null, diff.tail)
    }

    @Test
    fun testParseIssue33() = runTest {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue33.diff")!!
        )
        assertEquals(1, diff.getFiles().size)
        val file1 = diff.getFiles()[0]
        assertEquals("Main.java", file1.fromFile)
        assertEquals(1, file1.patch.getDeltas().size)
        assertEquals(null, diff.tail)
        assertEquals(null, diff.header)
    }

    @Test
    fun testParseIssue51() = runTest {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue51.diff")!!
        )
        assertEquals(2, diff.getFiles().size)
        val file1 = diff.getFiles()[0]
        assertEquals("f1", file1.fromFile)
        assertEquals(1, file1.patch.getDeltas().size)
        val file2 = diff.getFiles()[1]
        assertEquals("f2", file2.fromFile)
        assertEquals(1, file2.patch.getDeltas().size)
        assertEquals(null, diff.tail)
    }

    @Test
    fun testParseIssue79() = runTest {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue79.diff")!!
        )
        assertEquals(1, diff.getFiles().size)
        val file1 = diff.getFiles()[0]
        assertEquals("test/Issue.java", file1.fromFile)
        assertEquals(0, file1.patch.getDeltas().size)
        assertEquals(null, diff.tail)
        assertEquals(null, diff.header)
    }

    @Test
    fun testParseIssue84() = runTest {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue84.diff")!!
        )
        assertEquals(2, diff.getFiles().size)
        val file1 = diff.getFiles()[0]
        assertEquals("config/ant-phase-verify.xml", file1.fromFile)
        assertEquals(1, file1.patch.getDeltas().size)
        val file2 = diff.getFiles()[1]
        assertEquals("/dev/null", file2.fromFile)
        assertEquals(1, file2.patch.getDeltas().size)
        assertEquals("2.7.4", diff.tail)
        assertTrue(diff.header!!.startsWith("From b53e612a2ab5ff15d14860e252f84c0f343fe93a Mon Sep 17 00:00:00 2001"))
    }

    @Test
    fun testParseIssue85() = runTest {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue85.diff")!!
        )
        assertEquals(1, diff.getFiles().size)
        val file1 = diff.getFiles()[0]
        assertEquals(
            "diff -r 83e41b73d115 -r a4438263b228 tests/test-check-pyflakes.t",
            file1.diffCommand
        )
        assertEquals("tests/test-check-pyflakes.t", file1.fromFile)
        assertEquals("tests/test-check-pyflakes.t", file1.toFile)
        assertEquals(1, file1.patch.getDeltas().size)
        assertEquals(null, diff.tail)
    }

    @Test
    fun testTimeStampRegexp() {
        assertTrue(UnifiedDiffReader.TIMESTAMP_REGEXP.containsMatchIn("2019-04-18 13:49:39.516149751 +0200"))
    }

    @Test
    fun testParseIssue98() = runTest {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue98.diff")!!
        )
        assertEquals(1, diff.getFiles().size)
        val file1 = diff.getFiles()[0]
        assertEquals(
            "100644",
            file1.deletedFileMode
        )
        assertEquals("src/test/java/se/bjurr/violations/lib/model/ViolationTest.java", file1.fromFile)
        assertEquals("2.25.1", diff.tail)
    }

    @Test
    fun testParseIssue104() = runTest {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_parsing_issue104.diff")!!
        )
        assertEquals(6, diff.getFiles().size)
        val file = diff.getFiles()[2]
        assertEquals("/dev/null", file.fromFile)
        assertEquals("doc/samba_data_tool_path.xml.in", file.toFile)
        assertEquals("Patch{deltas=[[ChangeDelta, position: 0, lines: [] to [@SAMBA_DATA_TOOL@]]]}", file.patch.toString())
        assertEquals("2.14.4", diff.tail)
    }

    @Test
    fun testParseIssue107BazelDiff() = runTest {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("01-bazel-strip-unused.patch_issue107.diff")!!
        )
        assertEquals(450, diff.getFiles().size)
        val file = diff.getFiles()[0]
        assertEquals("./src/main/java/com/amazonaws/AbortedException.java", file.fromFile)
        assertEquals("/home/greg/projects/bazel/third_party/aws-sdk-auth-lite/src/main/java/com/amazonaws/AbortedException.java", file.toFile)
        assertEquals(48, diff.getFiles().count { it.isNoNewLineAtTheEndOfTheFile })
    }

    @Test
    fun testParseIssue107_2() = runTest {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReader::class.java.getResourceAsStream("problem_diff_issue107.diff")!!
        )
        assertEquals(2, diff.getFiles().size)
        val file1 = diff.getFiles()[0]
        assertEquals("Main.java", file1.fromFile)
        assertEquals(1, file1.patch.getDeltas().size)
    }

    @Test
    fun testParseIssue107_3() = runTest {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue107_3.diff")!!
        )
        assertEquals(1, diff.getFiles().size)
        val file1 = diff.getFiles()[0]
        assertEquals("Billion laughs attack.md", file1.fromFile)
        assertEquals(1, file1.patch.getDeltas().size)
    }

    @Test
    fun testParseIssue107_4() = runTest {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue107_4.diff")!!
        )
        assertEquals(27, diff.getFiles().size)
        assertTrue(diff.getFiles().map { it.fromFile }.contains("README.md"))
    }

    @Test
    fun testParseIssue107_5() = runTest {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue107_5.diff")!!
        )
        assertEquals(22, diff.getFiles().size)
        assertTrue(diff.getFiles().map { it.fromFile }.contains("rt/management/src/test/java/org/apache/cxf/management/jmx/MBServerConnectorFactoryTest.java"))
    }

    @Test
    fun testParseIssue110() = runTest {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("0001-avahi-python-Use-the-agnostic-DBM-interface.patch")!!
        )
        assertEquals(5, diff.getFiles().size)
        val file = diff.getFiles()[4]
        assertEquals(87, file.similarityIndex)
        assertEquals("service-type-database/build-db.in", file.renameFrom)
        assertEquals("service-type-database/build-db", file.renameTo)
        assertEquals("service-type-database/build-db.in", file.fromFile)
        assertEquals("service-type-database/build-db", file.toFile)
    }

    @Test
    fun testParseIssue117() = runTest {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue117.diff")!!
        )
        assertEquals(2, diff.getFiles().size)
        assertEquals(2, diff.getFiles()[0].patch.getDeltas().size)
        
        assertEquals(24, diff.getFiles()[0].patch.getDeltas()[0].source.changePosition!![0])
        assertEquals(27, diff.getFiles()[0].patch.getDeltas()[0].source.changePosition!![1])

        assertEquals(24, diff.getFiles()[0].patch.getDeltas()[0].target.changePosition!![0])
        assertEquals(27, diff.getFiles()[0].patch.getDeltas()[0].target.changePosition!![1])

        assertEquals(64, diff.getFiles()[0].patch.getDeltas()[1].source.changePosition!![0])
        assertEquals(64, diff.getFiles()[0].patch.getDeltas()[1].target.changePosition!![0])
        assertEquals(65, diff.getFiles()[0].patch.getDeltas()[1].target.changePosition!![1])
        assertEquals(66, diff.getFiles()[0].patch.getDeltas()[1].target.changePosition!![2])
        assertEquals(67, diff.getFiles()[0].patch.getDeltas()[1].target.changePosition!![3])
        assertEquals(68, diff.getFiles()[0].patch.getDeltas()[1].target.changePosition!![4])
        assertEquals(69, diff.getFiles()[0].patch.getDeltas()[1].target.changePosition!![5])
        assertEquals(70, diff.getFiles()[0].patch.getDeltas()[1].target.changePosition!![6])
        assertEquals(71, diff.getFiles()[0].patch.getDeltas()[1].target.changePosition!![7])
        assertEquals(72, diff.getFiles()[0].patch.getDeltas()[1].target.changePosition!![8])
        assertEquals(73, diff.getFiles()[0].patch.getDeltas()[1].target.changePosition!![9])
        assertEquals(74, diff.getFiles()[0].patch.getDeltas()[1].target.changePosition!![10])
    }

    @Test
    fun testParseIssue122() = runTest {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue122.diff")!!
        )
        assertEquals(1, diff.getFiles().size)
        assertTrue(diff.getFiles().map { it.fromFile }.contains("coders/wpg.c"))
    }

    @Test
    fun testParseIssue123() = runTest {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue123.diff")!!
        )
        assertEquals(2, diff.getFiles().size)
        assertTrue(diff.getFiles().map { f: UnifiedDiffFile -> f.fromFile }.contains("src/java/main/org/apache/zookeeper/server/FinalRequestProcessor.java"))
    }

    @Test
    fun testAddingNewLine() = runTest {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("new_line_added.diff")!!
        )
        assertEquals(1, diff.getFiles().size)
        val file = diff.getFiles()[0]
        assertEquals(null, file.renameFrom)
        assertEquals(null, file.renameTo)
        assertEquals("settings.gradle", file.fromFile)
        assertEquals("settings.gradle", file.toFile)
        val deltas = file.patch.getDeltas()
        assertEquals(1, deltas.size)
        val delta = deltas[0]
        assertEquals(1, delta.source.lines.size)
        assertEquals(2, delta.target.lines.size)
        assertEquals("rootProject.name = \"sample-repo\"", delta.source.lines[0])
        assertEquals("rootProject.name = \"sample-repo\"", delta.target.lines[0])
        assertEquals("", delta.target.lines[1])
    }

    @Test
    fun testRemovingNewLine() = runTest {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("new_line_removed.diff")!!
        )
        assertEquals(1, diff.getFiles().size)
        val file = diff.getFiles()[0]
        assertEquals(null, file.renameFrom)
        assertEquals(null, file.renameTo)
        assertEquals("settings.gradle", file.fromFile)
        assertEquals("settings.gradle", file.toFile)
        val deltas = file.patch.getDeltas()
        assertEquals(1, deltas.size)
        val delta = deltas[0]
        assertEquals(2, delta.source.lines.size)
        assertEquals(1, delta.target.lines.size)
        assertEquals("rootProject.name = \"sample-repo\"", delta.source.lines[0])
        assertEquals("", delta.source.lines[1])
        assertEquals("rootProject.name = \"sample-repo\"", delta.target.lines[0])
    }
    
    @Test
    fun fullTest() = runTest {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("full_test.diff")!!
        )
        assertEquals(1, diff.getFiles().size)
        val file = diff.getFiles()[0]
        assertEquals(null, file.renameFrom)
        assertEquals(null, file.renameTo)
        assertEquals("README.txt", file.fromFile)
        assertEquals("README.txt", file.toFile)
        val deltas = file.patch.getDeltas()
        assertEquals(1, deltas.size)
        val delta = deltas[0]
        assertEquals(13, delta.source.lines.size)
        assertEquals(14, delta.target.lines.size)

        val beforeContent = "    // staged      unstaged  result      filename\n" +
                "    // =========   ==========  ==========  ====================\n" +
                "    // unmodified  modified    modified    UnmodModMod.kt\n" +
                "    // unmodified  added       added       UnmodAddAdd.kt\n" +
                "    // unmodified  deleted     deleted     UnmodDelDel.kt\n" +
                "    // deleted     unmodified  deleted     DelUnmodDel.kt\n" +
                "    // modified    unmodified  modified    ModUnmodMod.kt\n" +
                "    // added       unmodified  added       AddUnmodAdd.kt\n" +
                "    // modified    modified    modified    ModModMod.kt\n" +
                "    // added       modified    added       AddModAdd.kt\n" +
                "    // deleted     added       modified    DelAddMod.kt\n" +
                "    // modified    deleted     deleted     ModDelDel.kt\n" +
                "    // added       deleted     unmodified  AddDelUnmod.kt"
        val afterContent = "    // staged      unstaged  result      filename\n" +
                "    // =========   ==========  ==========  ====================\n" +
                "    // =========   ==========  ==========  ====================\n" +
                "    // =========   ==========  ==========  ====================\n" +
                "    // unmodified  modified    modified    UnmodModMod.kt\n" +
                "    // unmodified  added       added       UnmodAddAdd.kt\n" +
                "    // modified    unmodified  modified    ModUnmodMod.kt\n" +
                "    // added       unmodified  added       AddUnmodAdd.kt\n" +
                "    // modified    modified    modified    ModModMod.kt\n" +
                "    // modified    modified    modified    ModModMod.kt\n" +
                "    // deleted     added       modified    DelAddMod.kt\n" +
                "    // deleted     added       modified    DelAddMod.kt\n" +
                "    // modified    deleted     deleted     ModDelDel.kt\n" +
                "    // added       deleted     unmodified  AddDelUnmod.kt"
        val unifiedAfterContent = file.patch.applyTo(beforeContent.split("\n")).joinToString("\n")

        assertEquals(afterContent, unifiedAfterContent)
    }
}