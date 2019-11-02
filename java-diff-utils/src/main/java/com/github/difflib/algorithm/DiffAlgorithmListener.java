/*
 * Copyright 2018 java-diff-utils.
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

/**
 *
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
public interface DiffAlgorithmListener {
    void diffStart();
    
    /**
     * This is a step within the diff algorithm. Due to different implementations the value
     * is not strict incrementing to the max and is not garantee to reach the max. It could
     * stop before.
     * @param value
     * @param max 
     */
    void diffStep(int value, int max);
    void diffEnd();
}
