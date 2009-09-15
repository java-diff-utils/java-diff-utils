package difflib;

import java.util.*;

/**
 * Describes the delta between original and revised texts.
 * 
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 */
public abstract class Delta {
	private Chunk original;
	private Chunk revised;

	/**
	 * Construct the delta for original and revised chunks
	 * 
	 * @param original chunk describes the original text
	 * @param revised chunk describes the revised text
	 */
	public Delta(Chunk original, Chunk revised) {
		this.original = original;
		this.revised  = revised;
	}
	
	/**
	 * Applies this delta as the patch for a given target
	 * 
	 * @param target the given target 
	 */
	public abstract void applyTo(List<Object> target);
	
	/**
	 * @return the Chunk describing the original text
	 */
	public Chunk getOriginal() {
		return original;
	}
	/**
	 * @param original the Chunk describing the original text to set
	 */
	public void setOriginal(Chunk original) {
		this.original = original;
	}
	
	/**
	 * @return the Chunk describing the revised text
	 */
	public Chunk getRevised() {
		return revised;
	}
	/**
	 * @param revised the Chunk describing the revised text to set
	 */
	public void setRevised(Chunk revised) {
		this.revised = revised;
	}
}
