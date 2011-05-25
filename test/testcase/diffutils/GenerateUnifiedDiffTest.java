package diffutils;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;
import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class GenerateUnifiedDiffTest extends TestCase {
    static final String FS = File.separator;

    public List<String> fileToLines(String filename) {
        List<String> lines = new LinkedList<String>();
        String line = "";
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            while ((line = in.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        return lines;
    }

    public void testGenerateUnified() {
        List<String> origLines = fileToLines("test" + FS + "mocks" + FS + "original.txt");
        List<String> revLines = fileToLines("test" + FS + "mocks" + FS + "revised.txt");

        verify(origLines, revLines);
    }

    public void testGenerateUnifiedWithOneDelta() {
        List<String> origLines = fileToLines("test" + FS + "mocks" + FS + "one_delta_test_original.txt");
        List<String> revLines = fileToLines("test" + FS + "mocks" + FS + "one_delta_test_revised.txt");

        verify(origLines, revLines);
    }

    public void testGenerateUnifiedDiffWithoutAnyDeltas() {
        List<String> test = Arrays.asList("abc");
        Patch patch = DiffUtils.diff(test, test);
        DiffUtils.generateUnifiedDiff("abc", "abc", test, patch, 0);
    }

    public void testDiff_Issue10() {
        final List<String> baseLines = fileToLines("test" + FS + "mocks" + FS + "issue10_base.txt");
        final List<String> patchLines = fileToLines("test" + FS + "mocks" + FS + "issue10_patch.txt");
        final Patch p = DiffUtils.parseUnifiedDiff(patchLines);
        try {
            DiffUtils.patch(baseLines, p);
        } catch (PatchFailedException e) {
            fail(e.getMessage());
        }
    }

    public void testDiff_Issue11() {
        final List<String> lines1 = fileToLines("test" + FS + "mocks" + FS + "issue11_1.txt");
        final List<String> lines2 = fileToLines("test" + FS + "mocks" + FS + "issue11_2.txt");
        verify(lines1, lines2);
    }

    public void testDiff5() {
        final List<String> lines1 = fileToLines("test" + FS + "mocks" + FS + "5A.txt");
        final List<String> lines2 = fileToLines("test" + FS + "mocks" + FS + "5B.txt");
        verify(lines1, lines2);
    }

    private void verify(List<String> origLines, List<String> revLines) {
        Patch p = DiffUtils.diff(origLines, revLines);
        List<String> unifiedDiff = DiffUtils.generateUnifiedDiff(
                "test" + FS + "mocks" + FS + "original.txt", "test" + FS + "mocks" + FS + "revised.txt", origLines, p, 10);

        Patch fromUnifiedPatch = DiffUtils.parseUnifiedDiff(unifiedDiff);
        List<String> patchedLines;
        try {
            patchedLines = (List<String>) fromUnifiedPatch.applyTo(origLines);
            assertTrue(revLines.size() == patchedLines.size());
            for (int i = 0; i < revLines.size(); i++) {
                String l1 = revLines.get(i);
                String l2 = patchedLines.get(i);
                if (!l1.equals(l2)) {
                    fail("Line " + (i + 1) + " of the patched file did not match the revised original");
                }
            }
        } catch (PatchFailedException e) {
            fail(e.getMessage());
        }
    }
}
