package difflib;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import difflib.myers.*;

/**
 * Implements the difference and patching engine
 * 
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 */
public class DiffUtils {
	private static DiffAlgorithm defaultDiffAlgorithm = new MyersDiff();
	private static Pattern unifiedDiffChunkRe = 
		Pattern.compile("@@\\s+-(?:(\\d+)(?:,(\\d+))?)\\s+\\+(?:(\\d+)(?:,(\\d+))?)\\s+@@");
	
	/**
	 * Compute the difference between the original and revised texts with default diff algorithm 
	 * 
	 * @param original the original text
	 * @param revised the revised text
	 * @return the patch describing the difference between the original and revised texts 
	 */
	public static Patch diff(List<?> original, List<?> revised) {
		return DiffUtils.diff(original, revised, defaultDiffAlgorithm);
	}
	
	/**
	 * Compute the difference between the original and revised texts with given diff algorithm 
	 * 
	 * @param original the original text
	 * @param revised the revised text
	 * @param algorithm the given algorithm
	 * @return the patch describing the difference between the original and revised texts
	 */
	public static Patch diff(List<?> original, List<?> revised, DiffAlgorithm algorithm) {
		return algorithm.diff(original, revised);
	}
	
	/**
	 * Patch the original text with given patch
	 * 
	 * @param original the original text
	 * @param patch the given patch
	 * @return the revised text
	 */
	public static List<?> patch(List<?> original, Patch patch) {
		return patch.applyTo(original);
	}
	
	/**
	 * Unpatch the revised text for a given patch
	 * 
	 * @param revised the revised text
	 * @param patch the given patch
	 * @return the original text
	 */
	public static String unpatch(String revised, Patch patch) {
		// TODO: Not-implemented
		return null;
	}
	
	/**
	 * Parse the given text in unified format and creates the list of deltas for it.
	 * 
	 * @param diff the text in unified format 
	 */
	public static Patch parseUnifiedDiff(List<String> diff) {
		boolean inPrelude = true;
		List<Object[]> rawChunk = new ArrayList<Object[]>();
		Patch patch = new Patch();
		
		int old_ln = 0, old_n = 0, new_ln = 0, new_n = 0;
		String tag = "", rest = "";
		for (String line: diff) {
			// Skip leading lines until after we've seen one starting with '+++'
			if (inPrelude) {
				if (line.startsWith("+++")) {
					inPrelude = false;
				}
				continue;
			}
			Matcher m = unifiedDiffChunkRe.matcher(line);
			if (m.find()) {
				// Process the lines in the previous chunk
				if (rawChunk.size() != 0) {
					List<String> oldChunkLines = new ArrayList<String>();
					List<String> newChunkLines = new ArrayList<String>();
					
					for (Object[] raw_line: rawChunk) {
						tag = (String)raw_line[0];
						rest = (String)raw_line[1];
						if (tag.equals(" ") || tag.equals("-")) {
							oldChunkLines.add(rest);
						}
						if (tag.equals(" ") || tag.equals("+")) {
							newChunkLines.add(rest);
						}
					}
					patch.addDelta(new ChangeDelta(new Chunk(old_ln - 1, old_n, oldChunkLines),  
							new Chunk(new_ln - 1, new_n, newChunkLines)));
					rawChunk.clear();
				}
				// Parse the @@ header
				old_ln = Integer.parseInt(m.group(1));
				old_n  = Integer.parseInt(m.group(2));
				new_ln = Integer.parseInt(m.group(3));
				new_n  = Integer.parseInt(m.group(4));
			} else {
				tag  = line.substring(0, 1);
				rest = line.substring(1);
				if (tag.equals(" ") || tag.equals("+") || tag.equals("-")) {
					rawChunk.add(new Object[] {tag, rest});
				}
			}
		}
		
		// Process the lines in the last chunk
		if (rawChunk.size() != 0) {
			List<String> oldChunkLines = new ArrayList<String>();
			List<String> newChunkLines = new ArrayList<String>();
			
			for (Object[] raw_line: rawChunk) {
				tag = (String)raw_line[0];
				rest = (String)raw_line[1];
				if (tag.equals(" ") || tag.equals("-")) {
					oldChunkLines.add(rest);
				} 
				if (tag.equals(" ") || tag.equals("+")) {
					newChunkLines.add(rest);
				}
			}
			
			patch.addDelta(new ChangeDelta(new Chunk(old_ln - 1, old_n, oldChunkLines),  
					new Chunk(new_ln - 1, new_n, newChunkLines)));
			rawChunk.clear();
		}
		
		return patch;
	}
	
	/**
	 * Get the DiffRows describing the difference between original and revised texts. Useful for
	 * displaying side-by-side diff. Computes the Patch before generate the diffs.
	 * 
	 * @param original the original text
	 * @param revised the revised text
	 * @return the DiffRows between original and revised texts
	 */
	public static List<DiffRow> getDiffRows(List<?> original, List<?> revised) {
		return null;
	}
}
