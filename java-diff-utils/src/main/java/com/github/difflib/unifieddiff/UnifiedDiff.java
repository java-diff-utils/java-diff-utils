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

import com.github.difflib.patch.PatchFailedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 *
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
public final class UnifiedDiff {

    private String header;
    private String tail;
    private final List<UnifiedDiffFile> files = new ArrayList<>();

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    void addFile(UnifiedDiffFile file) {
        files.add(file);
    }

    public List<UnifiedDiffFile> getFiles() {
        return Collections.unmodifiableList(files);
    }

    void setTailTxt(String tailTxt) {
        this.tail = tailTxt;
    }

    public String getTail() {
        return tail;
    }

    public List<String> applyPatchTo(Predicate<String> findFile, List<String> originalLines) throws PatchFailedException {
        UnifiedDiffFile file = files.stream()
                .filter(diff -> findFile.test(diff.getFromFile()))
                .findFirst().orElse(null);
        if (file != null) {
            return file.getPatch().applyTo(originalLines);
        } else {
            return originalLines;
        }
    }

    public static UnifiedDiff from(String header, String tail, UnifiedDiffFile... files) {
        UnifiedDiff diff = new UnifiedDiff();
        diff.setHeader(header);
        diff.setTailTxt(tail);
        for (UnifiedDiffFile file : files) {
            diff.addFile(file);
        }
        return diff;
    }
}
