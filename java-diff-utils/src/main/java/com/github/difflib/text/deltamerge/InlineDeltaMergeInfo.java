/*
 * Copyright 2009-2024 java-diff-utils.
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
package com.github.difflib.text.deltamerge;

import java.util.List;

import com.github.difflib.patch.AbstractDelta;

/**
 * Holds the information required to merge deltas originating from an inline
 * diff
 *
 * @author <a href="christian.meier@epictec.ch">Christian Meier</a>
 */
public final class InlineDeltaMergeInfo {

    private final List<AbstractDelta<String>> deltas;
    private final List<String> origList;
    private final List<String> revList;

    public InlineDeltaMergeInfo(List<AbstractDelta<String>> deltas, List<String> origList, List<String> revList) {
        this.deltas = deltas;
        this.origList = origList;
        this.revList = revList;
    }

    public List<AbstractDelta<String>> getDeltas() {
        return deltas;
    }

    public List<String> getOrigList() {
        return origList;
    }

    public List<String> getRevList() {
        return revList;
    }
}
