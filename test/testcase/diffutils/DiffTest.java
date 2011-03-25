package diffutils;

import difflib.*;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DiffTest extends TestCase {

    public void testDiff_Insert() throws Exception {
        final Patch patch = DiffUtils.diff(Arrays.asList("hhh"), Arrays.asList("hhh", "jjj", "kkk"));
        assertNotNull(patch);
        assertEquals(1, patch.getDeltas().size());
        final Delta delta = patch.getDeltas().get(0);
        assertEquals(InsertDelta.class, delta.getClass());
        assertEquals(new Chunk(1, Collections.EMPTY_LIST), delta.getOriginal());
        assertEquals(new Chunk(1, Arrays.asList("jjj", "kkk")), delta.getRevised());
    }

    public void testDiff_Delete() throws Exception {
        final Patch patch = DiffUtils.diff(Arrays.asList("ddd", "fff", "ggg"), Arrays.asList("ggg"));
        assertNotNull(patch);
        assertEquals(1, patch.getDeltas().size());
        final Delta delta = patch.getDeltas().get(0);
        assertEquals(DeleteDelta.class, delta.getClass());
        assertEquals(new Chunk(0, Arrays.asList("ddd", "fff")), delta.getOriginal());
        assertEquals(new Chunk(0, Collections.EMPTY_LIST), delta.getRevised());
    }

    public void testDiff_Change() throws Exception {
        final List<String> changeTest_from = Arrays.asList("aaa", "bbb", "ccc");
        final List<String> changeTest_to = Arrays.asList("aaa", "zzz", "ccc");

        final Patch patch = DiffUtils.diff(changeTest_from, changeTest_to);
        assertNotNull(patch);
        assertEquals(1, patch.getDeltas().size());
        final Delta delta = patch.getDeltas().get(0);
        assertEquals(ChangeDelta.class, delta.getClass());
        assertEquals(new Chunk(1, Arrays.asList("bbb")), delta.getOriginal());
        assertEquals(new Chunk(1, Arrays.asList("zzz")), delta.getRevised());
    }

    public void testDiff_EmptyList() throws Exception {
        final Patch patch = DiffUtils.diff(new ArrayList<String>(), new ArrayList<String>());
        assertNotNull(patch);
        assertEquals(0, patch.getDeltas().size());
    }

    public void testDiff_EmptyListWithNonEmpty() throws Exception {
        final Patch patch = DiffUtils.diff(new ArrayList<String>(), Arrays.asList("aaa"));
        assertNotNull(patch);
        assertEquals(1, patch.getDeltas().size());
        final Delta delta = patch.getDeltas().get(0);
        assertEquals(InsertDelta.class, delta.getClass());
    }
}
