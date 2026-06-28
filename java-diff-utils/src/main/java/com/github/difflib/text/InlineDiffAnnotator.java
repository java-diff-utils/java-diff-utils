package com.github.difflib.text;

import static java.util.stream.Collectors.toList;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Chunk;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.text.DiffRow.Tag;
import com.github.difflib.text.deltamerge.InlineDeltaMergeInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Applies character- or word-level inline diff markup to a single {@link AbstractDelta},
 * producing the annotated {@link DiffRow} list ready for side-by-side display.
 *
 * <p>This is a single-responsibility helper: it runs a sub-diff on the token lists,
 * merges adjacent inline deltas, and delegates tag insertion to {@link InlineTagRenderer}.
 * All rendering decisions (which HTML/markup tags to use, how to handle line-feeds, etc.)
 * are supplied by the caller through {@link InlineDiffAnnotatorConfig}.
 */
public final class InlineDiffAnnotator {

		private InlineDiffAnnotator() {}

		/**
		 * Generates the inline-annotated {@link DiffRow}s for a single changed delta.
		 *
		 * @param delta  the CHANGE delta to annotate. Must not be {@code null}.
		 * @param config all rendering and splitting configuration. Must not be {@code null}.
		 * @return the list of {@link DiffRow}s with inline markup applied.
		 */
		public static List<DiffRow> annotate(AbstractDelta<String> delta, InlineDiffAnnotatorConfig config) {

				List<String> orig = normalizeLines(delta.getSource().getLines(), config);
				List<String> rev = normalizeLines(delta.getTarget().getLines(), config);

				String joinedOrig = String.join("\n", orig);
				String joinedRev = String.join("\n", rev);

				List<String> origList = config.inlineDiffSplitter.apply(joinedOrig);
				List<String> revList = config.inlineDiffSplitter.apply(joinedRev);

				List<AbstractDelta<String>> originalInlineDeltas =
								DiffUtils.diff(origList, revList, config.equalizer).getDeltas();
				List<AbstractDelta<String>> inlineDeltas =
								config.inlineDeltaMerger.apply(new InlineDeltaMergeInfo(originalInlineDeltas, origList, revList));

				Collections.reverse(inlineDeltas);
				for (AbstractDelta<String> inlineDelta : inlineDeltas) {
						Chunk<String> inlineOrig = inlineDelta.getSource();
						Chunk<String> inlineRev = inlineDelta.getTarget();
						if (inlineDelta.getType() == DeltaType.DELETE) {
								InlineTagRenderer.wrapInTag(
												origList,
												inlineOrig.getPosition(),
												inlineOrig.getPosition() + inlineOrig.size(),
												Tag.DELETE,
												config.oldTag,
												config.processDiffs,
												config.replaceOriginalLinefeedInChangesWithSpaces && config.mergeOriginalRevised);
						} else if (inlineDelta.getType() == DeltaType.INSERT) {
								if (config.mergeOriginalRevised) {
										origList.addAll(
														inlineOrig.getPosition(),
														revList.subList(inlineRev.getPosition(), inlineRev.getPosition() + inlineRev.size()));
										InlineTagRenderer.wrapInTag(
														origList,
														inlineOrig.getPosition(),
														inlineOrig.getPosition() + inlineRev.size(),
														Tag.INSERT,
														config.newTag,
														config.processDiffs,
														false);
								} else {
										InlineTagRenderer.wrapInTag(
														revList,
														inlineRev.getPosition(),
														inlineRev.getPosition() + inlineRev.size(),
														Tag.INSERT,
														config.newTag,
														config.processDiffs,
														false);
								}
						} else if (inlineDelta.getType() == DeltaType.CHANGE) {
								if (config.mergeOriginalRevised) {
										origList.addAll(
														inlineOrig.getPosition() + inlineOrig.size(),
														revList.subList(inlineRev.getPosition(), inlineRev.getPosition() + inlineRev.size()));
										InlineTagRenderer.wrapInTag(
														origList,
														inlineOrig.getPosition() + inlineOrig.size(),
														inlineOrig.getPosition() + inlineOrig.size() + inlineRev.size(),
														Tag.CHANGE,
														config.newTag,
														config.processDiffs,
														false);
								} else {
										InlineTagRenderer.wrapInTag(
														revList,
														inlineRev.getPosition(),
														inlineRev.getPosition() + inlineRev.size(),
														Tag.CHANGE,
														config.newTag,
														config.processDiffs,
														false);
								}
								InlineTagRenderer.wrapInTag(
												origList,
												inlineOrig.getPosition(),
												inlineOrig.getPosition() + inlineOrig.size(),
												Tag.CHANGE,
												config.oldTag,
												config.processDiffs,
												config.replaceOriginalLinefeedInChangesWithSpaces && config.mergeOriginalRevised);
						}
				}

				StringBuilder origResult = new StringBuilder();
				StringBuilder revResult = new StringBuilder();
				for (String character : origList) {
						origResult.append(character);
				}
				for (String character : revList) {
						revResult.append(character);
				}

				List<String> originalLines = Arrays.asList(origResult.toString().split("\n"));
				List<String> revisedLines = Arrays.asList(revResult.toString().split("\n"));
				List<DiffRow> diffRows = new ArrayList<>();
				for (int j = 0; j < Math.max(originalLines.size(), revisedLines.size()); j++) {
						diffRows.add(buildDiffRowWithoutNormalizing(
										Tag.CHANGE,
										originalLines.size() > j ? originalLines.get(j) : "",
										revisedLines.size() > j ? revisedLines.get(j) : "",
										config.columnWidth));
				}
				return diffRows;
		}

		/**
		 * Applies the line normalizer from the config to every line in the list, unless
		 * {@code reportLinesUnchanged} is set in which case the list is returned as-is.
		 */
		private static List<String> normalizeLines(List<String> list, InlineDiffAnnotatorConfig config) {
				return config.reportLinesUnchanged
								? list
								: list.stream().map(config.lineNormalizer::apply).collect(toList());
		}

		/** Wraps the text at {@code columnWidth} and returns a plain {@link DiffRow}. */
		private static DiffRow buildDiffRowWithoutNormalizing(Tag type, String orgline, String newline, int columnWidth) {
				return new DiffRow(
								type, StringUtils.wrapText(orgline, columnWidth), StringUtils.wrapText(newline, columnWidth));
		}
}
