package com.github.difflib;

import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class to implement inline character-level differences.
 */
public final class InlineDiffUtils {

		/**
		 * Computes the difference between the given texts inline. This one uses the
		 * "trick" to make out of texts lists of characters, like DiffRowGenerator
		 * does and merges those changes at the end together again.
		 *
		 * @param original a {@link String} representing the original text. Must not be {@code null}.
		 * @param revised a {@link String} representing the revised text. Must not be {@code null}.
		 * @return The patch describing the difference between the original and
		 * revised sequences. Never {@code null}.
		 */
		public static Patch<String> diffInline(String original, String revised) {
				List<String> origList = new ArrayList<>();
				List<String> revList = new ArrayList<>();
				for (Character character : original.toCharArray()) {
						origList.add(character.toString());
				}
				for (Character character : revised.toCharArray()) {
						revList.add(character.toString());
				}
				Patch<String> patch = DiffUtils.diff(origList, revList);
				for (AbstractDelta<String> delta : patch.getDeltas()) {
						delta.getSource().setLines(compressLines(delta.getSource().getLines(), ""));
						delta.getTarget().setLines(compressLines(delta.getTarget().getLines(), ""));
				}
				return patch;
		}

		private static List<String> compressLines(List<String> lines, String delimiter) {
				if (lines.isEmpty()) {
						return Collections.emptyList();
				}
				return Collections.singletonList(String.join(delimiter, lines));
		}

		private InlineDiffUtils() {}
}
