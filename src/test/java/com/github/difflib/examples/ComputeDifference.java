package com.github.difflib.examples;

import com.github.difflib.DiffUtils;
import com.github.difflib.TestConstants;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class ComputeDifference {

    private static final String ORIGINAL = TestConstants.MOCK_FOLDER + "original.txt";
    private static final String RIVISED = TestConstants.MOCK_FOLDER + "revised.txt";

    public static void main(String[] args) throws DiffException, IOException {
        List<String> original = Files.readAllLines(new File(ORIGINAL).toPath());
        List<String> revised = Files.readAllLines(new File(RIVISED).toPath());

        // Compute diff. Get the Patch object. Patch is the container for computed deltas.
        Patch<String> patch = DiffUtils.diff(original, revised);

        for (AbstractDelta<String> delta : patch.getDeltas()) {
            System.out.println(delta);
        }
    }
}
