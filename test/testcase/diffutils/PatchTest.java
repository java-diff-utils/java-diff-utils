package diffutils;

import difflib.DiffUtils;
import difflib.Patch;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

public class PatchTest extends TestCase {

    public void testPatch_Insert() throws Exception {
        final List<String> insertTest_from = Arrays.asList("hhh");
        final List<String> insertTest_to = Arrays.asList("hhh", "jjj", "kkk", "lll");

        final Patch patch = DiffUtils.diff(insertTest_from, insertTest_to);
        assertEquals(insertTest_to, DiffUtils.patch(insertTest_from, patch));
    }

    public void testPatch_Delete() throws Exception {
        final List<String> deleteTest_from = Arrays.asList("ddd", "fff", "ggg", "hhh");
        final List<String> deleteTest_to = Arrays.asList("ggg");

        final Patch patch = DiffUtils.diff(deleteTest_from, deleteTest_to);
        assertEquals(deleteTest_to, DiffUtils.patch(deleteTest_from, patch));
    }

    public void testPatch_Change() throws Exception {
        final List<String> changeTest_from = Arrays.asList("aaa", "bbb", "ccc", "ddd");
        final List<String> changeTest_to = Arrays.asList("aaa", "bxb", "cxc", "ddd");

        final Patch patch = DiffUtils.diff(changeTest_from, changeTest_to);
        assertEquals(changeTest_to, DiffUtils.patch(changeTest_from, patch));
    }
}
