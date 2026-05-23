package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * An object describing a single-semester Year 12 Unit of a Year 12 Subject.
 * These are typically Unit 3 or Unit 4 for the Year 12 units, but may be different.
 */
public class Unit implements StreamManager, ManageableListItem {
    /** the text description of this unit. */
    private String description;

    /** the title of this unit. */
    private String title;

    /** the single character unit identifier. */
    private Character unitId;

    /** the parent subject of this unit. */
    private Subject subject;

    /**
     * Constructs a new Subject Unit object. Consists of a parent Subject, the applicable
     * unitId (typically '3' or '4' for Year 12), as a single character (i.e. '0' to '9'
     * or 'A' to 'Z'); a unit title that may be multiple (optionally capitalised) words,
     * including numbers (in words or digits) and/or Roman numerals (I,IV, etc.), each
     * separated by a SINGLE space, with NO leading or trailing spaces and no trailing
     * full stop (.), but other internal punctuation may be present - (titles supplied with
     * multiple spaces or leading or trailing spaces must be rectified); AND a description,
     * in whole English sentences, each beginning with a capital letter and finishing with
     * a full stop.
     *
     * @param subject     the parent subject of this unit.
     * @param unitId      the single character unit identifier of this unit.
     * @param title       the string title of this unit, consisting of one or more capitalised
     *                    words separated by one or more spaces or other punctuation.
     * @param description the string description of this unit, in whole sentences, each
     *                    beginning with a capital and finishing with a full stop, with words
     *                    separated by one or more spaces or other punctuation.
     * @param registry    the global object registry
     */
    public Unit(Subject subject, Character unitId, String title,
                String description, Registry registry) {
        this.title = sanitiseTitle(title);
        this.description = sanitiseDescription(description);
        this.subject = subject;
        this.unitId = unitId;
        registry.add(this, Unit.class);
    }

    /**
     * Constructs a Unit by reading a description from a text stream
     *
     * @param br       BufferedReader opened and ready to read from
     * @param registry the global object registry, needed to resolve textual Subject names
     * @param nthItem  the index number of this serialized object
     * @throws IOException      on any read failure
     * @throws RuntimeException  any runtime error
     */
    public Unit(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        streamIn(br, registry, nthItem);
        registry.add(this, Unit.class);
    }

    /**
     * Used to write data to the disk.
     * The format of the text written to the stream must be matched exactly by streamIn,
     * so it is very important to format the output as described.
     * 3. ANCIENT HISTORY
     * Ancient History, Unit 3: Reconstructing the Ancient World
     * "Investigate significant historical periods through an analysis of relevant
     * archaeological and written sources."
     *
     * @param bw      writer, already opened. Your data should be written at the current
     *                file position
     * @param nthItem a number representing this item's position in the stream. Used for
     *                sanity checks
     * @throws IOException on any stream related issues
     */
    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        bw.write(nthItem + ". " + subject.getTitle().toUpperCase() + System.lineSeparator());
        bw.write(subject.getTitle() + ", Unit " + unitId + ": " + title + System.lineSeparator());
        bw.write("\"" + description + "\"" + System.lineSeparator());
    }

    /**
     * Used to read data from the disk. IOExceptions and RuntimeExceptions must be allowed
     * to propagate out to the calling method, which co-ordinates the streaming. Any other
     * exceptions should be converted to RuntimeExceptions and rethrown.
     * For the format of the text in the input stream, refer to the {@code streamOut}
     * documentation.
     *
     * @param br       reader, already opened.
     * @param registry the global object registry
     * @param nthItem  a number representing this item's position in the stream. Used for
     *                 sanity checks
     * @throws IOException      on any stream related issues
     * @throws RuntimeException on any logic related issues
     */
    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        String heading = Utilities.getLine(br);
        if (heading == null) {
            throw new RuntimeException("EOF reading Unit #" + nthItem);
        }

        // split by period followed by space
        String[] firstLine = heading.split("\\. ");
        int index = Utilities.toInt(firstLine[0], "Number format exception parsing Unit "
                + nthItem + " header");

        if (index != nthItem) {
            throw new RuntimeException("Unit index out of sync!");
        }

        String detail = Utilities.getLine(br);
        if (detail == null) {
            throw new RuntimeException("EOF reading Unit #" + nthItem);
        }

        String[] secondLine = detail.split(", Unit ");
        this.subject = registry.get(sanitiseTitle(secondLine[0]), Subject.class);

        String[] unitParts = secondLine[1].split(": ");
        this.unitId = unitParts[0].charAt(0);
        this.title = sanitiseTitle(unitParts[1]);

        String description = Utilities.getLine(br);
        if (description == null) {
            throw new RuntimeException("EOF reading Unit #" + nthItem);
        }

        // remove double quotes
        this.description = sanitiseDescription(description.substring(1, description.length() - 1));
    }

    /**
     * Returns a detailed string representation of this unit
     *
     * @return a detailed string representation of this unit.
     */
    @Override
    public String getFullDetail() {
        return subject.getTitle() + ", Unit " + unitId + ": " + title
                + System.lineSeparator()
                + "\"" + description + "\"" + System.lineSeparator();
    }

    /**
     * return an Object[] containing class values suitable for use in the view model
     *
     * @return an Object[] containing class values suitable for use in the view model
     */
    @Override
    public Object[] toTableRow() {
        return new Object[]{subject.getTitle(), unitId, title, description};
    }

    /**
     * Return a unique string identifying us
     *
     * @return a unique string identifying us
     */
    @Override
    public String getId() {
        return subject.getTitle() + ", Unit " + unitId + ": " + title;
    }

    /**
     * Gets the parent Subject of this unit.
     *
     * @return the reference to the unit's parent subject.
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     * Returns the identifier of this unit.
     *
     * @return the identifier of this unit.
     */
    public Character id() {
        return unitId;
    }

    /**
     * Gets the text description of the unit.
     *
     * @return the string description of the unit.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns a brief string representation of this unit.
     *
     * @return the unitID and title of this unit.
     */
    @Override
    public String toString() {
        return unitId + " " + title;
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
        Unit other = (Unit) o;
        return other.subject.equals(this.subject)
                && other.unitId.equals(this.unitId)
                && other.title.equals(this.title)
                && other.description.equals(this.description);
    }

    /**
     * return the hash value of this object
     *
     * @return the hash value of this object
     */
    @Override
    public int hashCode() {
        return this.subject.hashCode()
                + 2 * this.unitId.hashCode()
                + 3 * this.title.hashCode()
                + 5 * this.description.hashCode();
    }

    private String sanitiseTitle(String text) {
        // remove trailing periods
        text = text.replaceAll("\\.+$", "");
        // trim and collapse multiple spaces into single space
        text = text.trim().replaceAll("\\s+", " ");
        return text;
    }

    private String sanitiseDescription(String text) {
        // handle extra spaces
        text = text.replaceAll("\\s+", " ");
        text = text.trim();
        // make sure starts with cap
        if (!text.isEmpty()) {
            text = Character.toUpperCase(text.charAt(0)) + text.substring(1);
        }
        // ensure ends with period
        if (!text.isEmpty() && text.charAt(text.length() - 1) != '.') {
            text = text + ".";
        }
        return text;
    }
}