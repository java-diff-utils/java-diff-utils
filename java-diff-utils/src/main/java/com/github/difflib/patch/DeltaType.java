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
package com.github.difflib.patch;

/**
 * Specifies the type of the delta. There are three types of modifications from  
 * the original to get the revised text. 
 * 
 * CHANGE: a block of data of the original is replaced by another block of data.
 * DELETE: a block of data of the original is removed
 * INSERT: at a position of the original a block of data is inserted
 * 
 * to be complete there is also 
 * 
 * EQUAL: a block of data of original and the revised text is equal
 * 
 * which is no change at all.
 *
 */
public enum DeltaType {
    /**
     * A change in the original.
     */
    CHANGE,
    /**
     * A delete from the original.
     */
    DELETE,
    /**
     * An insert into the original.
     */
    INSERT,
    /**
     * An do nothing.
     */
    EQUAL
}
