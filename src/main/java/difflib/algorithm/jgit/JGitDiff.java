/*
 * Copyright 2017 java-diff-utils.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package difflib.algorithm.jgit;

import difflib.algorithm.DiffAlgorithm;
import difflib.algorithm.DiffException;
import difflib.patch.ChangeDelta;
import difflib.patch.Chunk;
import difflib.patch.DeleteDelta;
import difflib.patch.InsertDelta;
import difflib.patch.Patch;
import java.util.List;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.HistogramDiff;
import org.eclipse.jgit.diff.Sequence;
import org.eclipse.jgit.diff.SequenceComparator;

/**
 *
 * @author toben
 */
public class JGitDiff<T> implements DiffAlgorithm<T> {

    @Override
    public Patch diff(List<T> original, List<T> revised) throws DiffException {
        EditList diffList = new EditList();
        diffList.addAll(new HistogramDiff().diff(new DataListComparator<>(), new DataList<>(original), new DataList<>(revised)));
        Patch<T> patch = new Patch<>();
        for (Edit edit : diffList) {
            System.out.println(edit);

            Chunk<T> orgChunk = new Chunk<>(edit.getBeginA(), original.subList(edit.getBeginA(), edit.getEndA()));
            Chunk<T> revChunk = new Chunk<>(edit.getBeginA(), revised.subList(edit.getBeginB(), edit.getEndB()));
            switch (edit.getType()) {
                case DELETE:
                    patch.addDelta(new DeleteDelta<>(orgChunk, revChunk));
                    break;
                case INSERT:
                    patch.addDelta(new InsertDelta<>(orgChunk, revChunk));
                    break;
                case REPLACE:
                    patch.addDelta(new ChangeDelta<>(orgChunk, revChunk));
                    break;
            }
        }
        return patch;
    }
}

class DataListComparator<T> extends SequenceComparator<DataList<T>> {

    @Override
    public boolean equals(DataList<T> original, int orgIdx, DataList<T> revised, int revIdx) {
        return original.data.get(orgIdx).equals(revised.data.get(revIdx));
    }

    @Override
    public int hash(DataList<T> s, int i) {
        return s.data.get(i).hashCode();
    }

}

class DataList<T> extends Sequence {

    final List<T> data;

    public DataList(List<T> data) {
        this.data = data;
    }

    @Override
    public int size() {
        return data.size();
    }
}
