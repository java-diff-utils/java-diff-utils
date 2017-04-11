package difflib.text;

import difflib.algorithm.DiffException;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class DiffRowGeneratorTest {

    @Test
    public void testGenerator_Default() throws DiffException {
        String first = "anything \n \nother";
        String second = "anything\n\nother";

        DiffRowGenerator generator = DiffRowGenerator.create()
                .columnWidth(Integer.MAX_VALUE) // do not wrap
                .build();
        List<DiffRow> rows = generator.generateDiffRows(split(first), split(second));
        print(rows);

        assertEquals(3, rows.size());
    }

    @Test
    public void testGenerator_InlineDiff() throws DiffException {
        String first = "anything \n \nother";
        String second = "anything\n\nother";

        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .columnWidth(Integer.MAX_VALUE) // do not wrap
                .build();
        List<DiffRow> rows = generator.generateDiffRows(split(first), split(second));
        print(rows);

        assertEquals(3, rows.size());
        assertTrue(rows.get(0).getOldLine().indexOf("<span") > 0);
    }

    @Test
    public void testGenerator_IgnoreWhitespaces() throws DiffException {
        String first = "anything \n \nother\nmore lines";
        String second = "anything\n\nother\nsome more lines";

        DiffRowGenerator generator = DiffRowGenerator.create()
                .ignoreWhiteSpaces(true)
                .columnWidth(Integer.MAX_VALUE) // do not wrap
                .build();
        List<DiffRow> rows = generator.generateDiffRows(split(first), split(second));
        print(rows);

        assertEquals(4, rows.size());
        assertEquals(rows.get(0).getTag(), DiffRow.Tag.EQUAL);
        assertEquals(rows.get(1).getTag(), DiffRow.Tag.EQUAL);
        assertEquals(rows.get(2).getTag(), DiffRow.Tag.EQUAL);
        assertEquals(rows.get(3).getTag(), DiffRow.Tag.CHANGE);
    }

    private List<String> split(String content) {
        return Arrays.asList(content.split("\n"));
    }

    private void print(List<DiffRow> diffRows) {
        for (DiffRow row : diffRows) {
            System.out.println(row);
        }
    }
    
    @Test
    public void testGeneratorWithWordWrap() throws DiffException {
        String first = "anything \n \nother";
        String second = "anything\n\nother";

        DiffRowGenerator generator = DiffRowGenerator.create()
                .columnWidth(5) 
                .build();
        List<DiffRow> rows = generator.generateDiffRows(split(first), split(second));
        print(rows);

        assertEquals(3, rows.size());
        assertEquals("[CHANGE,anyth<br/>ing ,anyth<br/>ing]", rows.get(0).toString());
        assertEquals("[CHANGE, ,]", rows.get(1).toString());
        assertEquals("[EQUAL,other,other]", rows.get(2).toString());
    }
    
    @Test
    public void testGeneratorWithMerge() throws DiffException {
        String first = "anything \n \nother";
        String second = "anything\n\nother";

        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .build();
        List<DiffRow> rows = generator.generateDiffRows(split(first), split(second));
        print(rows);

        assertEquals(3, rows.size());
        assertEquals("[CHANGE,anything<span class=\"editOldInline\"> </span>,anything]", rows.get(0).toString());
        assertEquals("[CHANGE,<span class=\"editOldInline\"> </span>,]", rows.get(1).toString());
        assertEquals("[EQUAL,other,other]", rows.get(2).toString());
    }
    
    @Test
    public void testGeneratorWithMerge2() throws DiffException {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .build();
        List<DiffRow> rows = generator.generateDiffRows(Arrays.asList("Test"),Arrays.asList("ester"));
        print(rows);

        assertEquals(1, rows.size());
        assertEquals("[CHANGE,<span class=\"editOldInline\">T</span>est<span class=\"editNewInline\">er</span>,ester]", rows.get(0).toString());
    }
    
    @Test
    public void testGeneratorWithMerge3() throws DiffException {
        String first = "test\nanything \n \nother";
        String second = "anything\n\nother\ntest\ntest2";

        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .build();
        List<DiffRow> rows = generator.generateDiffRows(split(first), split(second));
        print(rows);

        assertEquals(6, rows.size());
        assertEquals("[CHANGE,<span class=\"editOldInline\">test,anything]", rows.get(0).toString());
        assertEquals("[CHANGE,</span>anything<span class=\"editOldInline\"> </span>,]", rows.get(1).toString());
        assertEquals("[CHANGE,<span class=\"editOldInline\"> </span>,]", rows.get(2).toString());
        assertEquals("[EQUAL,other,other]", rows.get(3).toString());
        assertEquals("[INSERT,<span class=\"editNewInline\">test</span>,test]", rows.get(4).toString());
        assertEquals("[INSERT,<span class=\"editNewInline\">test2</span>,test2]", rows.get(5).toString());
    }
    
    @Test
    public void testGeneratorWithMergeByWord4() throws DiffException {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .inlineDiffByWord(true)
                .build();
        List<DiffRow> rows = generator.generateDiffRows(Arrays.asList("Test"),Arrays.asList("ester"));
        print(rows);

        assertEquals(1, rows.size());
        assertEquals("[CHANGE,<span class=\"editOldInline\">Test</span><span class=\"editNewInline\">ester</span>,ester]", rows.get(0).toString());
    }
    
    @Test
    public void testGeneratorWithMergeByWord5() throws DiffException {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .inlineDiffByWord(true)
                .build();
        List<DiffRow> rows = generator.generateDiffRows(Arrays.asList("Test feature"),Arrays.asList("ester feature best"));
        print(rows);

        assertEquals(1, rows.size());
        assertEquals("[CHANGE,<span class=\"editOldInline\">Test</span><span class=\"editNewInline\">ester</span> <br/>feature<span class=\"editNewInline\"> best</span>,ester feature best]", rows.get(0).toString());
    }
    
    @Test
    public void testSplitString() {
        List<String> list = DiffRowGenerator.splitStringPreserveDelimiter("test,test2");
        assertEquals(3, list.size());
        assertEquals("[test, ,, test2]", list.toString());
    }
    
    @Test
    public void testSplitString2() {
        List<String> list = DiffRowGenerator.splitStringPreserveDelimiter("test , test2");
        System.out.println(list);
        assertEquals(5, list.size());
        assertEquals("[test,  , ,,  , test2]", list.toString());
    }
    
    @Test
    public void testSplitString3() {
        List<String> list = DiffRowGenerator.splitStringPreserveDelimiter("test,test2,");
        System.out.println(list);
        assertEquals(4, list.size());
        assertEquals("[test, ,, test2, ,]", list.toString());
    }
    
    
    @Test
    public void testGeneratorExample1() throws DiffException {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .inlineDiffByWord(true)
                .oldTag(f -> "~")
                .newTag(f -> "**")
                .build();
        List<DiffRow> rows = generator.generateDiffRows(
                Arrays.asList("This is a test senctence."),
                Arrays.asList("This is a test for diffutils."));
        
        System.out.println(rows.get(0).getOldLine());        
        
        assertEquals(1, rows.size());
        assertEquals("This is a test ~senctence~**for diffutils**.", rows.get(0).getOldLine());
    }
    
    @Test
    public void testGeneratorExample2() throws DiffException {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .oldTag(f -> "~")
                .newTag(f -> "**")
                .build();
        List<DiffRow> rows = generator.generateDiffRows(
                Arrays.asList("This is a test senctence.", "This is the second line.", "And here is the finish."),
                Arrays.asList("This is a test for diffutils.", "This is the second line."));
        
        System.out.println("|original|new|");
        System.out.println("|--------|---|");
        for (DiffRow row : rows) {
            System.out.println("|" + row.getOldLine() + "|" + row.getNewLine() + "|");
        }
        
        assertEquals(3, rows.size());
        assertEquals("This is a test ~senctence~.", rows.get(0).getOldLine());
        assertEquals("This is a test **for diffutils**.", rows.get(0).getNewLine());
    }
}
