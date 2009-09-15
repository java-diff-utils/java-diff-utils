package difflib;

import java.util.*;

/**
 * The general interface for computing diffs between two texts
 * 
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 */
public interface DiffAlgorithm {
	
    /**
     * Computes the difference between the original
     * sequence and the revised sequence and returns it
     * as a {@link difflib.Patch Patch} object.
     *
     * @param original the original text
     * @param revised the revised text
     * @return the patch
     */
	public Patch diff(Object[] original, Object[] revised);
	
    /**
     * Computes the difference between the original
     * sequence and the revised sequence and returns it
     * as a {@link difflib.Patch Patch} object.
     *
     * @param original the original text
     * @param revised the revised text
     * @return the patch
     */
	public Patch diff(List<?> original, List<?> revised);
}
