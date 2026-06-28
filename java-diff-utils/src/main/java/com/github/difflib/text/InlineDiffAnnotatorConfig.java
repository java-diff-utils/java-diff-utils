package com.github.difflib.text;

import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.text.DiffRow.Tag;
import com.github.difflib.text.deltamerge.InlineDeltaMergeInfo;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Immutable value object carrying all configuration that {@link InlineDiffAnnotator} needs
 * to annotate a changed delta with inline markup.
 *
 * <p>Instances are created once per {@link DiffRowGenerator} construction and reused
 * for every delta processed.
 */
public final class InlineDiffAnnotatorConfig {

		final boolean reportLinesUnchanged;
		final Function<String, String> lineNormalizer;
		final Function<String, List<String>> inlineDiffSplitter;
		final BiPredicate<String, String> equalizer;
		final Function<InlineDeltaMergeInfo, List<AbstractDelta<String>>> inlineDeltaMerger;
		final BiFunction<Tag, Boolean, String> oldTag;
		final BiFunction<Tag, Boolean, String> newTag;
		final Function<String, String> processDiffs;
		final boolean mergeOriginalRevised;
		final boolean replaceOriginalLinefeedInChangesWithSpaces;
		final int columnWidth;

		InlineDiffAnnotatorConfig(
						boolean reportLinesUnchanged,
						Function<String, String> lineNormalizer,
						Function<String, List<String>> inlineDiffSplitter,
						BiPredicate<String, String> equalizer,
						Function<InlineDeltaMergeInfo, List<AbstractDelta<String>>> inlineDeltaMerger,
						BiFunction<Tag, Boolean, String> oldTag,
						BiFunction<Tag, Boolean, String> newTag,
						Function<String, String> processDiffs,
						boolean mergeOriginalRevised,
						boolean replaceOriginalLinefeedInChangesWithSpaces,
						int columnWidth) {
				this.reportLinesUnchanged = reportLinesUnchanged;
				this.lineNormalizer = lineNormalizer;
				this.inlineDiffSplitter = inlineDiffSplitter;
				this.equalizer = equalizer;
				this.inlineDeltaMerger = inlineDeltaMerger;
				this.oldTag = oldTag;
				this.newTag = newTag;
				this.processDiffs = processDiffs;
				this.mergeOriginalRevised = mergeOriginalRevised;
				this.replaceOriginalLinefeedInChangesWithSpaces = replaceOriginalLinefeedInChangesWithSpaces;
				this.columnWidth = columnWidth;
		}
}
