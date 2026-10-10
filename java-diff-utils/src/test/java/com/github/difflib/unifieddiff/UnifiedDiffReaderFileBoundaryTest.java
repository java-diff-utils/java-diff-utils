/*
 * Copyright 2026 java-diff-utils.
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
package com.github.difflib.unifieddiff;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.difflib.patch.PatchFailedException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class UnifiedDiffReaderFileBoundaryTest {

		@Test
		public void testParseHeaderOnlyFilesBeforeTextChange() throws IOException, PatchFailedException {
				UnifiedDiff diff = UnifiedDiffReader.parseUnifiedDiff(getClass().getResourceAsStream("header_only_files.diff"));

				assertThat(diff.getFiles()).hasSize(5);
				UnifiedDiffFile binary = diff.getFiles().get(0);
				assertThat(binary.getFromFile()).isEqualTo("a-image.bin");
				assertThat(binary.getToFile()).isEqualTo("a-image.bin");
				assertThat(binary.getBinaryEdited()).isEqualTo("a-image.bin");
				assertThat(binary.getPatch().getDeltas()).isEmpty();

				UnifiedDiffFile empty = diff.getFiles().get(1);
				assertThat(empty.getToFile()).isEqualTo("b-empty.txt");
				assertThat(empty.getNewFileMode()).isEqualTo("100644");
				assertThat(empty.getBinaryEdited()).isNull();
				assertThat(empty.getPatch().getDeltas()).isEmpty();

				UnifiedDiffFile mode = diff.getFiles().get(2);
				assertThat(mode.getFromFile()).isEqualTo("c-script.sh");
				assertThat(mode.getOldMode()).isEqualTo("100644");
				assertThat(mode.getNewMode()).isEqualTo("100755");
				assertThat(mode.getNewFileMode()).isNull();
				assertThat(mode.getPatch().getDeltas()).isEmpty();

				UnifiedDiffFile rename = diff.getFiles().get(3);
				assertThat(rename.getFromFile()).isEqualTo("d-old.txt");
				assertThat(rename.getToFile()).isEqualTo("e-renamed.txt");
				assertThat(rename.getSimilarityIndex()).isEqualTo(100);
				assertThat(rename.getRenameFrom()).isEqualTo("d-old.txt");
				assertThat(rename.getRenameTo()).isEqualTo("e-renamed.txt");
				assertThat(rename.getOldMode()).isNull();
				assertThat(rename.getPatch().getDeltas()).isEmpty();

				UnifiedDiffFile text = diff.getFiles().get(4);
				assertThat(text.getFromFile()).isEqualTo("z-text.txt");
				assertThat(text.getToFile()).isEqualTo("z-text.txt");
				assertThat(text.getBinaryEdited()).isNull();
				assertThat(text.getNewFileMode()).isNull();
				assertThat(text.getNewMode()).isNull();
				assertThat(text.getRenameFrom()).isNull();
				assertThat(text.getPatch().applyTo(Collections.singletonList("old"))).containsExactly("new");
		}

		@Test
		public void testParseConsecutiveHeaderOnlyFilesAtEndOfDiff() throws IOException {
				String text = "diff --git a/first.sh b/first.sh\n"
								+ "old mode 100644\nnew mode 100755\n"
								+ "diff --git a/second.sh b/second.sh\n"
								+ "old mode 100755\nnew mode 100644\n";
				UnifiedDiff diff =
								UnifiedDiffReader.parseUnifiedDiff(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));

				assertThat(diff.getFiles()).hasSize(2);
				assertThat(diff.getFiles().get(0).getFromFile()).isEqualTo("first.sh");
				assertThat(diff.getFiles().get(0).getNewMode()).isEqualTo("100755");
				assertThat(diff.getFiles().get(1).getFromFile()).isEqualTo("second.sh");
				assertThat(diff.getFiles().get(1).getNewMode()).isEqualTo("100644");
				assertThat(diff.getFiles())
								.allSatisfy(file -> assertThat(file.getPatch().getDeltas()).isEmpty());
		}

		@Test
		public void testParseTextChangeBeforeHeaderOnlyAndBinaryFiles() throws IOException, PatchFailedException {
				String text = "diff --git a/text.txt b/text.txt\n"
								+ "--- a/text.txt\n+++ b/text.txt\n@@ -1 +1 @@\n-old\n+new\n"
								+ "diff --git a/script.sh b/script.sh\n"
								+ "old mode 100644\nnew mode 100755\n"
								+ "diff --git a/image.bin b/image.bin\n"
								+ "Binary files a/image.bin and b/image.bin differ\n";
				UnifiedDiff diff =
								UnifiedDiffReader.parseUnifiedDiff(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));

				assertThat(diff.getFiles()).hasSize(3);
				UnifiedDiffFile changed = diff.getFiles().get(0);
				assertThat(changed.getFromFile()).isEqualTo("text.txt");
				assertThat(changed.getNewMode()).isNull();
				assertThat(changed.getBinaryEdited()).isNull();
				assertThat(changed.getPatch().applyTo(Collections.singletonList("old"))).containsExactly("new");
				UnifiedDiffFile mode = diff.getFiles().get(1);
				assertThat(mode.getFromFile()).isEqualTo("script.sh");
				assertThat(mode.getNewMode()).isEqualTo("100755");
				assertThat(mode.getBinaryEdited()).isNull();
				assertThat(mode.getPatch().getDeltas()).isEmpty();
				UnifiedDiffFile binary = diff.getFiles().get(2);
				assertThat(binary.getFromFile()).isEqualTo("image.bin");
				assertThat(binary.getBinaryEdited()).isEqualTo("image.bin");
				assertThat(binary.getNewMode()).isNull();
				assertThat(binary.getPatch().getDeltas()).isEmpty();
		}
}
