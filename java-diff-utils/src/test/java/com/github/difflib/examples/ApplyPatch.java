package com.github.difflib.examples;

import com.github.difflib.DiffUtils;
import com.github.difflib.TestConstants;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class ApplyPatch {

    private static final String ORIGINAL = TestConstants.MOCK_FOLDER + "issue10_base.txt";
    private static final String PATCH = TestConstants.MOCK_FOLDER + "issue10_patch.txt";

    public static void main(String[] args) throws PatchFailedException, IOException {
        List<String> original = Files.readAllLines(new File(ORIGINAL).toPath());
        List<String> patched = Files.readAllLines(new File(PATCH).toPath());

        // At first, parse the unified diff file and get the patch
        Patch<String> patch = UnifiedDiffUtils.parseUnifiedDiff(patched);

        // Then apply the computed patch to the given text
        List<String> result = DiffUtils.patch(original, patch);
        System.out.println(result);
        // / Or we can call patch.applyTo(original). There is no difference.
    }
}
