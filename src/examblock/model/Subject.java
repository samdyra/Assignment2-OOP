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

    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        bw.write(nthItem + ". " + this + System.lineSeparator());
        bw.write();

    }

    public void streamIn(BufferedReader br, Registry registry, int nthItem) throws IOException, RuntimeException {

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
