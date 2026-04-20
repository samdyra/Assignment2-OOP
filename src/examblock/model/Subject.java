package examblock.model;

import examblock.view.components.Verbose;

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

    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        bw.write(nthItem + ". " + this + System.lineSeparator());
        bw.write(title + System.lineSeparator());
        bw.write("\"" + description + "\"" + System.lineSeparator());
    }

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

    public String getFullDetail() {

    }

    public Object[] toTableRow() {

    }

    public String getId() {

    }

    public String sanitiseTitle(String text) {

    }

    public String sanitiseDescription(String text) {

    }

    public String getTitle() {

    }

    public String getDescription() {

    }

    public String toString() {

    }

    public boolean equals(Object o) {

    }

    public int hashCode() {

    }


}
