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

import com.github.difflib.patch.Patch;

/**
 *
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
public final class UnifiedDiffFile {

    private String diffCommand;
    private String fromFile;
    private String fromTimestamp;
    private String toFile;
    private String toTimestamp;
    private String index;
    private String newFileMode;
    private String deletedFileMode;
    private Patch<String> patch = new Patch<>();

    public String getDiffCommand() {
        return diffCommand;
    }

    public void setDiffCommand(String diffCommand) {
        this.diffCommand = diffCommand;
    }

    public String getFromFile() {
        return fromFile;
    }

    public void setFromFile(String fromFile) {
        this.fromFile = fromFile;
    }

    public String getToFile() {
        return toFile;
    }

    public void setToFile(String toFile) {
        this.toFile = toFile;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getIndex() {
        return index;
    }

    public Patch<String> getPatch() {
        return patch;
    }

    public String getFromTimestamp() {
        return fromTimestamp;
    }

    public void setFromTimestamp(String fromTimestamp) {
        this.fromTimestamp = fromTimestamp;
    }

    public String getToTimestamp() {
        return toTimestamp;
    }

    public void setToTimestamp(String toTimestamp) {
        this.toTimestamp = toTimestamp;
    }
    
    

    public static UnifiedDiffFile from(String fromFile, String toFile, Patch<String> patch) {
        UnifiedDiffFile file = new UnifiedDiffFile();
        file.setFromFile(fromFile);
        file.setToFile(toFile);
        file.patch = patch;
        return file;
    }

    public void setNewFileMode(String newFileMode) {
        this.newFileMode = newFileMode;
    }
    
    public String getNewFileMode() {
        return newFileMode;
    }

    public String getDeletedFileMode() {
        return deletedFileMode;
    }

    public void setDeletedFileMode(String deletedFileMode) {
        this.deletedFileMode = deletedFileMode;
    }
}
