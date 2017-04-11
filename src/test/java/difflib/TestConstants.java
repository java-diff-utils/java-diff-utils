package difflib;

import java.io.File;

/**
 * Test constants
 *
 * @author simon.mittermueller@gmail.com
 *
 */
public final class TestConstants {

    private TestConstants() {
        // prevent construction.
    }

    public static final String BASE_FOLDER_RESOURCES = "target/test-classes/";

    /**
     * The base folder containing the test files. Ends with {@link #FS}.
     */
    public static final String MOCK_FOLDER = BASE_FOLDER_RESOURCES + "/mocks/";

}
