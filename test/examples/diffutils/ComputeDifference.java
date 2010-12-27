package diffutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

public class ComputeDifference {
    static final String FS = File.separator;
    static final String originalFilename = "test" + FS + "mocks" + FS + "original.txt";
    static final String revisedFilename = "test" + FS + "mocks" + FS + "revised.txt";

    private static List<String> fileToLines(String filename) {
        List<String> lines = new LinkedList<String>();
        String line = "";
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            while ((line = in.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static void main(String[] args) {
        List<String> original = fileToLines(originalFilename);
        List<String> revised  = fileToLines(revisedFilename);

        // Compute diff. Get the Patch object. Patch is the container for computed deltas.
        Patch patch = DiffUtils.diff(original, revised);

        for (Delta delta: patch.getDeltas()) {
            System.out.println(delta);
        }
    }
}
