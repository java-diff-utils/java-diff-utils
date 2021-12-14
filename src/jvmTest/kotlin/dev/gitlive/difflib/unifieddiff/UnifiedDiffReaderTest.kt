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
import dev.gitlive.difflib.InputStream
import org.assertj.core.api.Assertions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.util.regex.Pattern


/**
 *
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
class UnifiedDiffReaderTest {
    @Test
    @Throws(IOException::class)
    fun testSimpleParse() {
        val diff =
            UnifiedDiffReader.parseUnifiedDiff(
                UnifiedDiffReaderTest::class.java.getResourceAsStream("jsqlparser_patch_1.diff") as InputStream
            )
        println(diff)
        Assertions.assertThat(diff.getFiles().size).isEqualTo(2)
        val file1 = diff.getFiles()[0]
        Assertions.assertThat(file1.fromFile).isEqualTo("src/main/jjtree/net/sf/jsqlparser/parser/JSqlParserCC.jjt")
        Assertions.assertThat(file1.patch.getDeltas().size).isEqualTo(3)
        Assertions.assertThat(diff.tail).isEqualTo("2.17.1.windows.2\n")
    }

    @Test
    fun testParseDiffBlock() {
        val files =
            UnifiedDiffReader.parseFileNames("diff --git a/src/test/java/net/sf/jsqlparser/statement/select/SelectTest.java b/src/test/java/net/sf/jsqlparser/statement/select/SelectTest.java")
        Assertions.assertThat(files).containsExactly(
            "src/test/java/net/sf/jsqlparser/statement/select/SelectTest.java",
            "src/test/java/net/sf/jsqlparser/statement/select/SelectTest.java"
        )
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
    @Throws(IOException::class)
    fun testSimpleParse2() {
        val diff =
            UnifiedDiffReader.parseUnifiedDiff(
                UnifiedDiffReaderTest::class.java.getResourceAsStream("jsqlparser_patch_1.diff") as InputStream
            )
        println(diff)
        Assertions.assertThat(diff.getFiles().size).isEqualTo(2)
        val file1 = diff.getFiles()[0]
        Assertions.assertThat(file1.fromFile).isEqualTo("src/main/jjtree/net/sf/jsqlparser/parser/JSqlParserCC.jjt")
        Assertions.assertThat(file1.patch.getDeltas().size).isEqualTo(3)
        val first = file1.patch.getDeltas()[0]
        Assertions.assertThat(first.source.size()).isGreaterThan(0)
        Assertions.assertThat(first.target.size()).isGreaterThan(0)
        Assertions.assertThat(diff.tail).isEqualTo("2.17.1.windows.2\n")
    }

    @Test
    fun testSimplePattern() {
        val pattern = Pattern.compile("^\\+\\+\\+\\s")
        val m = pattern.matcher("+++ revised.txt")
        assertTrue(m.find())
    }

    @Test
    @Throws(IOException::class)
    fun testParseIssue46() {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue46.diff") as InputStream
        )
        println(diff)
        Assertions.assertThat(diff.getFiles().size).isEqualTo(1)
        val file1 = diff.getFiles()[0]
        Assertions.assertThat(file1.fromFile).isEqualTo(".vhd")
        Assertions.assertThat(file1.patch.getDeltas().size).isEqualTo(1)
        Assertions.assertThat(diff.tail).isNull()
    }

    @Test
    @Throws(IOException::class)
    fun testParseIssue33() {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue33.diff") as InputStream
        )
        Assertions.assertThat(diff.getFiles().size).isEqualTo(1)
        val file1 = diff.getFiles()[0]
        Assertions.assertThat(file1.fromFile).isEqualTo("Main.java")
        Assertions.assertThat(file1.patch.getDeltas().size).isEqualTo(1)
        Assertions.assertThat(diff.tail).isNull()
        Assertions.assertThat(diff.header).isNull()
    }

    @Test
    @Throws(IOException::class)
    fun testParseIssue51() {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue51.diff") as InputStream
        )
        println(diff)
        Assertions.assertThat(diff.getFiles().size).isEqualTo(2)
        val file1 = diff.getFiles()[0]
        Assertions.assertThat(file1.fromFile).isEqualTo("f1")
        Assertions.assertThat(file1.patch.getDeltas().size).isEqualTo(1)
        val file2 = diff.getFiles()[1]
        Assertions.assertThat(file2.fromFile).isEqualTo("f2")
        Assertions.assertThat(file2.patch.getDeltas().size).isEqualTo(1)
        Assertions.assertThat(diff.tail).isNull()
    }

    @Test
    @Throws(IOException::class)
    fun testParseIssue79() {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue79.diff") as InputStream
        )
        Assertions.assertThat(diff.getFiles().size).isEqualTo(1)
        val file1 = diff.getFiles()[0]
        Assertions.assertThat(file1.fromFile).isEqualTo("test/Issue.java")
        Assertions.assertThat(file1.patch.getDeltas().size).isEqualTo(0)
        Assertions.assertThat(diff.tail).isNull()
        Assertions.assertThat(diff.header).isNull()
    }

    @Test
    @Throws(IOException::class)
    fun testParseIssue84() {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue84.diff") as InputStream
        )
        Assertions.assertThat(diff.getFiles().size).isEqualTo(2)
        val file1 = diff.getFiles()[0]
        Assertions.assertThat(file1.fromFile).isEqualTo("config/ant-phase-verify.xml")
        Assertions.assertThat(file1.patch.getDeltas().size).isEqualTo(1)
        val file2 = diff.getFiles()[1]
        Assertions.assertThat(file2.fromFile).isEqualTo("/dev/null")
        Assertions.assertThat(file2.patch.getDeltas().size).isEqualTo(1)
        Assertions.assertThat(diff.tail).isEqualTo("2.7.4")
        Assertions.assertThat(diff.header)
            .startsWith("From b53e612a2ab5ff15d14860e252f84c0f343fe93a Mon Sep 17 00:00:00 2001")
    }

    @Test
    @Throws(IOException::class)
    fun testParseIssue85() {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue85.diff") as InputStream
        )
        Assertions.assertThat(diff.getFiles().size).isEqualTo(1)
        assertEquals(1, diff.getFiles().size)
        val file1 = diff.getFiles()[0]
        assertEquals(
            "diff -r 83e41b73d115 -r a4438263b228 tests/test-check-pyflakes.t",
            file1.diffCommand
        )
        assertEquals("tests/test-check-pyflakes.t", file1.fromFile)
        assertEquals("tests/test-check-pyflakes.t", file1.toFile)
        assertEquals(1, file1.patch.getDeltas().size)
        assertNull(diff.tail)
    }

    @Test
    fun testTimeStampRegexp() {
        assertTrue(UnifiedDiffReader.TIMESTAMP_REGEXP.containsMatchIn("2019-04-18 13:49:39.516149751 +0200"))
    }

    @Test
    @Throws(IOException::class)
    fun testParseIssue98() {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue98.diff") as InputStream
        )
        Assertions.assertThat(diff.getFiles().size).isEqualTo(1)
        assertEquals(1, diff.getFiles().size)
        val file1 = diff.getFiles()[0]
        assertEquals(
            "100644",
            file1.deletedFileMode
        )
        assertEquals("src/test/java/se/bjurr/violations/lib/model/ViolationTest.java", file1.fromFile)
        Assertions.assertThat(diff.tail).isEqualTo("2.25.1")
    }

    @Test
    @Throws(IOException::class)
    fun testParseIssue104() {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_parsing_issue104.diff") as InputStream
        )
        Assertions.assertThat(diff.getFiles().size).isEqualTo(6)
        val file = diff.getFiles()[2]
        Assertions.assertThat(file.fromFile).isEqualTo("/dev/null")
        Assertions.assertThat(file.toFile).isEqualTo("doc/samba_data_tool_path.xml.in")
        Assertions.assertThat(file.patch.toString())
            .isEqualTo("Patch{deltas=[[ChangeDelta, position: 0, lines: [] to [@SAMBA_DATA_TOOL@]]]}")
        Assertions.assertThat(diff.tail).isEqualTo("2.14.4")
    }

    @Test
    @Throws(IOException::class)
    fun testParseIssue107BazelDiff() {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("01-bazel-strip-unused.patch_issue107.diff") as InputStream
        )
        Assertions.assertThat(diff.getFiles().size).isEqualTo(450)
        val file = diff.getFiles()[0]
        Assertions.assertThat(file.fromFile).isEqualTo("./src/main/java/com/amazonaws/AbortedException.java")
        Assertions.assertThat(file.toFile)
            .isEqualTo("/home/greg/projects/bazel/third_party/aws-sdk-auth-lite/src/main/java/com/amazonaws/AbortedException.java")
        Assertions.assertThat(diff.getFiles().stream()
            .filter { f: UnifiedDiffFile -> f.isNoNewLineAtTheEndOfTheFile }
            .count())
            .isEqualTo(48)
    }

    @Test
    @Throws(IOException::class)
    fun testParseIssue107_2() {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReader::class.java.getResourceAsStream("problem_diff_issue107.diff") as InputStream
        )
        Assertions.assertThat(diff.getFiles().size).isEqualTo(2)
        val file1 = diff.getFiles()[0]
        Assertions.assertThat(file1.fromFile).isEqualTo("Main.java")
        Assertions.assertThat(file1.patch.getDeltas().size).isEqualTo(1)
    }

    @Test
    @Throws(IOException::class)
    fun testParseIssue107_3() {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue107_3.diff") as InputStream
        )
        Assertions.assertThat(diff.getFiles().size).isEqualTo(1)
        val file1 = diff.getFiles()[0]
        Assertions.assertThat(file1.fromFile).isEqualTo("Billion laughs attack.md")
        Assertions.assertThat(file1.patch.getDeltas().size).isEqualTo(1)
    }

    @Test
    @Throws(IOException::class)
    fun testParseIssue107_4() {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue107_4.diff") as InputStream
        )
        Assertions.assertThat(diff.getFiles().size).isEqualTo(27)
        Assertions.assertThat(diff.getFiles()).extracting<String, RuntimeException> { f: UnifiedDiffFile -> f.fromFile }
            .contains("README.md")
    }

    @Test
    @Throws(IOException::class)
    fun testParseIssue107_5() {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue107_5.diff") as InputStream
        )
        Assertions.assertThat(diff.getFiles().size).isEqualTo(22)
        Assertions.assertThat(diff.getFiles()).extracting<String, RuntimeException> { f: UnifiedDiffFile -> f.fromFile }
            .contains("rt/management/src/test/java/org/apache/cxf/management/jmx/MBServerConnectorFactoryTest.java")
    }

    @Test
    @Throws(IOException::class)
    fun testParseIssue110() {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("0001-avahi-python-Use-the-agnostic-DBM-interface.patch") as InputStream
        )
        Assertions.assertThat(diff.getFiles().size).isEqualTo(5)
        val file = diff.getFiles()[4]
        Assertions.assertThat(file.similarityIndex).isEqualTo(87)
        Assertions.assertThat(file.renameFrom).isEqualTo("service-type-database/build-db.in")
        Assertions.assertThat(file.renameTo).isEqualTo("service-type-database/build-db")
        Assertions.assertThat(file.fromFile).isEqualTo("service-type-database/build-db.in")
        Assertions.assertThat(file.toFile).isEqualTo("service-type-database/build-db")
    }

    @Test
    @Throws(IOException::class)
    fun testParseIssue117() {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue117.diff") as InputStream
        )
        Assertions.assertThat(diff.getFiles().size).isEqualTo(2)
        Assertions.assertThat(diff.getFiles()[0].patch.getDeltas()[0].source.changePosition)
            .containsExactly(24, 27)
        Assertions.assertThat(diff.getFiles()[0].patch.getDeltas()[0].target.changePosition)
            .containsExactly(24, 27)
        Assertions.assertThat(diff.getFiles()[0].patch.getDeltas()[1].source.changePosition)
            .containsExactly(64)
        Assertions.assertThat(diff.getFiles()[0].patch.getDeltas()[1].target.changePosition)
            .containsExactly(64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74)

//        diff.getFiles().forEach(f -> {
//            System.out.println("File: " + f.getFromFile());
//            f.getPatch().getDeltas().forEach(delta -> {
//
//                System.out.println(delta);
//                System.out.println("Source: ");
//                System.out.println(delta.getSource().getPosition());
//                System.out.println(delta.getSource().getChangePosition());
//
//                System.out.println("Target: ");
//                System.out.println(delta.getTarget().getPosition());
//                System.out.println(delta.getTarget().getChangePosition());
//            });
//        });
    }

    @Test
    @Throws(IOException::class)
    fun testParseIssue122() {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue122.diff") as InputStream
        )
        Assertions.assertThat(diff.getFiles().size).isEqualTo(1)
        Assertions.assertThat(diff.getFiles()).extracting<String, RuntimeException> { f: UnifiedDiffFile -> f.fromFile }
            .contains("coders/wpg.c")
    }

    @Test
    @Throws(IOException::class)
    fun testParseIssue123() {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("problem_diff_issue123.diff") as InputStream
        )
        Assertions.assertThat(diff.getFiles().size).isEqualTo(2)
        Assertions.assertThat(diff.getFiles()).extracting<String, RuntimeException> { f: UnifiedDiffFile -> f.fromFile }
            .contains("src/java/main/org/apache/zookeeper/server/FinalRequestProcessor.java")
    }

    @Test
    @Throws(IOException::class)
    fun testAddingNewLine() {
        val diff = UnifiedDiffReader.parseUnifiedDiff(
            UnifiedDiffReaderTest::class.java.getResourceAsStream("new_line_added.diff") as InputStream
        )
        Assertions.assertThat(diff.getFiles().size).isEqualTo(1)
        val file = diff.getFiles()[0]
        Assertions.assertThat(file.renameFrom).isNull()
        Assertions.assertThat(file.renameTo).isNull()
        Assertions.assertThat(file.fromFile).isEqualTo("settings.gradle")
        Assertions.assertThat(file.toFile).isEqualTo("settings.gradle")
        val deltas = file.patch.getDeltas()
        Assertions.assertThat(deltas.size).isEqualTo(1)
        val delta = deltas[0]
        Assertions.assertThat(delta.source.lines).isEqualTo(listOf("rootProject.name = \"sample-repo\""))
        Assertions.assertThat(delta.target.lines).isEqualTo(listOf("rootProject.name = \"sample-repo\"", ""))
    }
}