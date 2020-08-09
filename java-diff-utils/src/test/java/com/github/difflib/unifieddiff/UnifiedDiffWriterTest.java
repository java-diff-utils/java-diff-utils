/*
 * Copyright 2019 java-diff-utils.
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

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
public class UnifiedDiffWriterTest {

    public UnifiedDiffWriterTest() {
    }

    @Test
    public void testWrite() throws URISyntaxException, IOException {
        String str = readFile(UnifiedDiffReaderTest.class.getResource("jsqlparser_patch_1.diff").toURI(), Charset.defaultCharset());
        UnifiedDiff diff = UnifiedDiffReader.parseUnifiedDiff(new ByteArrayInputStream(str.getBytes()));

        StringWriter writer = new StringWriter();
        UnifiedDiffWriter.write(diff, f -> Collections.EMPTY_LIST, writer, 5);
        System.out.println(writer.toString());
    }
    
    /**
     * Issue 47
     */
    @Test
    public void testWriteWithNewFile() throws URISyntaxException, IOException {
        
        List<String> original = new ArrayList<>();
        List<String> revised = new ArrayList<>();

        revised.add("line1");
        revised.add("line2");

        Patch<String> patch = DiffUtils.diff(original, revised);
        UnifiedDiff diff = new UnifiedDiff();
        diff.addFile( UnifiedDiffFile.from(null, "revised", patch) );

        StringWriter writer = new StringWriter();
        UnifiedDiffWriter.write(diff, f -> original, writer, 5);
        System.out.println(writer.toString());
        
        String[] lines = writer.toString().split("\\n");
        
        assertEquals("--- /dev/null", lines[0]);
        assertEquals("+++ revised", lines[1]);
        assertEquals("@@ -0,0 +1,2 @@", lines[2]);
    }

    static String readFile(URI path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
