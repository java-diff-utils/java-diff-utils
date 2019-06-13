package com.github.difflib.patch

import com.github.difflib.DiffUtils
import com.github.difflib.algorithm.DiffException
import java.util.Arrays
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class PatchTest {

    @Test
    @Throws(DiffException::class)
    fun testPatch_Insert() {
        val insertTest_from = Arrays.asList("hhh")
        val insertTest_to = Arrays.asList("hhh", "jjj", "kkk", "lll")

        val patch = DiffUtils.diff(insertTest_from, insertTest_to)
        try {
            assertEquals(insertTest_to, DiffUtils.patch(insertTest_from, patch))
        } catch (e: PatchFailedException) {
            fail(e.message)
        }

    }

    @Test
    @Throws(DiffException::class)
    fun testPatch_Delete() {
        val deleteTest_from = Arrays.asList("ddd", "fff", "ggg", "hhh")
        val deleteTest_to = Arrays.asList("ggg")

        val patch = DiffUtils.diff(deleteTest_from, deleteTest_to)
        try {
            assertEquals(deleteTest_to, DiffUtils.patch(deleteTest_from, patch))
        } catch (e: PatchFailedException) {
            fail(e.message)
        }

    }

    @Test
    @Throws(DiffException::class)
    fun testPatch_Change() {
        val changeTest_from = Arrays.asList("aaa", "bbb", "ccc", "ddd")
        val changeTest_to = Arrays.asList("aaa", "bxb", "cxc", "ddd")

        val patch = DiffUtils.diff(changeTest_from, changeTest_to)
        try {
            assertEquals(changeTest_to, DiffUtils.patch(changeTest_from, patch))
        } catch (e: PatchFailedException) {
            fail(e.message)
        }

    }
}
