package difflib;

import java.util.List;

/**
 * Describes the delete-delta between original and revised texts.
 * 
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 */
public class DeleteDelta extends Delta {

	/**
	 * {@inheritDoc}
	 */
	public DeleteDelta(Chunk original, Chunk revised) {
		super(original, revised);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void applyTo(List<Object> target) {
		int position = getOriginal().getPosition();
		int size = getOriginal().getSize();
		for (int i = 0; i < size; i++) {
			target.remove(position);
		}
	}

}
