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

import dev.gitlive.difflib.patch.Patch

/**
 * Data structure for one patched file from a unified diff file.
 *
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
class UnifiedDiffFile {
    var diffCommand: String? = null
    var fromFile: String? = null
    var fromTimestamp: String? = null
    var toFile: String? = null
    var renameFrom: String? = null
    var renameTo: String? = null
    var toTimestamp: String? = null
    var index: String? = null
    var newFileMode: String? = null
    var deletedFileMode: String? = null
    var patch = Patch<String>()
        private set
    var isNoNewLineAtTheEndOfTheFile = false
    var similarityIndex: Int? = null

    companion object {
        @kotlin.jvm.JvmStatic
        fun from(fromFile: String?, toFile: String?, patch: Patch<String>): UnifiedDiffFile {
            val file = UnifiedDiffFile()
            file.fromFile = fromFile
            file.toFile = toFile
            file.patch = patch
            return file
        }
    }
}