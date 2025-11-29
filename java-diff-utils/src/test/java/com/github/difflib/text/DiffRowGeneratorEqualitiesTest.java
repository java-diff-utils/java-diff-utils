package com.github.difflib.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class DiffRowGeneratorEqualitiesTest {

		@Test
		public void testDefaultEqualityProcessingLeavesTextUnchanged() {
				DiffRowGenerator generator =
								DiffRowGenerator.create().showInlineDiffs(false).build();

				List<DiffRow> rows = generator.generateDiffRows(Arrays.asList("hello world"), Arrays.asList("hello world"));

				assertEquals(1, rows.size());
				assertEquals("hello world", rows.get(0).getOldLine());
				assertEquals("hello world", rows.get(0).getNewLine());
				assertEquals(DiffRow.Tag.EQUAL, rows.get(0).getTag());
		}

		@Test
		public void testCustomEqualityProcessingIsApplied() {
				DiffRowGenerator generator = DiffRowGenerator.create()
								.showInlineDiffs(false)
								.processEqualities(text -> "[" + text + "]")
								.build();

				List<DiffRow> rows = generator.generateDiffRows(Arrays.asList("A", "B"), Arrays.asList("A", "B"));

				assertEquals(2, rows.size());
				assertEquals("[A]", rows.get(0).getOldLine());
				assertEquals("[B]", rows.get(1).getOldLine());
		}

		/**
		 * Verifies that processEqualities can be used to HTML-escape unchanged
		 * lines while still working together with the default HTML-oriented
		 * lineNormalizer.
		 */
		@Test
		public void testHtmlEscapingEqualitiesWorksWithDefaultNormalizer() {
				DiffRowGenerator generator = DiffRowGenerator.create()
								.showInlineDiffs(true)
								.inlineDiffByWord(true)
								.processEqualities(s -> s.replace("<", "&lt;").replace(">", "&gt;"))
								.build();

				// both lines are equal -> Tag.EQUAL, processEqualities is invoked
				List<DiffRow> rows = generator.generateDiffRows(Arrays.asList("hello <world>"), Arrays.asList("hello <world>"));

				DiffRow row = rows.get(0);

				assertTrue(row.getOldLine().contains("&lt;world&gt;"));
				assertTrue(row.getNewLine().contains("&lt;world&gt;"));
		}

		/**
		 * Ensures equalities are processed while inline diff markup is still
		 * present somewhere in the line.
		 */
		@Test
		public void testEqualitiesProcessedButInlineDiffStillPresent() {
				DiffRowGenerator generator = DiffRowGenerator.create()
								.showInlineDiffs(true)
								.inlineDiffByWord(true)
								.processEqualities(s -> "(" + s + ")")
								.build();

				List<DiffRow> rows = generator.generateDiffRows(Arrays.asList("hello world"), Arrays.asList("hello there"));

				DiffRow row = rows.get(0);

				System.out.println("OLD = " + row.getOldLine());
				System.out.println("NEW = " + row.getNewLine());

				// Row must be CHANGE
				assertEquals(DiffRow.Tag.CHANGE, row.getTag());

				// Inline diff markup must appear
				assertTrue(
								row.getOldLine().contains("span") || row.getNewLine().contains("span"),
								"Expected inline <span> diff markup in old or new line");

				// Equalities inside CHANGE row must NOT be wrapped by processEqualities
				// Option 3 does NOT modify inline equalities
				assertTrue(row.getOldLine().startsWith("hello "), "Equal (unchanged) inline segment should remain unchanged");
		}
}
