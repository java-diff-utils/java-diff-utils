package com.github.difflib.unifieddiff;

import com.github.difflib.DiffUtils;
import com.github.difflib.TestConstants;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

public class UnifiedDiffRoundTripTest {

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
    public void testGenerateUnifiedDiffWithoutAnyDeltas() throws DiffException, IOException {
        List<String> test = Arrays.asList("abc");
        Patch<String> patch = DiffUtils.diff(test, test);
        StringWriter writer = new StringWriter();

        UnifiedDiffWriter.write(
                UnifiedDiff.from("header", "tail", UnifiedDiffFile.from("abc", "abc", patch)),
                name -> test,
                writer, 0);

        System.out.println(writer);
    }

    @Test
    public void testDiff_Issue10() throws IOException {
        final List<String> baseLines = fileToLines(TestConstants.MOCK_FOLDER + "issue10_base.txt");
        final List<String> patchLines = fileToLines(TestConstants.MOCK_FOLDER + "issue10_patch.txt");

        UnifiedDiff unifiedDiff = UnifiedDiffReader.parseUnifiedDiff(
                new ByteArrayInputStream(patchLines.stream().collect(joining("\n")).getBytes())
        );

        final Patch<String> p = unifiedDiff.getFiles().get(0).getPatch();
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
    public void testDiffWithHeaderLineInText() throws DiffException, IOException {
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
        StringWriter writer = new StringWriter();
        UnifiedDiffWriter.write(
                UnifiedDiff.from("header", "tail", UnifiedDiffFile.from("original", "revised", patch)),
                name -> original,
                writer, 10);

        System.out.println(writer.toString());

        UnifiedDiff unifiedDiff = UnifiedDiffReader.parseUnifiedDiff(new ByteArrayInputStream(writer.toString().getBytes()));
    }

    private void verify(List<String> origLines, List<String> revLines,
            String originalFile, String revisedFile) throws DiffException, IOException {
        Patch<String> patch = DiffUtils.diff(origLines, revLines);

        StringWriter writer = new StringWriter();
        UnifiedDiffWriter.write(
                UnifiedDiff.from("header", "tail", UnifiedDiffFile.from(originalFile, revisedFile, patch)),
                name -> origLines,
                writer, 10);

        System.out.println(writer.toString());

        UnifiedDiff unifiedDiff = UnifiedDiffReader.parseUnifiedDiff(new ByteArrayInputStream(writer.toString().getBytes()));

        List<String> patchedLines;
        try {
//            if (unifiedDiff.getFiles().isEmpty()) {
//                patchedLines = new ArrayList<>(origLines);
//            } else {
//                Patch<String> fromUnifiedPatch = unifiedDiff.getFiles().get(0).getPatch();
//                patchedLines = fromUnifiedPatch.applyTo(origLines);
//            }
            patchedLines = unifiedDiff.spplyPatchTo(file -> originalFile.equals(file), origLines);
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
