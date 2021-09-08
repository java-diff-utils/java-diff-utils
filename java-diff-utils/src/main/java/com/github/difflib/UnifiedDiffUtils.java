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
package com.github.difflib;

import com.github.difflib.patch.ChangeDelta;
import com.github.difflib.patch.Chunk;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author toben
 */
public final class UnifiedDiffUtils {

    private static final Pattern UNIFIED_DIFF_CHUNK_REGEXP = Pattern
            .compile("^@@\\s+-(?:(\\d+)(?:,(\\d+))?)\\s+\\+(?:(\\d+)(?:,(\\d+))?)\\s+@@$");
    private static final String NULL_FILE_INDICATOR = "/dev/null";

    /**
     * Parse the given text in unified format and creates the list of deltas for it.
     *
     * @param diff the text in unified format
     * @return the patch with deltas.
     */
    public static Patch<String> parseUnifiedDiff(List<String> diff) {
        boolean inPrelude = true;
        List<String[]> rawChunk = new ArrayList<>();
        Patch<String> patch = new Patch<>();

        int old_ln = 0;
        int new_ln = 0;
        String tag;
        String rest;
        for (String line : diff) {
            // Skip leading lines until after we've seen one starting with '+++'
            if (inPrelude) {
                if (line.startsWith("+++")) {
                    inPrelude = false;
                }
                continue;
            }
            Matcher m = UNIFIED_DIFF_CHUNK_REGEXP.matcher(line);
            if (m.find()) {
                // Process the lines in the previous chunk
                processLinesInPrevChunk(rawChunk, patch, old_ln, new_ln);
                // Parse the @@ header
                old_ln = m.group(1) == null ? 1 : Integer.parseInt(m.group(1));
                new_ln = m.group(3) == null ? 1 : Integer.parseInt(m.group(3));

                if (old_ln == 0) {
                    old_ln = 1;
                }
                if (new_ln == 0) {
                    new_ln = 1;
                }
            } else {
                if (line.length() > 0) {
                    tag = line.substring(0, 1);
                    rest = line.substring(1);
                    if (" ".equals(tag) || "+".equals(tag) || "-".equals(tag)) {
                        rawChunk.add(new String[]{tag, rest});
                    }
                } else {
                    rawChunk.add(new String[]{" ", ""});
                }
            }
        }

        // Process the lines in the last chunk
        processLinesInPrevChunk(rawChunk, patch, old_ln, new_ln);

        return patch;
    }

    private static void processLinesInPrevChunk(List<String[]> rawChunk, Patch<String> patch, int old_ln, int new_ln) {
        String tag;
        String rest;
        if (!rawChunk.isEmpty()) {
            List<String> oldChunkLines = new ArrayList<>();
            List<String> newChunkLines = new ArrayList<>();

            List<Integer> removePosition = new ArrayList<>();
            List<Integer> addPosition = new ArrayList<>();
            int removeNum = 0;
            int addNum = 0;
            for (String[] raw_line : rawChunk) {
                tag = raw_line[0];
                rest = raw_line[1];
                if (" ".equals(tag) || "-".equals(tag)) {
                    removeNum++;
                    oldChunkLines.add(rest);
                    if ("-".equals(tag)) {
                        removePosition.add(old_ln - 1 + removeNum);
                    }
                }
                if (" ".equals(tag) || "+".equals(tag)) {
                    addNum++;
                    newChunkLines.add(rest);
                    if ("+".equals(tag)) {
                        addPosition.add(new_ln - 1 + addNum);
                    }
                }
            }
            patch.addDelta(new ChangeDelta<>(new Chunk<>(
                    old_ln - 1, oldChunkLines, removePosition), new Chunk<>(
                    new_ln - 1, newChunkLines, addPosition)));
            rawChunk.clear();
        }
    }

    /**
     * generateUnifiedDiff takes a Patch and some other arguments, returning the Unified Diff format
     * text representing the Patch.  Author: Bill James (tankerbay@gmail.com).
     *
     * @param originalFileName - Filename of the original (unrevised file)
     * @param revisedFileName - Filename of the revised file
     * @param originalLines - Lines of the original file
     * @param patch - Patch created by the diff() function
     * @param contextSize - number of lines of context output around each difference in the file.
     * @return List of strings representing the Unified Diff representation of the Patch argument.
     */
    public static List<String> generateUnifiedDiff(String originalFileName,
            String revisedFileName, List<String> originalLines, Patch<String> patch,
            int contextSize) {
        if (!patch.getDeltas().isEmpty()) {
            List<String> ret = new ArrayList<>();
            ret.add("--- " + Optional.ofNullable(originalFileName).orElse(NULL_FILE_INDICATOR));
            ret.add("+++ " + Optional.ofNullable(revisedFileName).orElse(NULL_FILE_INDICATOR));

            List<AbstractDelta<String>> patchDeltas = new ArrayList<>(
                    patch.getDeltas());

            // code outside the if block also works for single-delta issues.
            List<AbstractDelta<String>> deltas = new ArrayList<>(); // current
            // list
            // of
            // Delta's to
            // process
            AbstractDelta<String> delta = patchDeltas.get(0);
            deltas.add(delta); // add the first Delta to the current set
            // if there's more than 1 Delta, we may need to output them together
            if (patchDeltas.size() > 1) {
                for (int i = 1; i < patchDeltas.size(); i++) {
                    int position = delta.getSource().getPosition(); // store
                    // the
                    // current
                    // position
                    // of
                    // the first Delta

                    // Check if the next Delta is too close to the current
                    // position.
                    // And if it is, add it to the current set
                    AbstractDelta<String> nextDelta = patchDeltas.get(i);
                    if ((position + delta.getSource().size() + contextSize) >= (nextDelta
                            .getSource().getPosition() - contextSize)) {
                        deltas.add(nextDelta);
                    } else {
                        // if it isn't, output the current set,
                        // then create a new set and add the current Delta to
                        // it.
                        List<String> curBlock = processDeltas(originalLines,
                                deltas, contextSize, false);
                        ret.addAll(curBlock);
                        deltas.clear();
                        deltas.add(nextDelta);
                    }
                    delta = nextDelta;
                }

            }
            // don't forget to process the last set of Deltas
            List<String> curBlock = processDeltas(originalLines, deltas,
                    contextSize, patchDeltas.size() == 1 && originalFileName == null);
            ret.addAll(curBlock);
            return ret;
        }
        return new ArrayList<>();
    }

    /**
     * processDeltas takes a list of Deltas and outputs them together in a single block of
     * Unified-Diff-format text.  Author: Bill James (tankerbay@gmail.com).
     *
     * @param origLines - the lines of the original file
     * @param deltas - the Deltas to be output as a single block
     * @param contextSize - the number of lines of context to place around block
     * @return
     */
    private static List<String> processDeltas(List<String> origLines,
            List<AbstractDelta<String>> deltas, int contextSize, boolean newFile) {
        List<String> buffer = new ArrayList<>();
        int origTotal = 0; // counter for total lines output from Original
        int revTotal = 0; // counter for total lines output from Original
        int line;

        AbstractDelta<String> curDelta = deltas.get(0);
        int origStart;
        if (newFile) {
            origStart = 0;
        } else {
            // NOTE: +1 to overcome the 0-offset Position
            origStart = curDelta.getSource().getPosition() + 1 - contextSize;
            if (origStart < 1) {
                origStart = 1;
            }
        }

        int revStart = curDelta.getTarget().getPosition() + 1 - contextSize;
        if (revStart < 1) {
            revStart = 1;
        }

        // find the start of the wrapper context code
        int contextStart = curDelta.getSource().getPosition() - contextSize;
        if (contextStart < 0) {
            contextStart = 0; // clamp to the start of the file
        }

        // output the context before the first Delta
        for (line = contextStart; line < curDelta.getSource().getPosition(); line++) { //
            buffer.add(" " + origLines.get(line));
            origTotal++;
            revTotal++;
        }

        // output the first Delta
        buffer.addAll(getDeltaText(curDelta));
        origTotal += curDelta.getSource().getLines().size();
        revTotal += curDelta.getTarget().getLines().size();

        int deltaIndex = 1;
        while (deltaIndex < deltas.size()) { // for each of the other Deltas
            AbstractDelta<String> nextDelta = deltas.get(deltaIndex);
            int intermediateStart = curDelta.getSource().getPosition()
                    + curDelta.getSource().getLines().size();
            for (line = intermediateStart; line < nextDelta.getSource()
                    .getPosition(); line++) {
                // output the code between the last Delta and this one
                buffer.add(" " + origLines.get(line));
                origTotal++;
                revTotal++;
            }
            buffer.addAll(getDeltaText(nextDelta)); // output the Delta
            origTotal += nextDelta.getSource().getLines().size();
            revTotal += nextDelta.getTarget().getLines().size();
            curDelta = nextDelta;
            deltaIndex++;
        }

        // Now output the post-Delta context code, clamping the end of the file
        contextStart = curDelta.getSource().getPosition()
                + curDelta.getSource().getLines().size();
        for (line = contextStart; (line < (contextStart + contextSize))
                && (line < origLines.size()); line++) {
            buffer.add(" " + origLines.get(line));
            origTotal++;
            revTotal++;
        }

        // Create and insert the block header, conforming to the Unified Diff
        // standard
        StringBuilder header = new StringBuilder();
        header.append("@@ -");
        header.append(origStart);
        header.append(",");
        header.append(origTotal);
        header.append(" +");
        header.append(revStart);
        header.append(",");
        header.append(revTotal);
        header.append(" @@");
        buffer.add(0, header.toString());

        return buffer;
    }

    /**
     * getDeltaText returns the lines to be added to the Unified Diff text from the Delta parameter.  Author: Bill James (tankerbay@gmail.com).
     *
     * @param delta - the Delta to output
     * @return list of String lines of code.
     */
    private static List<String> getDeltaText(AbstractDelta<String> delta) {
        List<String> buffer = new ArrayList<>();
        for (String line : delta.getSource().getLines()) {
            buffer.add("-" + line);
        }
        for (String line : delta.getTarget().getLines()) {
            buffer.add("+" + line);
        }
        return buffer;
    }

    private UnifiedDiffUtils() {
    }
}
