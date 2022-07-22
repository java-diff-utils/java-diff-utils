/*
 * Copyright 2022 java-diff-utils.
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

import com.github.difflib.patch.PatchFailedException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("for next release")
public class UnifiedDiffRoundTripNewLineTest {
    @Test
    public void testIssue135MissingNoNewLineInPatched() throws IOException, PatchFailedException {
        String beforeContent = "rootProject.name = \"sample-repo\"";
        String afterContent = "rootProject.name = \"sample-repo\"\n";
        String patch = "diff --git a/settings.gradle b/settings.gradle\n" +
        "index ef3b8e2..ab30124 100644\n" +
        "--- a/settings.gradle\n" +
        "+++ b/settings.gradle\n" +
        "@@ -1 +1 @@\n" +
        "-rootProject.name = \"sample-repo\"\n" +
        "\\ No newline at end of file\n" +
        "+rootProject.name = \"sample-repo\"\n";
        UnifiedDiff unifiedDiff = UnifiedDiffReader.parseUnifiedDiff(new ByteArrayInputStream(patch.getBytes()));
        String unifiedAfterContent = unifiedDiff.getFiles().get(0).getPatch()
                .applyTo(Arrays.asList(beforeContent.split("\n"))).stream().collect(joining("\n"));
        assertEquals(afterContent, unifiedAfterContent);
    }
}
