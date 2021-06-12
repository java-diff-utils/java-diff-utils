package com.github.difflib.patch;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChunkTest {
    @Test
    void verifyChunk() throws PatchFailedException {
        Chunk<Character> chunk = new Chunk<>(7, toCharList("test"));

        // normal check
        assertEquals(VerifyChunk.OK,
                chunk.verifyChunk(toCharList("prefix test suffix")));
        assertEquals(VerifyChunk.CONTENT_DOES_NOT_MATCH_TARGET,
                chunk.verifyChunk(toCharList("prefix  es  suffix"), 0, 7));

        // position
        assertEquals(VerifyChunk.OK,
                chunk.verifyChunk(toCharList("short test suffix"), 0, 6));
        assertEquals(VerifyChunk.OK,
                chunk.verifyChunk(toCharList("loonger test suffix"), 0, 8));
        assertEquals(VerifyChunk.CONTENT_DOES_NOT_MATCH_TARGET,
                chunk.verifyChunk(toCharList("prefix test suffix"), 0, 6));
        assertEquals(VerifyChunk.CONTENT_DOES_NOT_MATCH_TARGET,
                chunk.verifyChunk(toCharList("prefix test suffix"), 0, 8));

        // fuzz
        assertEquals(VerifyChunk.OK,
                chunk.verifyChunk(toCharList("prefix test suffix"), 1, 7));
        assertEquals(VerifyChunk.OK,
                chunk.verifyChunk(toCharList("prefix  es  suffix"), 1, 7));
        assertEquals(VerifyChunk.CONTENT_DOES_NOT_MATCH_TARGET,
                chunk.verifyChunk(toCharList("prefix      suffix"), 1, 7));
    }

    private List<Character> toCharList(String str) {
        return str.chars().mapToObj(x -> (char) x).collect(Collectors.toList());
    }
}
