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
package com.github.difflib.patch;

import static com.github.difflib.patch.Patch.CONFLICT_PRODUCES_MERGE_CONFLICT;
import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.github.difflib.DiffUtils;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 *
 * @author tw
 */
public class PatchWithMyerDiffTest {

		@Test
		public void testPatch_Change_withExceptionProcessor() {
				final List<String> changeTest_from = Arrays.asList("aaa", "bbb", "ccc", "ddd");
				final List<String> changeTest_to = Arrays.asList("aaa", "bxb", "cxc", "ddd");

				final Patch<String> patch = DiffUtils.diff(changeTest_from, changeTest_to);

				changeTest_from.set(2, "CDC");

				patch.withConflictOutput(Patch.CONFLICT_PRODUCES_MERGE_CONFLICT);

				try {
						List<String> data = DiffUtils.patch(changeTest_from, patch);
						assertEquals(9, data.size());

						assertEquals(
										Arrays.asList("aaa", "<<<<<< HEAD", "bbb", "CDC", "======", "bbb", "ccc", ">>>>>>> PATCH", "ddd"),
										data);

				} catch (PatchFailedException e) {
						fail(e.getMessage());
				}
		}

		@Test
		public void testPatchThreeWayIssue138() throws PatchFailedException {
				List<String> base = Arrays.asList("Imagine there's no heaven".split("\\s+"));
				List<String> left = Arrays.asList("Imagine there's no HEAVEN".split("\\s+"));
				List<String> right = Arrays.asList("IMAGINE there's no heaven".split("\\s+"));

				Patch<String> rightPatch = DiffUtils.diff(base, right).withConflictOutput(CONFLICT_PRODUCES_MERGE_CONFLICT);

				List<String> applied = rightPatch.applyTo(left);

				assertEquals("IMAGINE there's no HEAVEN", applied.stream().collect(joining(" ")));
		}
}
