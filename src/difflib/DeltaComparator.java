package difflib;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author mksenzov
 */
public class DeltaComparator implements Comparator<Delta>, Serializable {

    public static final Comparator<Delta> INSTANCE = new DeltaComparator();

    private DeltaComparator() {
    }

    public int compare(final Delta a, final Delta b) {
        return new Integer(a.getOriginal().getPosition()).compareTo(b.getOriginal().getPosition());
    }
}