package com.github.difflib;

import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import java.util.List;

/**
 * Utility class to implement the patching engine.
 */
public final class PatchUtils {

		/**
		 * Applies the given patch to the original list and returns the revised list.
		 *
		 * @param original a {@link List} representing the original list.
		 * @param patch a {@link Patch} representing the patch to apply.
		 * @return the revised list.
		 * @throws PatchFailedException if the patch cannot be applied.
		 */
		public static <T> List<T> patch(List<? extends T> original, Patch<T> patch) throws PatchFailedException {
				return patch.applyTo(original);
		}

		/**
		 * Applies the given patch to the revised list and returns the original list.
		 *
		 * @param revised a {@link List} representing the revised list.
		 * @param patch a {@link Patch} representing the patch to apply.
		 * @return the original list.
		 * @throws PatchFailedException if the patch cannot be applied.
		 */
		public static <T> List<T> unpatch(List<? extends T> revised, Patch<T> patch) {
				return patch.restore(revised);
		}

		private PatchUtils() {}
}
