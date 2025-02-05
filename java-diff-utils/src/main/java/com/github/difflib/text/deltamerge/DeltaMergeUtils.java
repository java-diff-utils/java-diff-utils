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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.ChangeDelta;
import com.github.difflib.patch.Chunk;

/**
 * Provides utility features for merge inline deltas
 *
 * @author <a href="christian.meier@epictec.ch">Christian Meier</a>
 */
final public class DeltaMergeUtils {

    public static List<AbstractDelta<String>> mergeInlineDeltas(InlineDeltaMergeInfo deltaMergeInfo,
            Predicate<List<String>> replaceEquality) {
        final List<AbstractDelta<String>> originalDeltas = deltaMergeInfo.getDeltas();
        if (originalDeltas.size() < 2) {
            return originalDeltas;
        }

        final List<AbstractDelta<String>> newDeltas = new ArrayList<>();
        newDeltas.add(originalDeltas.get(0));
        for (int i = 1; i < originalDeltas.size(); i++) {
            final AbstractDelta<String> previousDelta = newDeltas.get(newDeltas.size()-1);
            final AbstractDelta<String> currentDelta = originalDeltas.get(i);

            final List<String> equalities = deltaMergeInfo.getOrigList().subList(
                    previousDelta.getSource().getPosition() + previousDelta.getSource().size(),
                    currentDelta.getSource().getPosition());

            if (replaceEquality.test(equalities)) {
                // Merge the previous delta, the equality and the current delta into one
                // ChangeDelta and replace the previous delta by this new ChangeDelta.
                final List<String> allSourceLines = new ArrayList<>();
                allSourceLines.addAll(previousDelta.getSource().getLines());
                allSourceLines.addAll(equalities);
                allSourceLines.addAll(currentDelta.getSource().getLines());

                final List<String> allTargetLines = new ArrayList<>();
                allTargetLines.addAll(previousDelta.getTarget().getLines());
                allTargetLines.addAll(equalities);
                allTargetLines.addAll(currentDelta.getTarget().getLines());

                final ChangeDelta<String> replacement = new ChangeDelta<>(
                        new Chunk<>(previousDelta.getSource().getPosition(), allSourceLines),
                        new Chunk<>(previousDelta.getTarget().getPosition(), allTargetLines));

                newDeltas.remove(newDeltas.size()-1);
                newDeltas.add(replacement);
            } else {
                newDeltas.add(currentDelta);
            }
        }

        return newDeltas;
    }

    private DeltaMergeUtils() {
    }
}
