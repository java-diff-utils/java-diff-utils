package com.github.difflib.patch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ChunkTest {
		@Test
		void verifyChunk() throws PatchFailedException {
				Chunk<Character> chunk = new Chunk<>(7, toCharList("test"));

				// normal check
				assertEquals(VerifyChunk.OK, chunk.verifyChunk(toCharList("prefix test suffix")));
				assertEquals(
								VerifyChunk.CONTENT_DOES_NOT_MATCH_TARGET, chunk.verifyChunk(toCharList("prefix  es  suffix"), 0, 7));

				// position
				assertEquals(VerifyChunk.OK, chunk.verifyChunk(toCharList("short test suffix"), 0, 6));
				assertEquals(VerifyChunk.OK, chunk.verifyChunk(toCharList("loonger test suffix"), 0, 8));
				assertEquals(
								VerifyChunk.CONTENT_DOES_NOT_MATCH_TARGET, chunk.verifyChunk(toCharList("prefix test suffix"), 0, 6));
				assertEquals(
								VerifyChunk.CONTENT_DOES_NOT_MATCH_TARGET, chunk.verifyChunk(toCharList("prefix test suffix"), 0, 8));

				// fuzz
				assertEquals(VerifyChunk.OK, chunk.verifyChunk(toCharList("prefix test suffix"), 1, 7));
				assertEquals(VerifyChunk.OK, chunk.verifyChunk(toCharList("prefix  es  suffix"), 1, 7));
				assertEquals(
								VerifyChunk.CONTENT_DOES_NOT_MATCH_TARGET, chunk.verifyChunk(toCharList("prefix      suffix"), 1, 7));
		}

		@Test
		void verifyChunkAtTheEndOfTheTarget() throws PatchFailedException {
				// chunk covers target indices 7..10, so it needs a target of at least 11
				Chunk<Character> chunk = new Chunk<>(7, toCharList("test"));

				assertEquals(VerifyChunk.OK, chunk.verifyChunk(toCharList("prefix test"), 0, 7));
				assertEquals(VerifyChunk.POSITION_OUT_OF_TARGET, chunk.verifyChunk(toCharList("prefix tes"), 0, 7));
				assertEquals(VerifyChunk.POSITION_OUT_OF_TARGET, chunk.verifyChunk(toCharList("prefix te"), 0, 7));
		}

		@Test
		void arrayConstructorCopiesSourceLines() throws PatchFailedException {
				String[] source = {"old"};
				String[] revised = {"new"};
				Patch<String> patch = new Patch<>();
				patch.addDelta(new ChangeDelta<>(new Chunk<>(0, source), new Chunk<>(0, revised)));
				source[0] = "changed source";
				revised[0] = "changed revision";
				assertEquals(Arrays.asList("new"), patch.applyTo(Arrays.asList("old")));
		}

		@Test
		void arrayConstructorCopiesLinesWithChangePositions() throws PatchFailedException {
				String[] source = {"old"};
				String[] revised = {"new"};
				Patch<String> patch = new Patch<>();
				patch.addDelta(
								new ChangeDelta<>(new Chunk<>(0, source, Arrays.asList(0)), new Chunk<>(0, revised, Arrays.asList(0))));
				source[0] = "changed source";
				revised[0] = "changed revision";
				assertEquals(Arrays.asList("new"), patch.applyTo(Arrays.asList("old")));
		}

		@Test
		void arrayConstructorCopiesRevisedLines() throws PatchFailedException {
				String[] revised = {"new"};
				Patch<String> patch = new Patch<>();
				patch.addDelta(new ChangeDelta<>(new Chunk<>(0, new String[] {"old"}), new Chunk<>(0, revised)));
				revised[0] = "changed revision";
				assertEquals(Arrays.asList("new"), patch.applyTo(Arrays.asList("old")));
		}

		private List<Character> toCharList(String str) {
				return str.chars().mapToObj(x -> (char) x).collect(Collectors.toList());
		}
}
