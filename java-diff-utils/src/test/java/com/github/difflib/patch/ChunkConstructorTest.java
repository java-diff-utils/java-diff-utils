/*
 * Copyright 2009-2017 java-diff-utils.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChunkConstructorTest {

		@Test
		void arrayConstructorPreservesLinesAndPosition() {
				String[] lines = {"first", "second"};
				int position = 7;

				Chunk<String> chunk = new Chunk<>(position, lines);

				assertEquals(position, chunk.getPosition());
				assertEquals(Arrays.asList("first", "second"), chunk.getLines());
				assertEquals(lines.length, chunk.size());
				assertEquals(position + lines.length - 1, chunk.last());
				assertNull(chunk.getChangePosition());
		}

		@Test
		void arrayConstructorCopiesChangePositions() {
				String[] lines = {"first", "second", "third"};
				int position = 4;
				List<Integer> changePositions = new ArrayList<>(Arrays.asList(position, position + 2));

				Chunk<String> chunk = new Chunk<>(position, lines, changePositions);
				changePositions.clear();
				changePositions.add(position + 1);

				assertEquals(position, chunk.getPosition());
				assertEquals(Arrays.asList("first", "second", "third"), chunk.getLines());
				assertEquals(lines.length, chunk.size());
				assertEquals(position + lines.length - 1, chunk.last());
				assertEquals(Arrays.asList(position, position + 2), chunk.getChangePosition());
		}

		@Test
		void arrayConstructorAcceptsNullChangePositions() {
				Chunk<String> chunk = new Chunk<>(3, new String[] {"line"}, null);

				assertEquals(Arrays.asList("line"), chunk.getLines());
				assertNull(chunk.getChangePosition());
		}

		@Test
		void listConstructorCopiesLines() {
				List<String> lines = new ArrayList<>(Arrays.asList("first", "second"));
				int position = 5;
				int originalSize = lines.size();

				Chunk<String> chunk = new Chunk<>(position, lines);
				lines.set(0, "changed");
				lines.add("third");

				assertEquals(Arrays.asList("first", "second"), chunk.getLines());
				assertEquals(originalSize, chunk.size());
				assertEquals(position + originalSize - 1, chunk.last());
		}

		@Test
		void listConstructorCopiesChangePositions() {
				List<String> lines = new ArrayList<>(Arrays.asList("first", "second"));
				List<Integer> changePositions = new ArrayList<>(Arrays.asList(6, 7));

				Chunk<String> chunk = new Chunk<>(6, lines, changePositions);
				changePositions.clear();
				changePositions.add(8);

				assertEquals(Arrays.asList(6, 7), chunk.getChangePosition());
		}
}
