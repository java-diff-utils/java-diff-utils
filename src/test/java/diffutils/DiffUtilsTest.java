package diffutils;

import difflib.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class DiffUtilsTest {

    @Test
    public void testDiff_Insert() {
        final Patch<String> patch = DiffUtils.diff(Arrays.asList("hhh"), Arrays.
                asList("hhh", "jjj", "kkk"));
        assertNotNull(patch);
        assertEquals(1, patch.getDeltas().size());
        final Delta<String> delta = patch.getDeltas().get(0);
        assertTrue(delta instanceof InsertDelta);
        assertEquals(new Chunk<>(1, Collections.<String>emptyList()), delta.getOriginal());
        assertEquals(new Chunk<>(1, Arrays.asList("jjj", "kkk")), delta.getRevised());
    }

    @Test
    public void testDiff_Delete() {
        final Patch<String> patch = DiffUtils.diff(Arrays.asList("ddd", "fff", "ggg"), Arrays.
                asList("ggg"));
        assertNotNull(patch);
        assertEquals(1, patch.getDeltas().size());
        final Delta<String> delta = patch.getDeltas().get(0);
        assertTrue(delta instanceof DeleteDelta);
        assertEquals(new Chunk<>(0, Arrays.asList("ddd", "fff")), delta.getOriginal());
        assertEquals(new Chunk<>(0, Collections.<String>emptyList()), delta.getRevised());
    }

    @Test
    public void testDiff_Change() {
        final List<String> changeTest_from = Arrays.asList("aaa", "bbb", "ccc");
        final List<String> changeTest_to = Arrays.asList("aaa", "zzz", "ccc");

        final Patch<String> patch = DiffUtils.diff(changeTest_from, changeTest_to);
        assertNotNull(patch);
        assertEquals(1, patch.getDeltas().size());
        final Delta<String> delta = patch.getDeltas().get(0);
        assertTrue(delta instanceof ChangeDelta);
        assertEquals(new Chunk<>(1, Arrays.asList("bbb")), delta.getOriginal());
        assertEquals(new Chunk<>(1, Arrays.asList("zzz")), delta.getRevised());
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
        final Delta<String> delta = patch.getDeltas().get(0);
        assertTrue(delta instanceof InsertDelta);
    }
    
    @Test
    public void testDiffInline() {
        final Patch<String> patch = DiffUtils.diffInline("", "test");
        assertEquals(1, patch.getDeltas().size());
        assertTrue(patch.getDeltas().get(0) instanceof InsertDelta);
        assertEquals(0, patch.getDeltas().get(0).getOriginal().getPosition());
        assertEquals(0, patch.getDeltas().get(0).getOriginal().getLines().size());
        assertEquals("test", patch.getDeltas().get(0).getRevised().getLines().get(0));
    }
    
    @Test
    public void testDiffInline2() {
        final Patch<String> patch = DiffUtils.diffInline("es", "fest");
        assertEquals(2, patch.getDeltas().size());
        assertTrue(patch.getDeltas().get(0) instanceof InsertDelta);
        assertEquals(0, patch.getDeltas().get(0).getOriginal().getPosition());
        assertEquals(2, patch.getDeltas().get(1).getOriginal().getPosition());
        assertEquals(0, patch.getDeltas().get(0).getOriginal().getLines().size());
        assertEquals(0, patch.getDeltas().get(1).getOriginal().getLines().size());
        assertEquals("f", patch.getDeltas().get(0).getRevised().getLines().get(0));
        assertEquals("t", patch.getDeltas().get(1).getRevised().getLines().get(0));
    }
}
