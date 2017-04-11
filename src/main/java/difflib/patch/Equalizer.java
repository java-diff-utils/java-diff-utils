/*-
 * #%L
 * java-diff-utils
 * %%
 * Copyright (C) 2009 - 2017 java-diff-utils
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */
package difflib.patch;

/**
 * Specifies when two compared elements are equal.
 *
 * @param T The type of the compared elements in the 'lines'.
 */
public interface Equalizer<T> {

    /**
     * Indicates if two elements are equal according to the diff mechanism.
     *
     * @param original The original element. Must not be {@code null}.
     * @param revised The revised element. Must not be {@code null}.
     * @return Returns true if the elements are equal.
     */
    boolean equals(T original, T revised);
}
