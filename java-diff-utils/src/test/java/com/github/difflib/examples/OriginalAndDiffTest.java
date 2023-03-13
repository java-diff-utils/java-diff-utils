package com.github.difflib.examples;

import com.github.difflib.TestConstants;
import com.github.difflib.UnifiedDiffUtils;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class OriginalAndDiffTest {

    @Test
    public void testGenerateOriginalAndDiff() throws  IOException {
        List<String> origLines = fileToLines(TestConstants.MOCK_FOLDER + "original.txt");
        List<String> revLines = fileToLines(TestConstants.MOCK_FOLDER + "revised.txt");

        List<String> originalAndDiff = UnifiedDiffUtils.generateOriginalAndDiff(origLines,revLines);
        System.out.println(originalAndDiff.stream().collect(joining("\n")));
    }

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


}
