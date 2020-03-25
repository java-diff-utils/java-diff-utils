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
package live.git.difflib.algorithm

/**
 * Interface of a diff algorithm.
 *
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
interface DiffAlgorithmI<T> {

    /**
     * Computes the changeset to patch the source list to the target list.
     *
     * @param source source data
     * @param target target data
     * @param progress progress listener
     * @return
     * @throws DiffException
     */
//    @Throws(DiffException::class)
    fun computeDiff(source: List<T>, target: List<T>, progress: DiffAlgorithmListener?): List<Change>

    /**
     * Simple extension to compute a changeset using arrays.
     *
     * @param source
     * @param target
     * @param progress
     * @return
     * @throws live.git.difflib.algorithm.DiffException
     */
//    @Throws(DiffException::class)
    fun computeDiff(source: Array<T>, target: Array<T>, progress: DiffAlgorithmListener?): List<Change> {
        return computeDiff(source.toList(), target.toList(), progress)
    }
}
