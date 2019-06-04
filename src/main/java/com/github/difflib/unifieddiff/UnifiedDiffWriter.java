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
package com.github.difflib.unifieddiff;

import com.github.difflib.patch.AbstractDelta;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @todo use an instance to store contextSize and originalLinesProvider.
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
public class UnifiedDiffWriter {

    private static final Logger LOG = Logger.getLogger(UnifiedDiffWriter.class.getName());

    public static void write(UnifiedDiff diff, Function<String, List<String>> originalLinesProvider, Writer writer, int contextSize) throws IOException {
        write(diff, originalLinesProvider, line -> {
            try {
                writer.append(line).append("\n");
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }, contextSize);
    }

    public static void write(UnifiedDiff diff, Function<String, List<String>> originalLinesProvider, Consumer<String> writer, int contextSize) throws IOException {
        writer.accept(diff.getHeader());

        for (UnifiedDiffFile file : diff.getFiles()) {
            writeOrNothing(writer, file.getDiffCommand());
            if (file.getIndex() != null) {
                writer.accept("index " + file.getIndex());
            }
            if (file.getFromFile() != null) {
                writer.accept("--- " + file.getFromFile());
            }
            if (file.getToFile() != null) {
                writer.accept("+++ " + file.getToFile());
            }

            List<String> originalLines = originalLinesProvider.apply(file.getFromFile());

            List<AbstractDelta<String>> patchDeltas = new ArrayList<>(
                    file.getPatch().getDeltas());

            if (!patchDeltas.isEmpty()) {
                List<AbstractDelta<String>> deltas = new ArrayList<>();

                AbstractDelta<String> delta = patchDeltas.get(0);
                deltas.add(delta); // add the first Delta to the current set
                // if there's more than 1 Delta, we may need to output them together
                if (patchDeltas.size() > 1) {
                    for (int i = 1; i < patchDeltas.size(); i++) {
                        int position = delta.getSource().getPosition();

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
                            processDeltas(writer, originalLines, deltas, contextSize);
                            deltas.clear();
                            deltas.add(nextDelta);
                        }
                        delta = nextDelta;
                    }

                }
                // don't forget to process the last set of Deltas
                processDeltas(writer, originalLines, deltas, contextSize);
            }

        }
        if (diff.getTail() != null) {
            writer.accept("--");
            writer.accept(diff.getTail());
        }
    }

    private static void processDeltas(Consumer<String> writer,
            List<String> origLines, List<AbstractDelta<String>> deltas,
            int contextSize) {
        List<String> buffer = new ArrayList<>();
        int origTotal = 0; // counter for total lines output from Original
        int revTotal = 0; // counter for total lines output from Original
        int line;

        AbstractDelta<String> curDelta = deltas.get(0);

        // NOTE: +1 to overcome the 0-offset Position
        int origStart = curDelta.getSource().getPosition() + 1;
        if (origStart < 1) {
            origStart = 1;
        }

        int revStart = curDelta.getTarget().getPosition() + 1;
        if (revStart < 1) {
            revStart = 1;
        }

        // find the start of the wrapper context code
        int contextStart = curDelta.getSource().getPosition();
        if (contextStart < 0) {
            contextStart = 0; // clamp to the start of the file
        }

//        // output the context before the first Delta
//        for (line = contextStart; line < curDelta.getSource().getPosition(); line++) { //
//            buffer.add(" " + curDelta.getSource().getLines().get(line - contextStart));
//            origTotal++;
//            revTotal++;
//        }
        // output the first Delta
        getDeltaText(txt -> buffer.add(txt), curDelta);
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
            getDeltaText(txt -> buffer.add(txt), nextDelta); // output the Delta
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
        writer.accept("@@ -" + origStart + "," + origTotal + " +" + revStart + "," + revTotal + " @@");
        buffer.forEach(txt -> {
            writer.accept(txt);
        });
    }

    /**
     * getDeltaText returns the lines to be added to the Unified Diff text from the Delta parameter
     *
     * @param delta - the Delta to output
     * @return list of String lines of code.
     * @author Bill James (tankerbay@gmail.com)
     */
    private static void getDeltaText(Consumer<String> writer, AbstractDelta<String> delta) {
        for (String line : delta.getSource().getLines()) {
            writer.accept("-" + line);
        }
        for (String line : delta.getTarget().getLines()) {
            writer.accept("+" + line);
        }
    }

    private static void writeOrNothing(Consumer<String> writer, String str) throws IOException {
        if (str != null) {
            writer.accept(str);
        }
    }
}
