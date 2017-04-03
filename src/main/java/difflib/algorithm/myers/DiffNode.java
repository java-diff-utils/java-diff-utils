/*-
 * #%L
 * java-diff-utils
 * %%
 * Copyright (C) 2009 - 2017 java-diff-utils
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */
package difflib.algorithm.myers;

/**
 * <p>
 * Title: </p>
 * <p>
 * Description: </p>
 * <p>
 * Copyright: Copyright (c) 2002</p>
 * <p>
 * Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
/**
 * A diffnode in a diffpath.
 * <p>
 * A DiffNode and its previous node mark a delta between two input sequences, that is, two differing
 * subsequences between (possibly zero length) matching sequences.
 *
 * {@link DiffNode DiffNodes} and {@link Snake Snakes} allow for compression of diffpaths, as each
 * snake is represented by a single {@link Snake Snake} node and each contiguous series of
 * insertions and deletions is represented by a single {@link DiffNode DiffNodes}.
 *
 * @version $Revision: 60 $ $Date: 2003-05-10 21:56:10 +0300 (Суб, 10 Май 2003) $
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 *
 */
public final class DiffNode extends PathNode {

    /**
     * Constructs a DiffNode.
     * <p>
     * DiffNodes are compressed. That means that the path pointed to by the <code>prev</code>
     * parameter will be followed using {@link PathNode#previousSnake} until a non-diff node is
     * found.
     *
     * @param the position in the original sequence
     * @param the position in the revised sequence
     * @param prev the previous node in the path.
     */
    public DiffNode(int i, int j, PathNode prev) {
        super(i, j, (prev == null ? null : prev.previousSnake()));
    }

    /**
     * {@inheritDoc}
     *
     * @return false, always
     */
    @Override
    public boolean isSnake() {
        return false;
    }

}
