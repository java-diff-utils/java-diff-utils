package com.github.difflib;

import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

public class GenerateUnifiedDiffTest {

    public static List<String> fileToLines(String filename) throws FileNotFoundException, IOException {
        List<String> lines = new ArrayList<>();
        String line = "";
        try (BufferedReader in = new BufferedReader(new FileReader(filename))) {
            while ((line = in.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    @Test
    public void testGenerateUnified() throws DiffException, IOException {
        List<String> origLines = fileToLines(TestConstants.MOCK_FOLDER + "original.txt");
        List<String> revLines = fileToLines(TestConstants.MOCK_FOLDER + "revised.txt");

        verify(origLines, revLines, "original.txt", "revised.txt");
    }

    @Test
    public void testGenerateUnifiedWithOneDelta() throws DiffException, IOException {
        List<String> origLines = fileToLines(TestConstants.MOCK_FOLDER + "one_delta_test_original.txt");
        List<String> revLines = fileToLines(TestConstants.MOCK_FOLDER + "one_delta_test_revised.txt");

        verify(origLines, revLines, "one_delta_test_original.txt", "one_delta_test_revised.txt");
    }

    @Test
    public void testGenerateUnifiedDiffWithoutAnyDeltas() throws DiffException {
        List<String> test = Arrays.asList("abc");
        Patch<String> patch = DiffUtils.diff(test, test);
        UnifiedDiffUtils.generateUnifiedDiff("abc", "abc", test, patch, 0);
    }

    @Test
    public void testDiff_Issue10() throws IOException {
        final List<String> baseLines = fileToLines(TestConstants.MOCK_FOLDER + "issue10_base.txt");
        final List<String> patchLines = fileToLines(TestConstants.MOCK_FOLDER + "issue10_patch.txt");
        final Patch<String> p = UnifiedDiffUtils.parseUnifiedDiff(patchLines);
        try {
            DiffUtils.patch(baseLines, p);
        } catch (PatchFailedException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Issue 12
     */
    @Test
    public void testPatchWithNoDeltas() throws DiffException, IOException {
        final List<String> lines1 = fileToLines(TestConstants.MOCK_FOLDER + "issue11_1.txt");
        final List<String> lines2 = fileToLines(TestConstants.MOCK_FOLDER + "issue11_2.txt");
        verify(lines1, lines2, "issue11_1.txt", "issue11_2.txt");
    }

    @Test
    public void testDiff5() throws DiffException, IOException {
        final List<String> lines1 = fileToLines(TestConstants.MOCK_FOLDER + "5A.txt");
        final List<String> lines2 = fileToLines(TestConstants.MOCK_FOLDER + "5B.txt");
        verify(lines1, lines2, "5A.txt", "5B.txt");
    }

    /**
     * Issue 19
     */
    @Test
    public void testDiffWithHeaderLineInText() throws DiffException {
        List<String> original = new ArrayList<>();
        List<String> revised = new ArrayList<>();

        original.add("test line1");
        original.add("test line2");
        original.add("test line 4");
        original.add("test line 5");

        revised.add("test line1");
        revised.add("test line2");
        revised.add("@@ -2,6 +2,7 @@");
        revised.add("test line 4");
        revised.add("test line 5");

        Patch<String> patch = DiffUtils.diff(original, revised);
        List<String> udiff = UnifiedDiffUtils.generateUnifiedDiff("original", "revised",
                original, patch, 10);
        UnifiedDiffUtils.parseUnifiedDiff(udiff);
    }
    
    /**
     * Issue 47
     */
    @Test
    public void testNewFileCreation() throws DiffException {
        List<String> original = new ArrayList<>();
        List<String> revised = new ArrayList<>();

        revised.add("line1");
        revised.add("line2");

        Patch<String> patch = DiffUtils.diff(original, revised);
        List<String> udiff = UnifiedDiffUtils.generateUnifiedDiff(null, "revised",
                original, patch, 10);
        
        assertEquals("@@ -0,0 +1,2 @@", udiff.get(2));
        
        UnifiedDiffUtils.parseUnifiedDiff(udiff);
    }

    private void verify(List<String> origLines, List<String> revLines,
            String originalFile, String revisedFile) throws DiffException {
        Patch<String> patch = DiffUtils.diff(origLines, revLines);
        List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(originalFile, revisedFile,
                origLines, patch, 10);

        System.out.println(unifiedDiff.stream().collect(joining("\n")));

        Patch<String> fromUnifiedPatch = UnifiedDiffUtils.parseUnifiedDiff(unifiedDiff);
        List<String> patchedLines;
        try {
            patchedLines = fromUnifiedPatch.applyTo(origLines);
            assertEquals(revLines.size(), patchedLines.size());
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
