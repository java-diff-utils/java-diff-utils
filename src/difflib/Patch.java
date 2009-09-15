package difflib;

import java.util.*;

/**
 * Describes the patch holding all deltas between the original and revised texts.
 * 
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 */
public class Patch {
	private List<Delta> deltas = new LinkedList<Delta>();
	
	public List<?> applyTo(List<?> target) {
		List<Object> result = new LinkedList<Object>(target);
		for (Delta delta: deltas) {
			delta.applyTo(result);
		}
		return result;
	}
	
	/**
	 * Add the given delta to this patch
	 * @param delta the given delta
	 */
	public void addDelta(Delta delta) {
		deltas.add(delta);
	}
	
	/**
	 * @param deltas the deltas to set
	 */
	public void setDeltas(List<Delta> deltas) {
		this.deltas = deltas;
	}

	/**
	 * @return the deltas
	 */
	public List<Delta> getDeltas() {
		return deltas;
	}
}
