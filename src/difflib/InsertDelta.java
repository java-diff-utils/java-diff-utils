package difflib;

import java.util.List;

/**
 * Describes the add-delta between original and revised texts.
 * 
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 */
public class InsertDelta extends Delta {

	/**
	 * {@inheritDoc}
	 */
	public InsertDelta(Chunk original, Chunk revised) {
		super(original, revised);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void applyTo(List<Object> target) {
		int position = this.getOriginal().getPosition(); 
		List<?> lines = this.getRevised().getLines();
		for (int i = 0; i < lines.size(); i++) {
			target.add(position + i, lines.get(i));
		}
	}

}
