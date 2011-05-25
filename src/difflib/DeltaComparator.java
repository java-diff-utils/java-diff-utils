package difflib;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author mksenzov
 */
public class DeltaComparator implements Comparator<Delta>, Serializable {
    private static final long serialVersionUID = 1L;
    public static final Comparator<Delta> INSTANCE = new DeltaComparator();

    private DeltaComparator() {
    }

    public int compare(final Delta a, final Delta b) {
        return Integer.valueOf(a.getOriginal().getPosition()).compareTo(b.getOriginal().getPosition());
    }
}