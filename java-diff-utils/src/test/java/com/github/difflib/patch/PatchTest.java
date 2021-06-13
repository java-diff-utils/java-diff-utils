package com.github.difflib.patch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.github.difflib.DiffUtils;

public class PatchTest {

    @Test
    public void testPatch_Insert() {
        final List<String> insertTest_from = Arrays.asList("hhh");
        final List<String> insertTest_to = Arrays.asList("hhh", "jjj", "kkk", "lll");

        final Patch<String> patch = DiffUtils.diff(insertTest_from, insertTest_to);
        try {
            assertEquals(insertTest_to, DiffUtils.patch(insertTest_from, patch));
        } catch (PatchFailedException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testPatch_Delete() {
        final List<String> deleteTest_from = Arrays.asList("ddd", "fff", "ggg", "hhh");
        final List<String> deleteTest_to = Arrays.asList("ggg");

        final Patch<String> patch = DiffUtils.diff(deleteTest_from, deleteTest_to);
        try {
            assertEquals(deleteTest_to, DiffUtils.patch(deleteTest_from, patch));
        } catch (PatchFailedException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testPatch_Change() {
        final List<String> changeTest_from = Arrays.asList("aaa", "bbb", "ccc", "ddd");
        final List<String> changeTest_to = Arrays.asList("aaa", "bxb", "cxc", "ddd");

        final Patch<String> patch = DiffUtils.diff(changeTest_from, changeTest_to);
        try {
            assertEquals(changeTest_to, DiffUtils.patch(changeTest_from, patch));
        } catch (PatchFailedException e) {
            fail(e.getMessage());
        }
    }

    // region testPatch_fuzzyApply utils

    private List<String> intRange(int count) {
        return IntStream.range(0, count)
                .mapToObj(Integer::toString)
                .collect(Collectors.toList());
    }

    @SafeVarargs
    private final List<String> join(List<String>... lists) {
        return Arrays.stream(lists).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private static class FuzzyApplyTestPair {
        public final List<String> from;
        public final List<String> to;
        public final int requiredFuzz;

        private FuzzyApplyTestPair(List<String> from, List<String> to, int requiredFuzz) {
            this.from = from;
            this.to = to;
            this.requiredFuzz = requiredFuzz;
        }
    }

    // endregion

    @Test
    public void fuzzyApply() throws PatchFailedException {
        Patch<String> patch = new Patch<>();
        List<String> deltaFrom = Arrays.asList("aaa", "bbb", "ccc", "ddd", "eee", "fff");
        List<String> deltaTo = Arrays.asList("aaa", "bbb", "cxc", "dxd", "eee", "fff");
        patch.addDelta(new ChangeDelta<>(
                new Chunk<>(6, deltaFrom),
                new Chunk<>(6, deltaTo)));

        //noinspection unchecked
        List<String>[] moves = new List[] {
                intRange(6), // no patch move
                intRange(3), // forward patch move
                intRange(9), // backward patch move
                intRange(0), // apply to the first
        };

        for (FuzzyApplyTestPair pair : FUZZY_APPLY_TEST_PAIRS) {
            for (List<String> move : moves) {
                List<String> from = join(move, pair.from);
                List<String> to = join(move, pair.to);

                for (int i = 0; i < pair.requiredFuzz; i++) {
                    int maxFuzz = i;
                    assertThrows(PatchFailedException.class, () ->
                            patch.applyFuzzy(from, maxFuzz),
                            () -> "fail for " + from + " -> " + to + " for fuzz " + maxFuzz + " required " + pair.requiredFuzz);
                }
                for (int i = pair.requiredFuzz; i < 4; i++) {
                    int maxFuzz = i;
                    assertEquals(to, patch.applyFuzzy(from, maxFuzz),
                            () -> "with " + maxFuzz);
                }
            }
        }
    }

    @Test
    public void fuzzyApplyTwoSideBySidePatches() throws PatchFailedException {
        Patch<String> patch = new Patch<>();
        List<String> deltaFrom = Arrays.asList("aaa", "bbb", "ccc", "ddd", "eee", "fff");
        List<String> deltaTo = Arrays.asList("aaa", "bbb", "cxc", "dxd", "eee", "fff");
        patch.addDelta(new ChangeDelta<>(
                new Chunk<>(0, deltaFrom),
                new Chunk<>(0, deltaTo)));
        patch.addDelta(new ChangeDelta<>(
                new Chunk<>(6, deltaFrom),
                new Chunk<>(6, deltaTo)));


        assertEquals(join(deltaTo, deltaTo), patch.applyFuzzy(join(deltaFrom, deltaFrom), 0));
    }

    @Test
    public void fuzzyApplyToNearest() throws PatchFailedException {
        Patch<String> patch = new Patch<>();
        List<String> deltaFrom = Arrays.asList("aaa", "bbb", "ccc", "ddd", "eee", "fff");
        List<String> deltaTo = Arrays.asList("aaa", "bbb", "cxc", "dxd", "eee", "fff");
        patch.addDelta(new ChangeDelta<>(
                new Chunk<>(0, deltaFrom),
                new Chunk<>(0, deltaTo)));
        patch.addDelta(new ChangeDelta<>(
                new Chunk<>(10, deltaFrom),
                new Chunk<>(10, deltaTo)));

        assertEquals(join(deltaTo, deltaFrom, deltaTo),
                patch.applyFuzzy(join(deltaFrom, deltaFrom, deltaFrom), 0));
        assertEquals(join(intRange(1), deltaTo, deltaFrom, deltaTo),
                patch.applyFuzzy(join(intRange(1), deltaFrom, deltaFrom, deltaFrom), 0));
    }

    @Test
    public void testPatch_Serializable() throws IOException, ClassNotFoundException {
        final List<String> changeTest_from = Arrays.asList("aaa", "bbb", "ccc", "ddd");
        final List<String> changeTest_to = Arrays.asList("aaa", "bxb", "cxc", "ddd");

        final Patch<String> patch = DiffUtils.diff(changeTest_from, changeTest_to);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(patch);
        out.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bais);
        Patch<String> result = (Patch<String>) in.readObject();
        in.close();

        try {
            assertEquals(changeTest_to, DiffUtils.patch(changeTest_from, result));
        } catch (PatchFailedException e) {
            fail(e.getMessage());
        }

    }

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
            
            assertEquals(Arrays.asList("aaa", "<<<<<< HEAD", "bbb", "CDC", "======", "bbb", "ccc", ">>>>>>> PATCH", "ddd"), data);
            
        } catch (PatchFailedException e) {
            fail(e.getMessage());
        }
    }

    static class FuzzyApplyTestDataGenerator {
        private static String createList(List<String> values) {
            return values.stream()
                    .map(x -> '"' + x + '"')
                    .collect(Collectors.joining(", ", "Arrays.asList(", ")"));
        }

        public static void main(String[] args) {
            String[] deltaFrom = new String[] { "aaa", "bbb", "ccc", "ddd", "eee", "fff" };
            String[] deltaTo = new String[] { "aaa", "bbb", "cxc", "dxd", "eee", "fff" };

            List<FuzzyApplyTestPair> pairs = new ArrayList<>();

            // create test data.
            // Brute-force search
            String[] changedValue = new String[]{"axa", "bxb", "czc", "dzd", "exe", "fxf"};
            for (int i = 0; i < 1 << 6; i++) {
                if ((i & 0b001100) != 0 && (i & 0b001100) != 0b001100) {
                    continue;
                }

                String[] from = deltaFrom.clone();
                String[] to = deltaTo.clone();
                for (int j = 0; j < 6; j++) {
                    if ((i & (1 << j)) != 0) {
                        from[j] = changedValue[j];
                        to[j] = changedValue[j];
                    }
                }

                int requiredFuzz;
                if ((i & 0b001100) != 0) {
                    requiredFuzz = 3;
                } else if ((i & 0b010010) != 0) {
                    requiredFuzz = 2;
                } else if ((i & 0b100001) != 0) {
                    requiredFuzz = 1;
                } else {
                    requiredFuzz = 0;
                }

                pairs.add(new FuzzyApplyTestPair(Arrays.asList(from), Arrays.asList(to), requiredFuzz));
            }
            pairs.sort(Comparator.comparingInt(a -> a.requiredFuzz));
            System.out.println("FuzzyApplyTestPair[] pairs = new FuzzyApplyTestPair[] {");
            for (FuzzyApplyTestPair pair : pairs) {
                System.out.println("        new FuzzyApplyTestPair(");
                System.out.println("                " + createList(pair.from) + ",");
                System.out.println("                " + createList(pair.to) + ",");
                System.out.println("                " + pair.requiredFuzz + "),");
            }
            System.out.println("};");
        }
    }

    private static final FuzzyApplyTestPair[] FUZZY_APPLY_TEST_PAIRS = new FuzzyApplyTestPair[] {
            new FuzzyApplyTestPair(
                    Arrays.asList("aaa", "bbb", "ccc", "ddd", "eee", "fff"),
                    Arrays.asList("aaa", "bbb", "cxc", "dxd", "eee", "fff"),
                    0),
            new FuzzyApplyTestPair(
                    Arrays.asList("axa", "bbb", "ccc", "ddd", "eee", "fff"),
                    Arrays.asList("axa", "bbb", "cxc", "dxd", "eee", "fff"),
                    1),
            new FuzzyApplyTestPair(
                    Arrays.asList("aaa", "bbb", "ccc", "ddd", "eee", "fxf"),
                    Arrays.asList("aaa", "bbb", "cxc", "dxd", "eee", "fxf"),
                    1),
            new FuzzyApplyTestPair(
                    Arrays.asList("axa", "bbb", "ccc", "ddd", "eee", "fxf"),
                    Arrays.asList("axa", "bbb", "cxc", "dxd", "eee", "fxf"),
                    1),
            new FuzzyApplyTestPair(
                    Arrays.asList("aaa", "bxb", "ccc", "ddd", "eee", "fff"),
                    Arrays.asList("aaa", "bxb", "cxc", "dxd", "eee", "fff"),
                    2),
            new FuzzyApplyTestPair(
                    Arrays.asList("axa", "bxb", "ccc", "ddd", "eee", "fff"),
                    Arrays.asList("axa", "bxb", "cxc", "dxd", "eee", "fff"),
                    2),
            new FuzzyApplyTestPair(
                    Arrays.asList("aaa", "bbb", "ccc", "ddd", "exe", "fff"),
                    Arrays.asList("aaa", "bbb", "cxc", "dxd", "exe", "fff"),
                    2),
            new FuzzyApplyTestPair(
                    Arrays.asList("axa", "bbb", "ccc", "ddd", "exe", "fff"),
                    Arrays.asList("axa", "bbb", "cxc", "dxd", "exe", "fff"),
                    2),
            new FuzzyApplyTestPair(
                    Arrays.asList("aaa", "bxb", "ccc", "ddd", "exe", "fff"),
                    Arrays.asList("aaa", "bxb", "cxc", "dxd", "exe", "fff"),
                    2),
            new FuzzyApplyTestPair(
                    Arrays.asList("axa", "bxb", "ccc", "ddd", "exe", "fff"),
                    Arrays.asList("axa", "bxb", "cxc", "dxd", "exe", "fff"),
                    2),
            new FuzzyApplyTestPair(
                    Arrays.asList("aaa", "bxb", "ccc", "ddd", "eee", "fxf"),
                    Arrays.asList("aaa", "bxb", "cxc", "dxd", "eee", "fxf"),
                    2),
            new FuzzyApplyTestPair(
                    Arrays.asList("axa", "bxb", "ccc", "ddd", "eee", "fxf"),
                    Arrays.asList("axa", "bxb", "cxc", "dxd", "eee", "fxf"),
                    2),
            new FuzzyApplyTestPair(
                    Arrays.asList("aaa", "bbb", "ccc", "ddd", "exe", "fxf"),
                    Arrays.asList("aaa", "bbb", "cxc", "dxd", "exe", "fxf"),
                    2),
            new FuzzyApplyTestPair(
                    Arrays.asList("axa", "bbb", "ccc", "ddd", "exe", "fxf"),
                    Arrays.asList("axa", "bbb", "cxc", "dxd", "exe", "fxf"),
                    2),
            new FuzzyApplyTestPair(
                    Arrays.asList("aaa", "bxb", "ccc", "ddd", "exe", "fxf"),
                    Arrays.asList("aaa", "bxb", "cxc", "dxd", "exe", "fxf"),
                    2),
            new FuzzyApplyTestPair(
                    Arrays.asList("axa", "bxb", "ccc", "ddd", "exe", "fxf"),
                    Arrays.asList("axa", "bxb", "cxc", "dxd", "exe", "fxf"),
                    2),
            new FuzzyApplyTestPair(
                    Arrays.asList("aaa", "bbb", "czc", "dzd", "eee", "fff"),
                    Arrays.asList("aaa", "bbb", "czc", "dzd", "eee", "fff"),
                    3),
            new FuzzyApplyTestPair(
                    Arrays.asList("axa", "bbb", "czc", "dzd", "eee", "fff"),
                    Arrays.asList("axa", "bbb", "czc", "dzd", "eee", "fff"),
                    3),
            new FuzzyApplyTestPair(
                    Arrays.asList("aaa", "bxb", "czc", "dzd", "eee", "fff"),
                    Arrays.asList("aaa", "bxb", "czc", "dzd", "eee", "fff"),
                    3),
            new FuzzyApplyTestPair(
                    Arrays.asList("axa", "bxb", "czc", "dzd", "eee", "fff"),
                    Arrays.asList("axa", "bxb", "czc", "dzd", "eee", "fff"),
                    3),
            new FuzzyApplyTestPair(
                    Arrays.asList("aaa", "bbb", "czc", "dzd", "exe", "fff"),
                    Arrays.asList("aaa", "bbb", "czc", "dzd", "exe", "fff"),
                    3),
            new FuzzyApplyTestPair(
                    Arrays.asList("axa", "bbb", "czc", "dzd", "exe", "fff"),
                    Arrays.asList("axa", "bbb", "czc", "dzd", "exe", "fff"),
                    3),
            new FuzzyApplyTestPair(
                    Arrays.asList("aaa", "bxb", "czc", "dzd", "exe", "fff"),
                    Arrays.asList("aaa", "bxb", "czc", "dzd", "exe", "fff"),
                    3),
            new FuzzyApplyTestPair(
                    Arrays.asList("axa", "bxb", "czc", "dzd", "exe", "fff"),
                    Arrays.asList("axa", "bxb", "czc", "dzd", "exe", "fff"),
                    3),
            new FuzzyApplyTestPair(
                    Arrays.asList("aaa", "bbb", "czc", "dzd", "eee", "fxf"),
                    Arrays.asList("aaa", "bbb", "czc", "dzd", "eee", "fxf"),
                    3),
            new FuzzyApplyTestPair(
                    Arrays.asList("axa", "bbb", "czc", "dzd", "eee", "fxf"),
                    Arrays.asList("axa", "bbb", "czc", "dzd", "eee", "fxf"),
                    3),
            new FuzzyApplyTestPair(
                    Arrays.asList("aaa", "bxb", "czc", "dzd", "eee", "fxf"),
                    Arrays.asList("aaa", "bxb", "czc", "dzd", "eee", "fxf"),
                    3),
            new FuzzyApplyTestPair(
                    Arrays.asList("axa", "bxb", "czc", "dzd", "eee", "fxf"),
                    Arrays.asList("axa", "bxb", "czc", "dzd", "eee", "fxf"),
                    3),
            new FuzzyApplyTestPair(
                    Arrays.asList("aaa", "bbb", "czc", "dzd", "exe", "fxf"),
                    Arrays.asList("aaa", "bbb", "czc", "dzd", "exe", "fxf"),
                    3),
            new FuzzyApplyTestPair(
                    Arrays.asList("axa", "bbb", "czc", "dzd", "exe", "fxf"),
                    Arrays.asList("axa", "bbb", "czc", "dzd", "exe", "fxf"),
                    3),
            new FuzzyApplyTestPair(
                    Arrays.asList("aaa", "bxb", "czc", "dzd", "exe", "fxf"),
                    Arrays.asList("aaa", "bxb", "czc", "dzd", "exe", "fxf"),
                    3),
            new FuzzyApplyTestPair(
                    Arrays.asList("axa", "bxb", "czc", "dzd", "exe", "fxf"),
                    Arrays.asList("axa", "bxb", "czc", "dzd", "exe", "fxf"),
                    3),
    };
}
