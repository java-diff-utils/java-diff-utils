package com.github.difflib.text

import com.github.difflib.algorithm.DiffException
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.Arrays
import java.util.Collections
import java.util.regex.Pattern
import java.util.stream.Collectors.toList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiffRowGeneratorTest {

    @Test
    @Throws(DiffException::class)
    fun testGenerator_Default() {
        val first = "anything \n \nother"
        val second = "anything\n\nother"

        val generator = DiffRowGenerator.create()
                .columnWidth(Integer.MAX_VALUE) // do not wrap
                .build()
        val rows = generator.generateDiffRows(split(first), split(second))
        print(rows)

        assertEquals(3, rows.size.toLong())
    }

    /**
     * Test of normalize method, of class StringUtils.
     */
    @Test
    fun testNormalize_List() {
        val generator = DiffRowGenerator.create()
                .build()
        assertEquals(listOf<String>("    test"), generator.normalizeLines(listOf<String>("\ttest")))
    }

    @Test
    @Throws(DiffException::class)
    fun testGenerator_Default2() {
        val first = "anything \n \nother"
        val second = "anything\n\nother"

        val generator = DiffRowGenerator.create()
                .columnWidth(0) // do not wrap
                .build()
        val rows = generator.generateDiffRows(split(first), split(second))
        print(rows)

        assertEquals(3, rows.size.toLong())
    }

    @Test
    @Throws(DiffException::class)
    fun testGenerator_InlineDiff() {
        val first = "anything \n \nother"
        val second = "anything\n\nother"

        val generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .columnWidth(Integer.MAX_VALUE) // do not wrap
                .build()
        val rows = generator.generateDiffRows(split(first), split(second))
        print(rows)

        assertEquals(3, rows.size.toLong())
        assertTrue(rows[0].oldLine!!.indexOf("<span") > 0)
    }

    @Test
    @Throws(DiffException::class)
    fun testGenerator_IgnoreWhitespaces() {
        val first = "anything \n \nother\nmore lines"
        val second = "anything\n\nother\nsome more lines"

        val generator = DiffRowGenerator.create()
                .ignoreWhiteSpaces(true)
                .columnWidth(Integer.MAX_VALUE) // do not wrap
                .build()
        val rows = generator.generateDiffRows(split(first), split(second))
        print(rows)

        assertEquals(4, rows.size.toLong())
        assertEquals(rows[0].tag, DiffRow.Tag.EQUAL)
        assertEquals(rows[1].tag, DiffRow.Tag.EQUAL)
        assertEquals(rows[2].tag, DiffRow.Tag.EQUAL)
        assertEquals(rows[3].tag, DiffRow.Tag.CHANGE)
    }

    private fun split(content: String): List<String> {
        return Arrays.asList(*content.split("\n".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray())
    }

    private fun print(diffRows: List<DiffRow>) {
        for (row in diffRows) {
            println(row)
        }
    }

    @Test
    @Throws(DiffException::class)
    fun testGeneratorWithWordWrap() {
        val first = "anything \n \nother"
        val second = "anything\n\nother"

        val generator = DiffRowGenerator.create()
                .columnWidth(5)
                .build()
        val rows = generator.generateDiffRows(split(first), split(second))
        print(rows)

        assertEquals(3, rows.size.toLong())
        assertEquals("[CHANGE,anyth<br/>ing ,anyth<br/>ing]", rows[0].toString())
        assertEquals("[CHANGE, ,]", rows[1].toString())
        assertEquals("[EQUAL,other,other]", rows[2].toString())
    }

    @Test
    @Throws(DiffException::class)
    fun testGeneratorWithMerge() {
        val first = "anything \n \nother"
        val second = "anything\n\nother"

        val generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .build()
        val rows = generator.generateDiffRows(split(first), split(second))
        print(rows)

        assertEquals(3, rows.size.toLong())
        assertEquals("[CHANGE,anything<span class=\"editOldInline\"> </span>,anything]", rows[0].toString())
        assertEquals("[CHANGE,<span class=\"editOldInline\"> </span>,]", rows[1].toString())
        assertEquals("[EQUAL,other,other]", rows[2].toString())
    }

    @Test
    @Throws(DiffException::class)
    fun testGeneratorWithMerge2() {
        val generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .build()
        val rows = generator.generateDiffRows(Arrays.asList("Test"), Arrays.asList("ester"))
        print(rows)

        assertEquals(1, rows.size.toLong())
        assertEquals("[CHANGE,<span class=\"editOldInline\">T</span>est<span class=\"editNewInline\">er</span>,ester]", rows[0].toString())
    }

    @Test
    @Throws(DiffException::class)
    fun testGeneratorWithMerge3() {
        val first = "test\nanything \n \nother"
        val second = "anything\n\nother\ntest\ntest2"

        val generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .build()
        val rows = generator.generateDiffRows(split(first), split(second))
        print(rows)

        assertEquals(6, rows.size.toLong())
        assertEquals("[CHANGE,<span class=\"editOldInline\">test</span>,anything]", rows[0].toString())
        assertEquals("[CHANGE,anything<span class=\"editOldInline\"> </span>,]", rows[1].toString())
        assertEquals("[CHANGE,<span class=\"editOldInline\"> </span>,]", rows[2].toString())
        assertEquals("[EQUAL,other,other]", rows[3].toString())
        assertEquals("[INSERT,<span class=\"editNewInline\">test</span>,test]", rows[4].toString())
        assertEquals("[INSERT,<span class=\"editNewInline\">test2</span>,test2]", rows[5].toString())
    }

    @Test
    @Throws(DiffException::class)
    fun testGeneratorWithMergeByWord4() {
        val generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .inlineDiffByWord(true)
                .build()
        val rows = generator.generateDiffRows(Arrays.asList("Test"), Arrays.asList("ester"))
        print(rows)

        assertEquals(1, rows.size.toLong())
        assertEquals("[CHANGE,<span class=\"editOldInline\">Test</span><span class=\"editNewInline\">ester</span>,ester]", rows[0].toString())
    }

    @Test
    @Throws(DiffException::class)
    fun testGeneratorWithMergeByWord5() {
        val generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .inlineDiffByWord(true)
                .columnWidth(80)
                .build()
        val rows = generator.generateDiffRows(Arrays.asList("Test feature"), Arrays.asList("ester feature best"))
        print(rows)

        assertEquals(1, rows.size.toLong())
        assertEquals("[CHANGE,<span class=\"editOldInline\">Test</span><span class=\"editNewInline\">ester</span> <br/>feature<span class=\"editNewInline\"> best</span>,ester feature best]", rows[0].toString())
    }

    @Test
    fun testSplitString() {
        val list = DiffRowGenerator.splitStringPreserveDelimiter("test,test2", DiffRowGenerator.SPLIT_BY_WORD_PATTERN)
        assertEquals(3, list.size.toLong())
        assertEquals("[test, ,, test2]", list.toString())
    }

    @Test
    fun testSplitString2() {
        val list = DiffRowGenerator.splitStringPreserveDelimiter("test , test2", DiffRowGenerator.SPLIT_BY_WORD_PATTERN)
        println(list)
        assertEquals(5, list.size.toLong())
        assertEquals("[test,  , ,,  , test2]", list.toString())
    }

    @Test
    fun testSplitString3() {
        val list = DiffRowGenerator.splitStringPreserveDelimiter("test,test2,", DiffRowGenerator.SPLIT_BY_WORD_PATTERN)
        println(list)
        assertEquals(4, list.size.toLong())
        assertEquals("[test, ,, test2, ,]", list.toString())
    }

    @Test
    @Throws(DiffException::class)
    fun testGeneratorExample1() {
        val generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .inlineDiffByWord(true)
                .oldTag { f -> "~" }
                .newTag { f -> "**" }
                .build()
        val rows = generator.generateDiffRows(
                Arrays.asList("This is a test senctence."),
                Arrays.asList("This is a test for diffutils."))

        println(rows[0].oldLine)

        assertEquals(1, rows.size.toLong())
        assertEquals("This is a test ~senctence~**for diffutils**.", rows[0].oldLine)
    }

    @Test
    @Throws(DiffException::class)
    fun testGeneratorExample2() {
        val generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .oldTag { f -> "~" }
                .newTag { f -> "**" }
                .build()
        val rows = generator.generateDiffRows(
                Arrays.asList("This is a test senctence.", "This is the second line.", "And here is the finish."),
                Arrays.asList("This is a test for diffutils.", "This is the second line."))

        println("|original|new|")
        println("|--------|---|")
        for (row in rows) {
            println("|" + row.oldLine + "|" + row.newLine + "|")
        }

        assertEquals(3, rows.size.toLong())
        assertEquals("This is a test ~senctence~.", rows[0].oldLine)
        assertEquals("This is a test **for diffutils**.", rows[0].newLine)
    }

    @Test
    @Throws(DiffException::class)
    fun testGeneratorUnchanged() {
        val first = "anything \n \nother"
        val second = "anything\n\nother"

        val generator = DiffRowGenerator.create()
                .columnWidth(5)
                .reportLinesUnchanged(true)
                .build()
        val rows = generator.generateDiffRows(split(first), split(second))
        print(rows)

        assertEquals(3, rows.size.toLong())
        assertEquals("[CHANGE,anything ,anything]", rows[0].toString())
        assertEquals("[CHANGE, ,]", rows[1].toString())
        assertEquals("[EQUAL,other,other]", rows[2].toString())
    }

    @Test
    @Throws(DiffException::class)
    fun testGeneratorIssue14() {
        val generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .inlineDiffBySplitter { line -> DiffRowGenerator.splitStringPreserveDelimiter(line, Pattern.compile(",")) }
                .oldTag { f -> "~" }
                .newTag { f -> "**" }
                .build()
        val rows = generator.generateDiffRows(
                Arrays.asList("J. G. Feldstein, Chair"),
                Arrays.asList("T. P. Pastor, Chair"))

        println(rows[0].oldLine)

        assertEquals(1, rows.size.toLong())
        assertEquals("~J. G. Feldstein~**T. P. Pastor**, Chair", rows[0].oldLine)
    }

    @Test
    @Throws(DiffException::class, IOException::class)
    fun testGeneratorIssue15() {
        val generator = DiffRowGenerator.create()
                .showInlineDiffs(true) //show the ~ ~ and ** ** symbols on each difference
                .inlineDiffByWord(true) //show the ~ ~ and ** ** around each different word instead of each letter
                //.reportLinesUnchanged(true) //experiment
                .oldTag { f -> "~" }
                .newTag { f -> "**" }
                .build()

        val listOne = Files.lines(File("target/test-classes/mocks/issue15_1.txt").toPath())
                .collect<List<String>, Any>(toList())

        val listTwo = Files.lines(File("target/test-classes/mocks/issue15_2.txt").toPath())
                .collect<List<String>, Any>(toList())

        val rows = generator.generateDiffRows(listOne, listTwo)

        assertEquals(9, rows.size.toLong())

        for (row in rows) {
            println("|" + row.oldLine + "| " + row.newLine + " |")
            if (!row.oldLine!!.startsWith("TABLE_NAME")) {
                assertTrue(row.newLine!!.startsWith("**ACTIONS_C16913**"))
                assertTrue(row.oldLine!!.startsWith("~ACTIONS_C1700"))
            }
        }
    }

    @Test
    @Throws(DiffException::class)
    fun testGeneratorIssue22() {
        val generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .oldTag { f -> "~" }
                .newTag { f -> "**" }
                .build()
        val aa = "This is a test senctence."
        val bb = "This is a test for diffutils.\nThis is the second line."
        val rows = generator.generateDiffRows(
                Arrays.asList(*aa.split("\n".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()),
                Arrays.asList(*bb.split("\n".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()))

        assertEquals("[[CHANGE,This is a test ~senctence~.,This is a test **for diffutils**.], [CHANGE,,**This is the second line.**]]",
                rows.toString())

        println("|original|new|")
        println("|--------|---|")
        for (row in rows) {
            println("|" + row.oldLine + "|" + row.newLine + "|")
        }
    }

    @Test
    @Throws(DiffException::class)
    fun testGeneratorIssue22_2() {
        val generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .oldTag { f -> "~" }
                .newTag { f -> "**" }
                .build()
        val aa = "This is a test for diffutils.\nThis is the second line."
        val bb = "This is a test senctence."
        val rows = generator.generateDiffRows(
                Arrays.asList(*aa.split("\n".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()),
                Arrays.asList(*bb.split("\n".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()))

        assertEquals("[[CHANGE,This is a test ~for diffutils~.,This is a test **senctence**.], [CHANGE,~This is the second line.~,]]",
                rows.toString())
    }

    @Test
    @Throws(DiffException::class)
    fun testGeneratorIssue22_3() {
        val generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .oldTag { f -> "~" }
                .newTag { f -> "**" }
                .build()
        val aa = "This is a test senctence."
        val bb = "This is a test for diffutils.\nThis is the second line.\nAnd one more."
        val rows = generator.generateDiffRows(
                Arrays.asList(*aa.split("\n".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()),
                Arrays.asList(*bb.split("\n".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()))

        assertEquals("[[CHANGE,This is a test ~senctence~.,This is a test **for diffutils**.], [CHANGE,,**This is the second line.**], [CHANGE,,**And one more.**]]",
                rows.toString())
    }

    @Test
    @Throws(DiffException::class)
    fun testGeneratorIssue41DefaultNormalizer() {
        val generator = DiffRowGenerator.create()
                .build()
        val rows = generator.generateDiffRows(Arrays.asList("<"), Arrays.asList("<"))
        assertEquals("[[EQUAL,&lt;,&lt;]]", rows.toString())
    }

    @Test
    @Throws(DiffException::class)
    fun testGeneratorIssue41UserNormalizer() {
        val generator = DiffRowGenerator.create()
                .lineNormalizer { str -> str.replace("\t", "    ") }
                .build()
        var rows = generator.generateDiffRows(Arrays.asList("<"), Arrays.asList("<"))
        assertEquals("[[EQUAL,<,<]]", rows.toString())
        rows = generator.generateDiffRows(Arrays.asList("\t<"), Arrays.asList("<"))
        assertEquals("[[CHANGE,    <,<]]", rows.toString())
    }
}
