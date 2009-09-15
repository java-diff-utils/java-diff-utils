package difflib;

import java.util.List;

/**
 * Describes the change-delta between original and revised texts.
 * 
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 */
public class ChangeDelta extends Delta {

	/**
	 * {@inheritDoc}
	 */
	public ChangeDelta(Chunk original, Chunk revised) {
		super(original, revised);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void applyTo(List<Object> target) {
		int position = getOriginal().getPosition();
		int originalSize = getOriginal().getSize();
		for (int i = 0; i < originalSize; i++) {
			target.remove(position);
		}
		int i = 0;
		for (Object line: getRevised().getLines()) {
			target.add(position + i, line);
			i++;
		}
	}

}
