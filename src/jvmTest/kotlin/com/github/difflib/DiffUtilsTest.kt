package com.github.difflib

import com.github.difflib.algorithm.DiffException
import com.github.difflib.patch.ChangeDelta
import com.github.difflib.patch.Chunk
import com.github.difflib.patch.DeleteDelta
import com.github.difflib.patch.AbstractDelta
import com.github.difflib.patch.InsertDelta
import com.github.difflib.patch.Patch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.stream.Collectors.toList
import java.util.zip.ZipFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

class DiffUtilsTest {

    @Test
    @Throws(DiffException::class)
    fun testDiff_Insert() {
        val patch = DiffUtils.diff(Arrays.asList("hhh"), Arrays.asList("hhh", "jjj", "kkk"))
        assertNotNull(patch)
        assertEquals(1, patch.deltas.size.toLong())
        val delta = patch.deltas[0]
        assertTrue(delta is InsertDelta<*>)
        assertEquals(Chunk<String>(1, emptyList<String>()), delta.source)
        assertEquals(Chunk(1, Arrays.asList("jjj", "kkk")), delta.target)
    }

    @Test
    @Throws(DiffException::class)
    fun testDiff_Delete() {
        val patch = DiffUtils.diff(Arrays.asList("ddd", "fff", "ggg"), Arrays.asList("ggg"))
        assertNotNull(patch)
        assertEquals(1, patch.deltas.size.toLong())
        val delta = patch.deltas[0]
        assertTrue(delta is DeleteDelta<*>)
        assertEquals(Chunk(0, Arrays.asList("ddd", "fff")), delta.source)
        assertEquals(Chunk<String>(0, emptyList<String>()), delta.target)
    }

    @Test
    @Throws(DiffException::class)
    fun testDiff_Change() {
        val changeTest_from = Arrays.asList("aaa", "bbb", "ccc")
        val changeTest_to = Arrays.asList("aaa", "zzz", "ccc")

        val patch = DiffUtils.diff(changeTest_from, changeTest_to)
        assertNotNull(patch)
        assertEquals(1, patch.deltas.size.toLong())
        val delta = patch.deltas[0]
        assertTrue(delta is ChangeDelta<*>)
        assertEquals(Chunk(1, Arrays.asList("bbb")), delta.source)
        assertEquals(Chunk(1, Arrays.asList("zzz")), delta.target)
    }

    @Test
    @Throws(DiffException::class)
    fun testDiff_EmptyList() {
        val patch = DiffUtils.diff(ArrayList(), ArrayList<String>())
        assertNotNull(patch)
        assertEquals(0, patch.deltas.size.toLong())
    }

    @Test
    @Throws(DiffException::class)
    fun testDiff_EmptyListWithNonEmpty() {
        val patch = DiffUtils.diff(ArrayList(), Arrays.asList("aaa"))
        assertNotNull(patch)
        assertEquals(1, patch.deltas.size.toLong())
        val delta = patch.deltas[0]
        assertTrue(delta is InsertDelta<*>)
    }

    @Test
    @Throws(DiffException::class)
    fun testDiffInline() {
        val patch = DiffUtils.diffInline("", "test")
        assertEquals(1, patch.deltas.size.toLong())
        assertTrue(patch.deltas[0] is InsertDelta<*>)
        assertEquals(0, patch.deltas[0].source.position.toLong())
        assertEquals(0, patch.deltas[0].source.lines!!.size.toLong())
        assertEquals("test", patch.deltas[0].target.lines!![0])
    }

    @Test
    @Throws(DiffException::class)
    fun testDiffInline2() {
        val patch = DiffUtils.diffInline("es", "fest")
        assertEquals(2, patch.deltas.size.toLong())
        assertTrue(patch.deltas[0] is InsertDelta<*>)
        assertEquals(0, patch.deltas[0].source.position.toLong())
        assertEquals(2, patch.deltas[1].source.position.toLong())
        assertEquals(0, patch.deltas[0].source.lines!!.size.toLong())
        assertEquals(0, patch.deltas[1].source.lines!!.size.toLong())
        assertEquals("f", patch.deltas[0].target.lines!![0])
        assertEquals("t", patch.deltas[1].target.lines!![0])
    }

    @Test
    @Throws(DiffException::class)
    fun testDiffIntegerList() {
        val original = Arrays.asList(1, 2, 3, 4, 5)
        val revised = Arrays.asList(2, 3, 4, 6)

        val patch = DiffUtils.diff(original, revised)

        for (delta in patch.deltas) {
            println(delta)
        }

        assertEquals(2, patch.deltas.size.toLong())
        assertEquals("[DeleteDelta, position: 0, lines: [1]]", patch.deltas[0].toString())
        assertEquals("[ChangeDelta, position: 4, lines: [5] to [6]]", patch.deltas[1].toString())
    }

    @Test
    @Throws(DiffException::class)
    fun testDiffMissesChangeForkDnaumenkoIssue31() {
        val original = Arrays.asList("line1", "line2", "line3")
        val revised = Arrays.asList("line1", "line2-2", "line4")

        val patch = DiffUtils.diff(original, revised)
        assertEquals(1, patch.deltas.size.toLong())
        assertEquals("[ChangeDelta, position: 1, lines: [line2, line3] to [line2-2, line4]]", patch.deltas[0].toString())
    }

    /**
     * To test this, the greedy meyer algorithm is not suitable.
     */
    @Test
    @Ignore
    @Throws(IOException::class, DiffException::class)
    fun testPossibleDiffHangOnLargeDatasetDnaumenkoIssue26() {
        val zip = ZipFile(TestConstants.MOCK_FOLDER + "/large_dataset1.zip")

        val patch = DiffUtils.diff(
                readStringListFromInputStream(zip.getInputStream(zip.getEntry("ta"))),
                readStringListFromInputStream(zip.getInputStream(zip.getEntry("tb"))))

        assertEquals(1, patch.deltas.size.toLong())
    }

    @Test
    @Throws(DiffException::class)
    fun testDiffMyersExample1() {
        val patch = DiffUtils.diff(Arrays.asList("A", "B", "C", "A", "B", "B", "A"), Arrays.asList("C", "B", "A", "B", "A", "C"))
        assertNotNull(patch)
        assertEquals(4, patch.deltas.size.toLong())
        assertEquals("Patch{deltas=[[DeleteDelta, position: 0, lines: [A, B]], [InsertDelta, position: 3, lines: [B]], [DeleteDelta, position: 5, lines: [B]], [InsertDelta, position: 7, lines: [C]]]}", patch.toString())
    }

    companion object {

        @Throws(IOException::class)
        fun readStringListFromInputStream(`is`: InputStream): List<String> {
            BufferedReader(
                    InputStreamReader(`is`, Charset.forName(StandardCharsets.UTF_8.name()))).use { reader ->

                return reader.lines().collect(toList<String>())
            }
        }
    }
}
