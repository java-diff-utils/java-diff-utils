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
/**
 * This is the new implementation of UnifiedDiff Tools. This version is multi file aware.
 * <p/>
 * To read a unified diff file you should use {@link UnifiedDiffReader#parseUnifiedDiff}.
 * You will get a {@link UnifiedDiff} that holds all informations about the
 * diffs and the files.
 * <p/>
 * To process the UnifiedDiff use {@link UnifiedDiffWriter#write}.
 */
package com.github.difflib.unifieddiff;
