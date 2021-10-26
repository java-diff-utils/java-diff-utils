/*
 * Copyright 2020 java-diff-utils.
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
package dev.gitlive.difflib.patch

import dev.gitlive.difflib.patch.PatchFailedException

/**
 * This delta contains equal lines of data. Therefore nothing is to do in applyTo and restore.
 * @author tobens
 */
class EqualDelta<T>(source: Chunk<T>, target: Chunk<T>) : AbstractDelta<T>(DeltaType.EQUAL, source, target) {
//    @Throws(PatchFailedException::class)
    override fun applyTo(target: MutableList<T>) {
    }

    override fun restore(target: MutableList<T>) {}
    override fun toString(): String {
        return ("[EqualDelta, position: " + source.position + ", lines: "
                + source.lines + "]")
    }

    override fun withChunks(original: Chunk<T>, revised: Chunk<T>): AbstractDelta<T> {
        return EqualDelta(original, revised)
    }
}