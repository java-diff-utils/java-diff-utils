package dev.gitlive.difflib.examples

import dev.gitlive.difflib.DiffUtils
import dev.gitlive.difflib.TestConstants
import dev.gitlive.difflib.patch.AbstractDelta
import dev.gitlive.difflib.patch.DiffException
import dev.gitlive.difflib.patch.Patch
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

        for (delta in patch.getDeltas()) {
            println(delta)
        }
    }
}
