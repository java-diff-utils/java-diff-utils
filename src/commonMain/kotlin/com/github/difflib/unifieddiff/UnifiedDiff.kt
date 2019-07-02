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
package com.github.difflib.unifieddiff

import com.github.difflib.Predicate
import com.github.difflib.patch.PatchFailedException

/**
 *
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
class UnifiedDiff {

    var header: String? = null
    var tail: String? = null
        private set
    internal val files = ArrayList<UnifiedDiffFile>()

    fun addFile(file: UnifiedDiffFile) {
        files.add(file)
    }

    fun getFiles(): List<UnifiedDiffFile> {
        return files
    }

    fun setTailTxt(tailTxt: String) {
        this.tail = tailTxt
    }

//    @Throws(PatchFailedException::class)
    fun spplyPatchTo(findFile: Predicate<String>, originalLines: List<String>): List<String> {
        val file = files.filter { diff -> findFile(diff.fromFile!!) }.firstOrNull()
        return if (file != null) {
            file.patch.applyTo(originalLines)
        } else {
            originalLines
        }
    }

    companion object {

        fun from(header: String, tail: String, vararg files: UnifiedDiffFile): UnifiedDiff {
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
