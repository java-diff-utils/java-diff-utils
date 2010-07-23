package testcase.diffutills;

import java.util.*;

import junit.framework.TestCase;
import difflib.*;

public class DiffTest extends TestCase {
    private List<String> changeTest_from = Arrays.asList("aaa", "bbb", "ccc");
    private List<String> changeTest_to = Arrays.asList("aaa", "zzz", "ccc");
    
    private List<String> deleteTest_from = Arrays.asList("ddd", "fff", "ggg");
    private List<String> deleteTest_to = Arrays.asList("ggg");
    
    private List<String> insertTest_from = Arrays.asList("hhh");
    private List<String> insertTest_to = Arrays.asList("hhh", "jjj", "kkk");
    
    @SuppressWarnings("serial")
    public void testDiff_Insert() {
        Patch patch = DiffUtils.diff(insertTest_from, insertTest_to);
        assertNotNull(patch);
        assertEquals(patch.getDeltas().size(), 1);
        Delta delta = patch.getDeltas().get(0);
        assertEquals(InsertDelta.class, delta.getClass());
        assertEquals(new Chunk(1, 0, new LinkedList<String>()), delta.getOriginal());
        assertEquals(new Chunk(1, 2, new LinkedList<String>() {
            {
                add("jjj");
                add("kkk");
            }
        }), delta.getRevised());
    }
    
    @SuppressWarnings("serial")
    public void testDiff_Delete() {
        Patch patch = DiffUtils.diff(deleteTest_from, deleteTest_to);
        assertNotNull(patch);
        assertEquals(patch.getDeltas().size(), 1);
        Delta delta = patch.getDeltas().get(0);
        assertEquals(DeleteDelta.class, delta.getClass());
        assertEquals(new Chunk(0, 2, new LinkedList<String>() {
            {
                add("ddd");
                add("fff");
            }
        }), delta.getOriginal());
        assertEquals(new Chunk(0, 0, new LinkedList<String>()), delta.getRevised());
    }
    
    @SuppressWarnings("serial")
    public void testDiff_Change() {
        Patch patch = DiffUtils.diff(changeTest_from, changeTest_to);
        assertNotNull(patch);
        assertEquals(patch.getDeltas().size(), 1);
        Delta delta = patch.getDeltas().get(0);
        assertEquals(ChangeDelta.class, delta.getClass());
        assertEquals(new Chunk(1, 1, new LinkedList<String>() {
            {
                add("bbb");
            }
        }), delta.getOriginal());
        assertEquals(new Chunk(1, 1, new LinkedList<String>() {
            {
                add("zzz");
            }
        }), delta.getRevised());
    }
}
