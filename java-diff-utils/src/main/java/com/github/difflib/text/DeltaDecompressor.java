package com.github.difflib.text;

import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.ChangeDelta;
import com.github.difflib.patch.Chunk;
import com.github.difflib.patch.DeleteDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.InsertDelta;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility that normalises asymmetric {@link ChangeDelta}s into equal-size pairs
 * so that DiffRow building stays simple.
 *
 * <p>When a CHANGE delta has a different number of source and target lines it is
 * split into a same-size {@link ChangeDelta} followed by either an {@link InsertDelta}
 * or a {@link DeleteDelta} for the surplus lines.
 */
public final class DeltaDecompressor {

		private DeltaDecompressor() {}

		/**
		 * Decompresses a {@link ChangeDelta} whose source and target sizes differ into
		 * a same-size {@link ChangeDelta} plus a trailing {@link InsertDelta} or
		 * {@link DeleteDelta}. All other delta types are returned unchanged in a
		 * singleton list.
		 *
		 * @param delta the delta to (possibly) decompress. Must not be {@code null}.
		 * @return a list containing the original delta, or the two replacement deltas.
		 */
		public static List<AbstractDelta<String>> decompress(AbstractDelta<String> delta) {
				if (delta.getType() == DeltaType.CHANGE
								&& delta.getSource().size() != delta.getTarget().size()) {
						List<AbstractDelta<String>> deltas = new ArrayList<>();

						int minSize = Math.min(delta.getSource().size(), delta.getTarget().size());
						Chunk<String> orig = delta.getSource();
						Chunk<String> rev = delta.getTarget();

						deltas.add(new ChangeDelta<String>(
										new Chunk<>(orig.getPosition(), orig.getLines().subList(0, minSize)),
										new Chunk<>(rev.getPosition(), rev.getLines().subList(0, minSize))));

						if (orig.getLines().size() < rev.getLines().size()) {
								deltas.add(new InsertDelta<String>(
												new Chunk<>(orig.getPosition() + minSize, Collections.emptyList()),
												new Chunk<>(
																rev.getPosition() + minSize,
																rev.getLines().subList(minSize, rev.getLines().size()))));
						} else {
								deltas.add(new DeleteDelta<String>(
												new Chunk<>(
																orig.getPosition() + minSize,
																orig.getLines().subList(minSize, orig.getLines().size())),
												new Chunk<>(rev.getPosition() + minSize, Collections.emptyList())));
						}
						return deltas;
				}

				return Collections.singletonList(delta);
		}
}
