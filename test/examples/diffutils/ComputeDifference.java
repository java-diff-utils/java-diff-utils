package diffutils;

import java.io.File;
import java.util.List;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

public class ComputeDifference extends Example {
    static final String FS = File.separator;
    static final String ORIGINAL = "test" + FS + "mocks" + FS + "original.txt";
    static final String RIVISED = "test" + FS + "mocks" + FS + "revised.txt";

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
