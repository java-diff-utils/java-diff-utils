package com.github.difflib

import com.github.difflib.algorithm.DiffException
import com.github.difflib.patch.Patch
import com.github.difflib.patch.PatchFailedException
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.util.ArrayList
import java.util.Arrays
import java.util.stream.Collectors.joining
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class GenerateUnifiedDiffTest {

    @Test
    @Throws(DiffException::class, IOException::class)
    fun testGenerateUnified() {
        val origLines = fileToLines(TestConstants.MOCK_FOLDER + "original.txt")
        val revLines = fileToLines(TestConstants.MOCK_FOLDER + "revised.txt")

        verify(origLines, revLines, "original.txt", "revised.txt")
    }

    @Test
    @Throws(DiffException::class, IOException::class)
    fun testGenerateUnifiedWithOneDelta() {
        val origLines = fileToLines(TestConstants.MOCK_FOLDER + "one_delta_test_original.txt")
        val revLines = fileToLines(TestConstants.MOCK_FOLDER + "one_delta_test_revised.txt")

        verify(origLines, revLines, "one_delta_test_original.txt", "one_delta_test_revised.txt")
    }

    @Test
    @Throws(DiffException::class)
    fun testGenerateUnifiedDiffWithoutAnyDeltas() {
        val test = Arrays.asList("abc")
        val patch = DiffUtils.diff(test, test)
        UnifiedDiffUtils.generateUnifiedDiff("abc", "abc", test, patch, 0)
    }

    @Test
    @Throws(IOException::class)
    fun testDiff_Issue10() {
        val baseLines = fileToLines(TestConstants.MOCK_FOLDER + "issue10_base.txt")
        val patchLines = fileToLines(TestConstants.MOCK_FOLDER + "issue10_patch.txt")
        val p = UnifiedDiffUtils.parseUnifiedDiff(patchLines)
        try {
            DiffUtils.patch(baseLines, p)
        } catch (e: PatchFailedException) {
            fail(e.message)
        }

    }

    /**
     * Issue 12
     */
    @Test
    @Throws(DiffException::class, IOException::class)
    fun testPatchWithNoDeltas() {
        val lines1 = fileToLines(TestConstants.MOCK_FOLDER + "issue11_1.txt")
        val lines2 = fileToLines(TestConstants.MOCK_FOLDER + "issue11_2.txt")
        verify(lines1, lines2, "issue11_1.txt", "issue11_2.txt")
    }

    @Test
    @Throws(DiffException::class, IOException::class)
    fun testDiff5() {
        val lines1 = fileToLines(TestConstants.MOCK_FOLDER + "5A.txt")
        val lines2 = fileToLines(TestConstants.MOCK_FOLDER + "5B.txt")
        verify(lines1, lines2, "5A.txt", "5B.txt")
    }

    /**
     * Issue 19
     */
    @Test
    @Throws(DiffException::class)
    fun testDiffWithHeaderLineInText() {
        val original = ArrayList<String>()
        val revised = ArrayList<String>()

        original.add("test line1")
        original.add("test line2")
        original.add("test line 4")
        original.add("test line 5")

        revised.add("test line1")
        revised.add("test line2")
        revised.add("@@ -2,6 +2,7 @@")
        revised.add("test line 4")
        revised.add("test line 5")

        val patch = DiffUtils.diff(original, revised)
        val udiff = UnifiedDiffUtils.generateUnifiedDiff("original", "revised",
                original, patch, 10)
        UnifiedDiffUtils.parseUnifiedDiff(udiff)
    }

    @Throws(DiffException::class)
    private fun verify(origLines: List<String>, revLines: List<String>,
                       originalFile: String, revisedFile: String) {
        val patch = DiffUtils.diff(origLines, revLines)
        val unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(originalFile, revisedFile,
                origLines, patch, 10)

        println(unifiedDiff.stream().collect<String, *>(joining("\n")))

        val fromUnifiedPatch = UnifiedDiffUtils.parseUnifiedDiff(unifiedDiff)
        val patchedLines: List<String>
        try {
            patchedLines = fromUnifiedPatch.applyTo(origLines)
            assertEquals(revLines.size.toLong(), patchedLines.size.toLong())
            for (i in revLines.indices) {
                val l1 = revLines[i]
                val l2 = patchedLines[i]
                if (l1 != l2) {
                    fail("Line " + (i + 1) + " of the patched file did not match the revised original")
                }
            }
        } catch (e: PatchFailedException) {
            fail(e.message)
        }

    }

    companion object {

        @Throws(FileNotFoundException::class, IOException::class)
        fun fileToLines(filename: String): List<String> {
            val lines = ArrayList<String>()
            var line = ""
            BufferedReader(FileReader(filename)).use { `in` ->
                while ((line = `in`.readLine()) != null) {
                    lines.add(line)
                }
            }
            return lines
        }
    }
}
