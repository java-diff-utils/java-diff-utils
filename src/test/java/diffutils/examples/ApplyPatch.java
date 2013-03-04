package diffutils.examples;

import java.util.List;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;
import diffutils.TestConstants;

public class ApplyPatch extends Example {

	static final String ORIGINAL = TestConstants.MOCK_FOLDER + "issue10_base.txt";
	static final String PATCH = TestConstants.MOCK_FOLDER + "issue10_patch.txt";

	public static void main(String[] args) throws PatchFailedException {
		List<String> original = fileToLines(ORIGINAL);
		List<String> patched = fileToLines(PATCH);

		// At first, parse the unified diff file and get the patch
		Patch<String> patch = DiffUtils.parseUnifiedDiff(patched);

		// Then apply the computed patch to the given text
		List<String> result = DiffUtils.patch(original, patch);
		System.out.println(result);
		// / Or we can call patch.applyTo(original). There is no difference.
	}
}
