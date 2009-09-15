package difflib;

/**
 * Describes the diff row in form [tag, oldLine, newLine) for showing the difference between two
 * texts 
 * 
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 */
public class DiffRow {
	private Tag tag;
	private String oldLine;
	private String newLine;
	
	public enum Tag {
		INSERT, DELETE, CHANGE
	}
	
	/**
	 * @return the tag
	 */
	public Tag getTag() {
		return tag;
	}
	/**
	 * @param tag the tag to set
	 */
	public void setTag(Tag tag) {
		this.tag = tag;
	}
	
	/**
	 * @return the oldLine
	 */
	public String getOldLine() {
		return oldLine;
	}
	/**
	 * @param oldLine the oldLine to set
	 */
	public void setOldLine(String oldLine) {
		this.oldLine = oldLine;
	}
	
	/**
	 * @return the newLine
	 */
	public String getNewLine() {
		return newLine;
	}
	/**
	 * @param newLine the newLine to set
	 */
	public void setNewLine(String newLine) {
		this.newLine = newLine;
	}
}
