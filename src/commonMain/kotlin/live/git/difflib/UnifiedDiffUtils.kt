/*
 * Copyright 2017 java-diff-utils.
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
package live.git.difflib

import live.git.difflib.patch.ChangeDelta
import live.git.difflib.patch.Chunk
import live.git.difflib.patch.AbstractDelta
import live.git.difflib.patch.Patch

/**
 *
 * @author toben
 */
object UnifiedDiffUtils {

    private val UNIFIED_DIFF_CHUNK_REGEXP =
            Regex("^@@\\s+-(?:(\\d+)(?:,(\\d+))?)\\s+\\+(?:(\\d+)(?:,(\\d+))?)\\s+@@$")

    /**
     * Parse the given text in unified format and creates the list of deltas for it.
     *
     * @param diff the text in unified format
     * @return the patch with deltas.
     */
    fun parseUnifiedDiff(diff: List<String>): Patch<String> {
        var inPrelude = true
        val rawChunk = ArrayList<Array<String>>()
        val patch = Patch<String>()

        var old_ln = 0
        var new_ln = 0
        var tag: String
        var rest: String
        for (line in diff) {
            // Skip leading lines until after we've seen one starting with '+++'
            if (inPrelude) {
                if (line.startsWith("+++")) {
                    inPrelude = false
                }
                continue
            }
            val m = UNIFIED_DIFF_CHUNK_REGEXP.find(line)
            if (m != null) {
                // Process the lines in the previous chunk
                if (!rawChunk.isEmpty()) {
                    val oldChunkLines = ArrayList<String>()
                    val newChunkLines = ArrayList<String>()

                    for (raw_line in rawChunk) {
                        tag = raw_line[0]
                        rest = raw_line[1]
                        if (" " == tag || "-" == tag) {
                            oldChunkLines.add(rest)
                        }
                        if (" " == tag || "+" == tag) {
                            newChunkLines.add(rest)
                        }
                    }
                    patch.addDelta(ChangeDelta(Chunk(
                            old_ln - 1, oldChunkLines), Chunk(
                            new_ln - 1, newChunkLines)))
                    rawChunk.clear()
                }
                // Parse the @@ header
                old_ln = if (m.groups[1] == null) 1 else m.groupValues[1].toInt()
                new_ln = if (m.groups[3] == null) 1 else m.groupValues[3].toInt()

                if (old_ln == 0) {
                    old_ln = 1
                }
                if (new_ln == 0) {
                    new_ln = 1
                }
            } else {
                if (line.length > 0) {
                    tag = line.substring(0, 1)
                    rest = line.substring(1)
                    if (" " == tag || "+" == tag || "-" == tag) {
                        rawChunk.add(arrayOf(tag, rest))
                    }
                } else {
                    rawChunk.add(arrayOf(" ", ""))
                }
            }
        }

        // Process the lines in the last chunk
        if (!rawChunk.isEmpty()) {
            val oldChunkLines = ArrayList<String>()
            val newChunkLines = ArrayList<String>()

            for (raw_line in rawChunk) {
                tag = raw_line[0]
                rest = raw_line[1]
                if (" " == tag || "-" == tag) {
                    oldChunkLines.add(rest)
                }
                if (" " == tag || "+" == tag) {
                    newChunkLines.add(rest)
                }
            }

            patch.addDelta(ChangeDelta(Chunk(
                    old_ln - 1, oldChunkLines), Chunk(new_ln - 1,
                    newChunkLines)))
            rawChunk.clear()
        }

        return patch
    }

    /**
     * generateUnifiedDiff takes a Patch and some other arguments, returning the Unified Diff format text representing
     * the Patch.
     *
     * @param originalFileName - Filename of the original (unrevised file)
     * @param revisedFileName - Filename of the revised file
     * @param originalLines - Lines of the original file
     * @param patch - Patch created by the diff() function
     * @param contextSize - number of lines of context output around each difference in the file.
     * @return List of strings representing the Unified Diff representation of the Patch argument.
     * @author Bill James (tankerbay@gmail.com)
     */
    fun generateUnifiedDiff(originalFileName: String?,
                            revisedFileName: String?, originalLines: List<String>, patch: Patch<String>,
                            contextSize: Int): List<String> {
        if (patch.getDeltas().isNotEmpty()) {
            val ret = ArrayList<String>()
            ret.add("--- $originalFileName")
            ret.add("+++ $revisedFileName")

            val patchDeltas = ArrayList(
                    patch.getDeltas())

            // code outside the if block also works for single-delta issues.
            val deltas = ArrayList<AbstractDelta<String>>() // current
            // list
            // of
            // Delta's to
            // process
            var delta = patchDeltas[0]
            deltas.add(delta) // add the first Delta to the current set
            // if there's more than 1 Delta, we may need to output them together
            if (patchDeltas.size > 1) {
                for (i in 1 until patchDeltas.size) {
                    val position = delta.source.position // store
                    // the
                    // current
                    // position
                    // of
                    // the first Delta

                    // Check if the next Delta is too close to the current
                    // position.
                    // And if it is, add it to the current set
                    val nextDelta = patchDeltas[i]
                    if (position + delta.source.size() + contextSize >= nextDelta
                                    .source.position - contextSize) {
                        deltas.add(nextDelta)
                    } else {
                        // if it isn't, output the current set,
                        // then create a new set and add the current Delta to
                        // it.
                        val curBlock = processDeltas(originalLines,
                                deltas, contextSize)
                        ret.addAll(curBlock)
                        deltas.clear()
                        deltas.add(nextDelta)
                    }
                    delta = nextDelta
                }

            }
            // don't forget to process the last set of Deltas
            val curBlock = processDeltas(originalLines, deltas,
                    contextSize)
            ret.addAll(curBlock)
            return ret
        }
        return ArrayList()
    }

    /**
     * processDeltas takes a list of Deltas and outputs them together in a single block of Unified-Diff-format text.
     *
     * @param origLines - the lines of the original file
     * @param deltas - the Deltas to be output as a single block
     * @param contextSize - the number of lines of context to place around block
     * @return
     * @author Bill James (tankerbay@gmail.com)
     */
    private fun processDeltas(origLines: List<String>,
                              deltas: List<AbstractDelta<String>>, contextSize: Int): List<String> {
        val buffer = ArrayList<String>()
        var origTotal = 0 // counter for total lines output from Original
        var revTotal = 0 // counter for total lines output from Original
        var line: Int

        var curDelta = deltas[0]

        // NOTE: +1 to overcome the 0-offset Position
        var origStart = curDelta.source.position + 1 - contextSize
        if (origStart < 1) {
            origStart = 1
        }

        var revStart = curDelta.target.position + 1 - contextSize
        if (revStart < 1) {
            revStart = 1
        }

        // find the start of the wrapper context code
        var contextStart = curDelta.source.position - contextSize
        if (contextStart < 0) {
            contextStart = 0 // clamp to the start of the file
        }

        // output the context before the first Delta
        line = contextStart
        while (line < curDelta.source.position) { //
            buffer.add(" " + origLines[line])
            origTotal++
            revTotal++
            line++
        }

        // output the first Delta
        buffer.addAll(getDeltaText(curDelta))
        origTotal += curDelta.source.lines!!.size
        revTotal += curDelta.target.lines!!.size

        var deltaIndex = 1
        while (deltaIndex < deltas.size) { // for each of the other Deltas
            val nextDelta = deltas[deltaIndex]
            val intermediateStart = curDelta.source.position + curDelta.source.lines!!.size
            line = intermediateStart
            while (line < nextDelta.source
                            .position) {
                // output the code between the last Delta and this one
                buffer.add(" " + origLines[line])
                origTotal++
                revTotal++
                line++
            }
            buffer.addAll(getDeltaText(nextDelta)) // output the Delta
            origTotal += nextDelta.source.lines!!.size
            revTotal += nextDelta.target.lines!!.size
            curDelta = nextDelta
            deltaIndex++
        }

        // Now output the post-Delta context code, clamping the end of the file
        contextStart = curDelta.source.position + curDelta.source.lines!!.size
        line = contextStart
        while (line < contextStart + contextSize && line < origLines.size) {
            buffer.add(" " + origLines[line])
            origTotal++
            revTotal++
            line++
        }

        // Create and insert the block header, conforming to the Unified Diff
        // standard
        val header = StringBuilder()
        header.append("@@ -")
        header.append(origStart)
        header.append(",")
        header.append(origTotal)
        header.append(" +")
        header.append(revStart)
        header.append(",")
        header.append(revTotal)
        header.append(" @@")
        buffer.add(0, header.toString())

        return buffer
    }

    /**
     * getDeltaText returns the lines to be added to the Unified Diff text from the Delta parameter
     *
     * @param delta - the Delta to output
     * @return list of String lines of code.
     * @author Bill James (tankerbay@gmail.com)
     */
    private fun getDeltaText(delta: AbstractDelta<String>): List<String> {
        val buffer = ArrayList<String>()
        for (line in delta.source.lines!!) {
            buffer.add("-$line")
        }
        for (line in delta.target.lines!!) {
            buffer.add("+$line")
        }
        return buffer
    }
}
