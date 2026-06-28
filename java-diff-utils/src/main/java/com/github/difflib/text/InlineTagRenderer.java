package com.github.difflib.text;

import com.github.difflib.text.DiffRow.Tag;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Utility that injects markup open/close tags into a mutable token list at specified positions,
 * respecting newline boundaries.
 *
 * <p>This class has a single responsibility: presentation markup injection. It has no knowledge
 * of diff algorithms, patch structures, or {@link DiffRow} assembly.
 */
public final class InlineTagRenderer {

		private InlineTagRenderer() {}

		/**
		 * Wraps the tokens between {@code startPosition} (inclusive) and {@code endPosition}
		 * (exclusive) with markup strings produced by {@code tagGenerator}.
		 *
		 * <p>Line-feed tokens ({@code "\n"}) act as segment boundaries — a separate open/close tag
		 * pair is emitted for each contiguous non-newline run. When {@code replaceLinefeedWithSpace}
		 * is {@code true} the newline tokens are replaced with a space instead.
		 *
		 * @param sequence the mutable token list to annotate in-place.
		 * @param startPosition the index of the first token to wrap (zero-based, inclusive).
		 * @param endPosition the index past the last token to wrap (exclusive).
		 * @param tag the semantic tag type passed to the generator so callers can vary markup by type.
		 * @param tagGenerator produces the open ({@code isOpen=true}) or close ({@code isOpen=false})
		 *     markup string for a given tag.
		 * @param processDiffs optional post-processor applied to every diffed token before the close
		 *     tag is inserted; may be {@code null}.
		 * @param replaceLinefeedWithSpace when {@code true}, newline tokens inside the range are
		 *     replaced with a space rather than used as segment boundaries.
		 */
		public static void wrapInTag(
						List<String> sequence,
						int startPosition,
						int endPosition,
						Tag tag,
						BiFunction<Tag, Boolean, String> tagGenerator,
						Function<String, String> processDiffs,
						boolean replaceLinefeedWithSpace) {
				int endPos = endPosition;

				while (endPos >= startPosition) {

						// search position for end tag
						while (endPos > startPosition) {
								if (!"\n".equals(sequence.get(endPos - 1))) {
										break;
								} else if (replaceLinefeedWithSpace) {
										sequence.set(endPos - 1, " ");
										break;
								}
								endPos--;
						}

						if (endPos == startPosition) {
								break;
						}

						sequence.add(endPos, tagGenerator.apply(tag, false));
						if (processDiffs != null) {
								sequence.set(endPos - 1, processDiffs.apply(sequence.get(endPos - 1)));
						}
						endPos--;

						// search position for start tag
						while (endPos > startPosition) {
								if ("\n".equals(sequence.get(endPos - 1))) {
										if (replaceLinefeedWithSpace) {
												sequence.set(endPos - 1, " ");
										} else {
												break;
										}
								}
								if (processDiffs != null) {
										sequence.set(endPos - 1, processDiffs.apply(sequence.get(endPos - 1)));
								}
								endPos--;
						}

						sequence.add(endPos, tagGenerator.apply(tag, true));
						endPos--;
				}
		}
}
