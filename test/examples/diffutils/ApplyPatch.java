package diffutils;

import java.io.File;
import java.util.List;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;

public class ApplyPatch extends Example {
    static final String FS = File.separator;
    static final String ORIGINAL = "test" + FS + "mocks" + FS + "issue10_base.txt";
    static final String PATCH = "test" + FS + "mocks" + FS + "issue10_patch.txt";

    public static void main(String[] args) throws PatchFailedException {
        List<String> original = fileToLines(ORIGINAL);
        List<String> patched  = fileToLines(PATCH);

     // At first, parse the unified diff file and get the patch
        Patch patch = DiffUtils.parseUnifiedDiff(patched);

        // Then apply the computed patch to the given text
        List result = DiffUtils.patch(original, patch);
        System.out.println(result);
        /// Or we can call patch.applyTo(original). There is no difference.
    }
}
