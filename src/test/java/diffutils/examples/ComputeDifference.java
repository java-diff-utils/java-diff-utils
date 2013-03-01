package diffutils.examples;

import java.util.List;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import diffutils.TestConstants;

public class ComputeDifference extends Example {
    
     
    static final String ORIGINAL = TestConstants.MOCK_FOLDER + "original.txt";
    static final String RIVISED = TestConstants.MOCK_FOLDER + "revised.txt";

    public static void main(String[] args) {
        List<String> original = fileToLines(ORIGINAL);
        List<String> revised  = fileToLines(RIVISED);

        // Compute diff. Get the Patch object. Patch is the container for computed deltas.
        Patch patch = DiffUtils.diff(original, revised);

        for (Delta delta: patch.getDeltas()) {
            System.out.println(delta);
        }
    }
}
