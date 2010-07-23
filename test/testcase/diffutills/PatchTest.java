package testcase.diffutills;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import difflib.DiffUtils;
import difflib.Patch;

public class PatchTest extends TestCase {
    private List<String> changeTest_from = Arrays.asList("aaa", "bbb", "ccc", "ddd");
    private List<String> changeTest_to = Arrays.asList("aaa", "bxb", "cxc", "ddd");
    
    private List<String> deleteTest_from = Arrays.asList("ddd", "fff", "ggg", "hhh");
    private List<String> deleteTest_to = Arrays.asList("ggg");
    
    private List<String> insertTest_from = Arrays.asList("hhh");
    private List<String> insertTest_to = Arrays.asList("hhh", "jjj", "kkk", "lll");
    
    public void testPatch_Insert() {
        Patch patch = DiffUtils.diff(insertTest_from, insertTest_to);
        try {
            assertEquals(insertTest_to, DiffUtils.patch(insertTest_from, patch));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void testPatch_Delete() {
        Patch patch = DiffUtils.diff(deleteTest_from, deleteTest_to);
        try {
            assertEquals(deleteTest_to, DiffUtils.patch(deleteTest_from, patch));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void testPatch_Change() {
        Patch patch = DiffUtils.diff(changeTest_from, changeTest_to);
        try {
            assertEquals(changeTest_to, DiffUtils.patch(changeTest_from, patch));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
