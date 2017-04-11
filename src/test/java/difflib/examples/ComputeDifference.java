package difflib.examples;

import difflib.DiffUtils;
import difflib.TestConstants;
import difflib.algorithm.DiffException;
import difflib.patch.Delta;
import difflib.patch.Patch;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class ComputeDifference {

    static final String ORIGINAL = TestConstants.MOCK_FOLDER + "original.txt";
    static final String RIVISED = TestConstants.MOCK_FOLDER + "revised.txt";

    public static void main(String[] args) throws DiffException, IOException {
        List<String> original = Files.readAllLines(new File(ORIGINAL).toPath());
        List<String> revised = Files.readAllLines(new File(RIVISED).toPath());

        // Compute diff. Get the Patch object. Patch is the container for computed deltas.
        Patch<String> patch = DiffUtils.diff(original, revised);

        for (Delta<String> delta : patch.getDeltas()) {
            System.out.println(delta);
        }
    }
}
