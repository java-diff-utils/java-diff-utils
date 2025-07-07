package com.github.difflib.examples;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.fail;

import com.github.difflib.TestConstants;
import com.github.difflib.UnifiedDiffUtils;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class OriginalAndDiffTest {

		@Test
		public void testGenerateOriginalAndDiff() {
				List<String> origLines = null;
				List<String> revLines = null;
				try {
						origLines = fileToLines(TestConstants.MOCK_FOLDER + "original.txt");
						revLines = fileToLines(TestConstants.MOCK_FOLDER + "revised.txt");
				} catch (IOException e) {
						fail(e.getMessage());
				}

				List<String> originalAndDiff = UnifiedDiffUtils.generateOriginalAndDiff(origLines, revLines);
				System.out.println(originalAndDiff.stream().collect(joining("\n")));
		}

		@Test
		public void testGenerateOriginalAndDiffFirstLineChange() {
				List<String> origLines = null;
				List<String> revLines = null;
				try {
						origLines = fileToLines(TestConstants.MOCK_FOLDER + "issue_170_original.txt");
						revLines = fileToLines(TestConstants.MOCK_FOLDER + "issue_170_revised.txt");
				} catch (IOException e) {
						fail(e.getMessage());
				}

				List<String> originalAndDiff = UnifiedDiffUtils.generateOriginalAndDiff(origLines, revLines);
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
