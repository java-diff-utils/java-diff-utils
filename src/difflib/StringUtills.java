package difflib;

import java.util.*;

public class StringUtills {
	
	public static <T> String join(final Iterable<T> objs, final String delimiter) {
	    Iterator<T> iter = objs.iterator();
	    if (!iter.hasNext()) {
	        return "";
	    }
	    StringBuffer buffer = new StringBuffer(String.valueOf(iter.next()));
	    while (iter.hasNext()) {
	        buffer.append(delimiter).append(String.valueOf(iter.next()));
	    }
	    return buffer.toString();
	}
	
	public static String expandTabs(String str) {
		return str.replace("\t", "    ");
	}
	
	public static String htmlEntites(String str) {
		return str.replace("<", "&lt;").replace(">", "&gt;");
	}
	
	public static String normalize(String str) {
		return expandTabs(htmlEntites(str));
	}
	
	public static List<String> normalize(List<String> list) {
		List<String> result = new LinkedList<String>();
		for (String line: list) {
			result.add(normalize(line));
		}
		return result;
	}

	public static List<String> wrapText(List<String> list, int columnWidth) {
		List<String> result = new LinkedList<String>();
		for (String line: list) {
			result.add(wrapText(line, columnWidth));
		}
		return result;
	}
	
	public static String wrapText(String line, int columnWidth) {
		if (line.length() > columnWidth) {
			return line.subSequence(0, columnWidth) + "<br>" + line.substring(columnWidth);
		}
		return line;
	}
}
