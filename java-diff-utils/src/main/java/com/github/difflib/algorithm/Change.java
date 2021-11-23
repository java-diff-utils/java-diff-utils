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
package com.github.difflib.algorithm;

import com.github.difflib.patch.DeltaType;

/**
 *
 * @author <a href="t.warneke@gmx.net">Tobias Warneke</a>
 */
public class Change {

    public final DeltaType deltaType;
    public final int startOriginal;
    public final int endOriginal;
    public final int startRevised;
    public final int endRevised;

    public Change(DeltaType deltaType, int startOriginal, int endOriginal, int startRevised, int endRevised) {
        this.deltaType = deltaType;
        this.startOriginal = startOriginal;
        this.endOriginal = endOriginal;
        this.startRevised = startRevised;
        this.endRevised = endRevised;
    }
    
    public Change withEndOriginal(int endOriginal) {
        return new Change(deltaType, startOriginal, endOriginal, startRevised, endRevised);
    }
    
    public Change withEndRevised(int endRevised) {
        return new Change(deltaType, startOriginal, endOriginal, startRevised, endRevised);
    }
}
