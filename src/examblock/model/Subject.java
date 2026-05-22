package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * An object describing a single Year 12 Subject.
 */
public class Subject implements StreamManager, ManageableListItem {

    /** the text description of this subject. */
    private String description;

    /** the title of this subject. */
    private String title;

    /**
     * Constructs a new Year 12 Subject object. Consists of a title that
     * may be multiple capitalised words, including numbers (in words or
     * digits) and/or Roman numerals (I,IV, etc.), each separated by a
     * SINGLE space, with NO leading or trailing spaces and no trailing
     * full stop (.), but other internal punctuation may be present -
     * (titles supplied with multiple spaces or leading or trailing spaces
     * must be rectified); AND a description, in whole English sentences,
     * each beginning with a capital letter and finishing with a full stop.
     *
     * @param title       the string title of this subject, formatted as
     *                    described above.
     * @param description the string description of this subject, in whole
     *                    sentences, each beginning with a capital and
     *                    finishing with a full stop, with words separated
     *                    by one or more spaces or other punctuation.
     * @param registry    the global object registry
     */
    public Subject(String title, String description, Registry registry) {
        this.title = sanitiseTitle(title);
        this.description = sanitiseDescription(description);
        registry.add(this, Subject.class);
    }

    /**
     * Constructs a Subject by reading a description from a text stream
     *
     * @param br       BufferedReader opened and ready to read from
     * @param registry the global object registry
     * @param nthItem  the index number of this serialized object
     * @throws IOException      on any read failure
     * @throws RuntimeException runtime exception
     */
    public Subject(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        streamIn(br, registry, nthItem);
        registry.add(this, Subject.class);
    }

    /**
     * Used to write data to the disk.
     * The format of the text written to the stream must be matched
     * exactly by streamIn, so it is very important to format the
     * output as described.
     * 1. ACCOUNTING
     * Accounting
     * "The study of the management of financial resources of the
     * public sector, businesses, and individuals."
     *
     * @param bw      writer, already opened. Your data should be written
     *                at the current file position
     * @param nthItem a number representing this item's position in the
     *                stream. Used for sanity checks
     * @throws IOException on any stream related issues
     */
    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        bw.write(nthItem + ". " + this);
        bw.write(title + System.lineSeparator());
        bw.write("\"" + description + "\"" + System.lineSeparator());
    }

    /**
     * Used to read data from the disk. IOExceptions and RuntimeExceptions
     * must be allowed to propagate out to the calling method, which
     * co-ordinates the streaming. Any other exceptions should be converted
     * to RuntimeExceptions and rethrown.
     * For the format of the text in the input stream, refer to the
     * streamOut documentation.
     *
     * @param br       reader, already opened.
     * @param registry the global object registry
     * @param nthItem  a number representing this item's position in the
     *                 stream. Used for sanity checks
     * @throws IOException      on any stream related issues.
     * @throws RuntimeException on any logic related issues
     */
    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        String heading = Utilities.getLine(br);
        if (heading == null) {
            throw new RuntimeException("EOF reading Subject #" + nthItem);
        }

        // split by period followed by space
        String[] firstLine = heading.split("\\. ");
        int index = Utilities.toInt(firstLine[0],
                "Number format exception parsing Subject "
                        + nthItem + " header");

        if (index != nthItem) {
            throw new RuntimeException("Subject index out of sync!");
        }

        String title = Utilities.getLine(br);
        if (title == null) {
            throw new RuntimeException("EOF reading Subject #" + nthItem);
        }

        this.title = sanitiseTitle(title);

        String description = Utilities.getLine(br);
        if (description == null) {
            throw new RuntimeException("EOF reading Subject #" + nthItem);
        }

        // remove double quotes
        this.description = sanitiseDescription(
                description.substring(1, description.length() - 1));
    }

    /**
     * Returns a detailed string representation of this subject.
     * Returns the title in all uppercase, then on a new line, the
     * entire text description inside double quotes.
     *
     * @return a string representation of this subject.
     */
    @Override
    public String getFullDetail() {
        return title.toUpperCase() + System.lineSeparator()
                + "\"" + description + "\"" + System.lineSeparator();
    }

    /**
     * return an Object[] containing class values suitable for use
     * in the view model
     *
     * @return an Object[] containing class values suitable for use
     *         in the view model
     */
    @Override
    public Object[] toTableRow() {
        return new Object[]{title};
    }

    /**
     * Return a unique string identifying us
     *
     * @return a unique string identifying us
     */
    @Override
    public String getId() {
        return title;
    }

    /**
     * Return a string from the input string, following these rules :-
     * there may be multiple capitalised words,
     * including numbers (in words or digits) and/or Roman numerals
     * (I,IV, etc.),
     * each separated by a SINGLE space,
     * with NO leading or trailing spaces, and
     * no trailing full stop (.), but other internal punctuation may
     * be present
     *
     * @param text the string to sanitise
     * @return the sanitised string
     */
    public String sanitiseTitle(String text) {
        // remove trailing periods
        text = text.replaceAll("\\.+$", "");
        // trim extra spaces
        text = text.trim();
        // Split by one or more spaces
        String[] words = text.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                result.append(" ");
            }
            String word = words[i];
            // might be roman numerals, dont lower
            if (word.matches("^[IVXLCDM]+$")) {
                result.append(word);
            } else {
                // cap first letter, lower the rest
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }

    /**
     * Return the string description of this subject, in whole sentences,
     * each beginning with a capital and finishing with a full stop,
     * with words separated by one or more spaces or other punctuation.
     *
     * @param text the string to sanitise
     * @return the sanitised string
     */
    public String sanitiseDescription(String text) {
        // Handle extra spaces
        text = text.replaceAll("\\s+", " ");
        text = text.trim();
        // make sure start with cap
        if (!text.isEmpty()) {
            text = Character.toUpperCase(text.charAt(0)) + text.substring(1);
        }
        // Ensure ends with period
        if (!text.isEmpty() && text.charAt(text.length() - 1) != '.') {
            text = text + ".";
        }
        return text;
    }

    /**
     * Gets the title of this subject.
     *
     * @return the String title of this subject.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Gets the text description of this subject.
     *
     * @return the String description of this subject.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns a brief string representation of this subject. Returns
     * the subject title in all uppercase.
     *
     * @return the subject title as a String in all uppercase and a
     *         newline.
     */
    @Override
    public String toString() {
        return title.toUpperCase() + System.lineSeparator();
    }

    /**
     * class specific equals method
     *
     * @param o the other object
     * @return true if they match, field for field, otherwise false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Subject other = (Subject) o;
        return other.title.equals(this.title)
                && other.description.equals(this.description);
    }

    /**
     * return the hash value of this object
     *
     * @return the hash value of this object
     */
    @Override
    public int hashCode() {
        return this.title.hashCode()
                + 2 * this.description.hashCode();
    }
}