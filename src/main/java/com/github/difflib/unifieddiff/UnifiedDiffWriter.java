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

import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
public class UnifiedDiffWriter {

    public static void write(UnifiedDiff diff, Writer writer) throws IOException {
        writer.write(diff.getHeader());

        for (UnifiedDiffFile file : diff.getFiles()) {
            writeOrNothing(writer, file.getDiffCommand());
            if (file.getIndex() != null) {
                writer.write("index " + file.getIndex() + "\n");
            }
            if (file.getFromFile() != null) {
                writer.write("--- " + file.getFromFile() + "\n");
            }
            if (file.getToFile() != null) {
                writer.write("+++ " + file.getToFile() + "\n");
            }

        }
        if (diff.getTail() != null) {
            writer.write("--\n" + diff.getTail());
        }
    }

    private static void writeOrNothing(Writer writer, String str) throws IOException {
        if (str != null) {
            writer.append(str).append("\n");
        }
    }
}
