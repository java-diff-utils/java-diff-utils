package com.github.difflib.examples

import com.github.difflib.DiffUtils
import com.github.difflib.TestConstants
import com.github.difflib.algorithm.DiffException
import com.github.difflib.patch.AbstractDelta
import com.github.difflib.patch.Patch
import java.io.File
import java.io.IOException
import java.nio.file.Files

object ComputeDifference {

    private val ORIGINAL = TestConstants.MOCK_FOLDER + "original.txt"
    private val RIVISED = TestConstants.MOCK_FOLDER + "revised.txt"

    @Throws(DiffException::class, IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val original = Files.readAllLines(File(ORIGINAL).toPath())
        val revised = Files.readAllLines(File(RIVISED).toPath())

        // Compute diff. Get the Patch object. Patch is the container for computed deltas.
        val patch = DiffUtils.diff(original, revised)

        for (delta in patch.deltas) {
            println(delta)
        }
    }
}
