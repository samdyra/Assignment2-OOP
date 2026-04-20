package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class Subject implements StreamManager, ManageableListItem {
    private String description;
    private String title;

    public Subject(String title, String description, Registry registry) {
        this.title = sanitiseTitle(title);
        this.description = sanitiseDescription(description);
        registry.add(this, Subject.class);
    }

    public Subject(BufferedReader br, Registry registry, int nthItem) throws IOException, RuntimeException {
        streamIn(br, registry, nthItem);
        registry.add(this, Subject.class);
    }

    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        bw.write(nthItem + ". " + this);
        bw.write(title + System.lineSeparator());
        bw.write("\"" + description + "\"" + System.lineSeparator());
    }

    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem) throws IOException, RuntimeException {
        String heading = Utilities.getLine(br);
        if (heading == null) {
            throw new RuntimeException("EOF reading Subject #" + nthItem);
        }

        var bits = heading.split("\\. ");
        int index = Utilities.toInt(bits[0], "Number format exception parsing Subject "
                + nthItem
                + " header");
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

        this.description = sanitiseDescription(description.substring(1, description.length() - 1));
    }

    @Override
    public String getFullDetail() {
        return title.toUpperCase() + System.lineSeparator()
                + "\"" + description + "\"" + System.lineSeparator();
    }

    @Override
    public Object[] toTableRow() {
        return new Object[]{title};
    }

    @Override
    public String getId() {
        return title;
    }

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
            // cap first letter, lower the rest
            result.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase());
        }
        return result.toString();
    }

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

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return title.toUpperCase() + System.lineSeparator();
    }

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

    @Override
    public int hashCode() {
        return this.title.hashCode()
                + 2 * this.description.hashCode();
    }
}
