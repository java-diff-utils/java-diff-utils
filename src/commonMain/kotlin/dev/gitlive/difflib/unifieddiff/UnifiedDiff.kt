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
package dev.gitlive.difflib.unifieddiff

import dev.gitlive.difflib.Predicate
import dev.gitlive.difflib.patch.PatchFailedException

/**
 *
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
class UnifiedDiff {
    var header: String? = null
    var tail: String? = null
        private set
    private val files: MutableList<UnifiedDiffFile> = ArrayList()
    fun addFile(file: UnifiedDiffFile) {
        files.add(file)
    }

    fun getFiles(): List<UnifiedDiffFile> {
        return files
    }

    fun setTailTxt(tailTxt: String?) {
        tail = tailTxt.takeUnless { it.isNullOrEmpty() }
    }

//    @Throws(PatchFailedException::class)
    fun applyPatchTo(findFile: Predicate<String>, originalLines: List<String>): List<String> {
        val file = files.asSequence()
            .filter { diff: UnifiedDiffFile -> findFile(diff.fromFile!!) }
            .firstOrNull()
        return file?.patch?.applyTo(originalLines) ?: originalLines
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun from(header: String?, tail: String?, vararg files: UnifiedDiffFile): UnifiedDiff {
            val diff = UnifiedDiff()
            diff.header = header
            diff.setTailTxt(tail)
            for (file in files) {
                diff.addFile(file)
            }
            return diff
        }
    }
}