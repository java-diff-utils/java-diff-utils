package live.git.difflib.examples

import live.git.difflib.DiffUtils
import live.git.difflib.TestConstants
import live.git.difflib.UnifiedDiffUtils
import live.git.difflib.patch.Patch
import live.git.difflib.patch.PatchFailedException
import java.io.File
import java.io.IOException
import java.nio.file.Files

object ApplyPatch {

    private val ORIGINAL = TestConstants.MOCK_FOLDER + "issue10_base.txt"
    private val PATCH = TestConstants.MOCK_FOLDER + "issue10_patch.txt"

    @Throws(PatchFailedException::class, IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val original = Files.readAllLines(File(ORIGINAL).toPath())
        val patched = Files.readAllLines(File(PATCH).toPath())

        // At first, parse the unified diff file and get the patch
        val patch = UnifiedDiffUtils.parseUnifiedDiff(patched)

        // Then apply the computed patch to the given text
        val result = DiffUtils.patch(original, patch)
        println(result)
        // / Or we can call patch.applyTo(original). There is no difference.
    }
}
