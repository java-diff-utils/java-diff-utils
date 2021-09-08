/*
 * Copyright 2021 java-diff-utils.
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

import java.util.function.BiPredicate;

/**
 * Tool to create new instances of a diff algorithm. This one is only needed at the moment to 
 * set DiffUtils default diff algorithm.
 * @author tw
 */
public interface DiffAlgorithmFactory {
    <T> DiffAlgorithmI<T> create();
    
    <T> DiffAlgorithmI<T> create(BiPredicate<T, T> equalizer);
}
