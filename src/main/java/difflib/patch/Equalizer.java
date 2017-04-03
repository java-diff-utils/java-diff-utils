package difflib.patch;

/**
 * Specifies when two compared elements are equal.
 *
 * @param T The type of the compared elements in the 'lines'.
 */
public interface Equalizer<T> {

    /**
     * Indicates if two elements are equal according to the diff mechanism.
     *
     * @param original The original element. Must not be {@code null}.
     * @param revised The revised element. Must not be {@code null}.
     * @return Returns true if the elements are equal.
     */
    public boolean equals(T original, T revised);
}
