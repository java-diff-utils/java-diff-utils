package com.github.difflib;

import com.github.difflib.patch.ChangeDelta;
import com.github.difflib.patch.Chunk;
import com.github.difflib.patch.DeleteDelta;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.EqualDelta;
import com.github.difflib.patch.InsertDelta;
import com.github.difflib.patch.Patch;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static java.util.stream.Collectors.toList;
import java.util.zip.ZipFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DiffUtilsTest {

    @Test
    public void testDiff_Insert() {
        final Patch<String> patch = DiffUtils.diff(Arrays.asList("hhh"), Arrays.
                asList("hhh", "jjj", "kkk"));
        assertNotNull(patch);
        assertEquals(1, patch.getDeltas().size());
        final AbstractDelta<String> delta = patch.getDeltas().get(0);
        assertTrue(delta instanceof InsertDelta);
        assertEquals(new Chunk<>(1, Collections.<String>emptyList()), delta.getSource());
        assertEquals(new Chunk<>(1, Arrays.asList("jjj", "kkk")), delta.getTarget());
    }

    @Test
    public void testDiff_Delete() {
        final Patch<String> patch = DiffUtils.diff(Arrays.asList("ddd", "fff", "ggg"), Arrays.
                asList("ggg"));
        assertNotNull(patch);
        assertEquals(1, patch.getDeltas().size());
        final AbstractDelta<String> delta = patch.getDeltas().get(0);
        assertTrue(delta instanceof DeleteDelta);
        assertEquals(new Chunk<>(0, Arrays.asList("ddd", "fff")), delta.getSource());
        assertEquals(new Chunk<>(0, Collections.<String>emptyList()), delta.getTarget());
    }

    @Test
    public void testDiff_Change() {
        final List<String> changeTest_from = Arrays.asList("aaa", "bbb", "ccc");
        final List<String> changeTest_to = Arrays.asList("aaa", "zzz", "ccc");

        final Patch<String> patch = DiffUtils.diff(changeTest_from, changeTest_to);
        assertNotNull(patch);
        assertEquals(1, patch.getDeltas().size());
        final AbstractDelta<String> delta = patch.getDeltas().get(0);
        assertTrue(delta instanceof ChangeDelta);
        assertEquals(new Chunk<>(1, Arrays.asList("bbb")), delta.getSource());
        assertEquals(new Chunk<>(1, Arrays.asList("zzz")), delta.getTarget());
    }

    @Test
    public void testDiff_EmptyList() {
        final Patch<String> patch = DiffUtils.diff(new ArrayList<>(), new ArrayList<>());
        assertNotNull(patch);
        assertEquals(0, patch.getDeltas().size());
    }

    @Test
    public void testDiff_EmptyListWithNonEmpty() {
        final Patch<String> patch = DiffUtils.diff(new ArrayList<>(), Arrays.asList("aaa"));
        assertNotNull(patch);
        assertEquals(1, patch.getDeltas().size());
        final AbstractDelta<String> delta = patch.getDeltas().get(0);
        assertTrue(delta instanceof InsertDelta);
    }

    @Test
    public void testDiffInline() {
        final Patch<String> patch = DiffUtils.diffInline("", "test");
        assertEquals(1, patch.getDeltas().size());
        assertTrue(patch.getDeltas().get(0) instanceof InsertDelta);
        assertEquals(0, patch.getDeltas().get(0).getSource().getPosition());
        assertEquals(0, patch.getDeltas().get(0).getSource().getLines().size());
        assertEquals("test", patch.getDeltas().get(0).getTarget().getLines().get(0));
    }

    @Test
    public void testDiffInline2() {
        final Patch<String> patch = DiffUtils.diffInline("es", "fest");
        assertEquals(2, patch.getDeltas().size());
        assertTrue(patch.getDeltas().get(0) instanceof InsertDelta);
        assertEquals(0, patch.getDeltas().get(0).getSource().getPosition());
        assertEquals(2, patch.getDeltas().get(1).getSource().getPosition());
        assertEquals(0, patch.getDeltas().get(0).getSource().getLines().size());
        assertEquals(0, patch.getDeltas().get(1).getSource().getLines().size());
        assertEquals("f", patch.getDeltas().get(0).getTarget().getLines().get(0));
        assertEquals("t", patch.getDeltas().get(1).getTarget().getLines().get(0));
    }

    @Test
    public void testDiffIntegerList() {
        List<Integer> original = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> revised = Arrays.asList(2, 3, 4, 6);

        final Patch<Integer> patch = DiffUtils.diff(original, revised);

        for (AbstractDelta delta : patch.getDeltas()) {
            System.out.println(delta);
        }

        assertEquals(2, patch.getDeltas().size());
        assertEquals("[DeleteDelta, position: 0, lines: [1]]", patch.getDeltas().get(0).toString());
        assertEquals("[ChangeDelta, position: 4, lines: [5] to [6]]", patch.getDeltas().get(1).toString());
    }

    @Test
    public void testDiffMissesChangeForkDnaumenkoIssue31() {
        List<String> original = Arrays.asList("line1", "line2", "line3");
        List<String> revised = Arrays.asList("line1", "line2-2", "line4");

        Patch<String> patch = DiffUtils.diff(original, revised);
        assertEquals(1, patch.getDeltas().size());
        assertEquals("[ChangeDelta, position: 1, lines: [line2, line3] to [line2-2, line4]]", patch.getDeltas().get(0).toString());
    }

    /**
     * To test this, the greedy meyer algorithm is not suitable.
     */
    @Test
    @Disabled
    public void testPossibleDiffHangOnLargeDatasetDnaumenkoIssue26() throws IOException {
        ZipFile zip = new ZipFile(TestConstants.MOCK_FOLDER + "/large_dataset1.zip");

        Patch<String> patch = DiffUtils.diff(
                readStringListFromInputStream(zip.getInputStream(zip.getEntry("ta"))),
                readStringListFromInputStream(zip.getInputStream(zip.getEntry("tb"))));

        assertEquals(1, patch.getDeltas().size());
    }

    public static List<String> readStringListFromInputStream(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName(StandardCharsets.UTF_8.name())))) {

            return reader.lines().collect(toList());
        }
    }

    @Test
    public void testDiffMyersExample1() {
        final Patch<String> patch = DiffUtils.diff(Arrays.asList("A", "B", "C", "A", "B", "B", "A"), Arrays.asList("C", "B", "A", "B", "A", "C"));
        assertNotNull(patch);
        assertEquals(4, patch.getDeltas().size());
        assertEquals("Patch{deltas=[[DeleteDelta, position: 0, lines: [A, B]], [InsertDelta, position: 3, lines: [B]], [DeleteDelta, position: 5, lines: [B]], [InsertDelta, position: 7, lines: [C]]]}", patch.toString());
    }

    @Test
    public void testDiff_Equal() {
        final Patch<String> patch = DiffUtils.diff(
                Arrays.asList("hhh", "jjj", "kkk"),
                Arrays.asList("hhh", "jjj", "kkk"), true);
        assertNotNull(patch);
        assertEquals(1, patch.getDeltas().size());
        final AbstractDelta<String> delta = patch.getDeltas().get(0);
        assertTrue(delta instanceof EqualDelta);
        assertEquals(new Chunk<>(0, Arrays.asList("hhh", "jjj", "kkk")), delta.getSource());
        assertEquals(new Chunk<>(0, Arrays.asList("hhh", "jjj", "kkk")), delta.getTarget());
    }

    @Test
    public void testDiff_InsertWithEqual() {
        final Patch<String> patch = DiffUtils.diff(Arrays.asList("hhh"), Arrays.
                asList("hhh", "jjj", "kkk"), true);
        assertNotNull(patch);
        assertEquals(2, patch.getDeltas().size());

        AbstractDelta<String> delta = patch.getDeltas().get(0);
        assertTrue(delta instanceof EqualDelta);
        assertEquals(new Chunk<>(0, Arrays.asList("hhh")), delta.getSource());
        assertEquals(new Chunk<>(0, Arrays.asList("hhh")), delta.getTarget());

        delta = patch.getDeltas().get(1);
        assertTrue(delta instanceof InsertDelta);
        assertEquals(new Chunk<>(1, Collections.<String>emptyList()), delta.getSource());
        assertEquals(new Chunk<>(1, Arrays.asList("jjj", "kkk")), delta.getTarget());
    }

    @Test
    public void testDiff_ProblemIssue42() {
        final Patch<String> patch = DiffUtils.diff(
                Arrays.asList("The", "dog", "is", "brown"),
                Arrays.asList("The", "fox", "is", "down"), true);

        System.out.println(patch);
        assertNotNull(patch);
        assertEquals(4, patch.getDeltas().size());

        
        assertThat(patch.getDeltas()).extracting(d -> d.getType().name())
                .containsExactly("EQUAL", "CHANGE", "EQUAL", "CHANGE");
        
        AbstractDelta<String> delta = patch.getDeltas().get(0);
        assertTrue(delta instanceof EqualDelta);
        assertEquals(new Chunk<>(0, Arrays.asList("The")), delta.getSource());
        assertEquals(new Chunk<>(0, Arrays.asList("The")), delta.getTarget());

        delta = patch.getDeltas().get(1);
        assertTrue(delta instanceof ChangeDelta);
        assertEquals(new Chunk<>(1, Arrays.asList("dog")), delta.getSource());
        assertEquals(new Chunk<>(1, Arrays.asList("fox")), delta.getTarget());
        
        delta = patch.getDeltas().get(2);
        assertTrue(delta instanceof EqualDelta);
        assertEquals(new Chunk<>(2, Arrays.asList("is")), delta.getSource());
        assertEquals(new Chunk<>(2, Arrays.asList("is")), delta.getTarget());
        
        delta = patch.getDeltas().get(3);
        assertTrue(delta instanceof ChangeDelta);
        assertEquals(new Chunk<>(3, Arrays.asList("brown")), delta.getSource());
        assertEquals(new Chunk<>(3, Arrays.asList("down")), delta.getTarget());
    }
}
