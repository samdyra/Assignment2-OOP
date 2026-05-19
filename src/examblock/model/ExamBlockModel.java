package examblock.model;

import java.util.List;
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import examblock.view.components.FileChooser;

/**
 * The M in the MVC model - this is the source of truth for all the
 * data in the app
 */
public class ExamBlockModel {
    private Registry registry;
    private String title;
    private double version;
    private String filename;
    private SubjectList subjects;
    private UnitList units;
    private StudentList students;
    private ExamList exams;
    private RoomList rooms;
    private VenueList venues;
    private SessionList sessions;
    private final List<ModelObserver> observers;

    /**
     * Constructor
     */
    public ExamBlockModel() {
        this.registry = new Registry();
        this.title = "";
        this.version = 0;
        this.filename = "";
        this.subjects = new SubjectList(registry);
        this.units = new UnitList(registry);
        this.students = new StudentList(registry);
        this.exams = new ExamList(registry);
        this.rooms = new RoomList(registry);
        this.venues = new VenueList(registry);
        this.sessions = new SessionList(registry);
        this.observers = new ArrayList<>();
    }

    /**
     * Add an observer
     *
     * @param observer the new observer to be called on update
     */
    public void addObserver(ModelObserver observer) {
        observers.add(observer);
    }

    /**
     * Notify observers that data has changed
     *
     * @param property a string naming the property
     */
    public void notifyObservers(String property) {
        for (ModelObserver observer : observers) {
            observer.modelChanged(property);
        }
    }

    /**
     * get the most up-to-date ExamList from the source of truth,
     * the registry
     *
     * @return the ExamList
     */
    public ExamList getExams() {
        return exams;
    }

    /**
     * get the most up-to-date VenueList from the source of truth,
     * the registry
     *
     * @return the VenueList
     */
    public VenueList getVenues() {
        return venues;
    }

    /**
     * get the most up-to-date RoomList from the source of truth,
     * the registry
     *
     * @return the RoomList
     */
    public RoomList getRooms() {
        return rooms;
    }

    /**
     * get the most up-to-date StudentList from the source of truth,
     * the registry
     *
     * @return the StudentList
     */
    public StudentList getStudents() {
        return students;
    }

    /**
     * get the most up-to-date SessionList from the source of truth,
     * the registry
     *
     * @return the SessionList
     */
    public SessionList getSessions() {
        return sessions;
    }

    /**
     * get the most up-to-date SubjectList from the source of truth,
     * the registry
     *
     * @return the SubjectList
     */
    public SubjectList getSubjects() {
        return subjects;
    }

    /**
     * get the most up-to-date UnitList from the source of truth,
     * the registry
     *
     * @return the UnitList
     */
    public UnitList getUnits() {
        return units;
    }

    /**
     * return the Exam Block title
     *
     * @return the Exam Block title
     */
    public String getTitle() {
        return title;
    }

    /**
     * change the title of an Exam Block. Observers advised
     *
     * @param title to change
     */
    public void setTitle(String title) {
        this.title = title;
        notifyObservers("title");
    }

    /**
     * get the current version number
     *
     * @return the version number
     */
    public double getVersion() {
        return version;
    }

    /**
     * change the version of the Exam Block. Has to be greater than
     * the current version. Observers advised
     *
     * @param version the new version.
     */
    public void setVersion(double version) {
        if (version > this.version) {
            this.version = version;
        }
        notifyObservers("version");
    }

    /**
     * get the currenly loaded filename
     *
     * @return filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * change the filename. Observers advised
     *
     * @param filename new filename
     */
    public void setFilename(String filename) {
        this.filename = filename;
        notifyObservers("filename");
    }

    /**
     * returns the registry
     *
     * @return the registry
     */
    public Registry getRegistry() {
        return registry;
    }

    /**
     * Save a registry to disk. This file format has to match the format
     * expected by loadFromFile. If the file cannot be successfully saved,
     * log a message to the console and return false.
     *
     * @param registry the registry to save
     * @param filename the filename to save to. If null, a dialog is shown
     *                 to ask for a filename.
     * @param title    the Exam Block title
     * @param version  the current version number
     * @return true is file saved, otherwise false
     */
    public boolean saveToFile(Registry registry, String filename,
                              String title, double version) {
        // If no filename, ask user for one
        if (filename == null) {
            FileChooser chooser = new FileChooser(title, version);
            String selectedFilename = chooser.save(this.filename, Utilities.FileType.EBD);
            if (selectedFilename == null || selectedFilename.isEmpty()) {
                return false;
            }
            filename = selectedFilename;
        }

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filename));

            // write header
            bw.write("Title: " + title + System.lineSeparator());
            bw.write("Version: " + version + System.lineSeparator());
            bw.write(System.lineSeparator());
            bw.write("[Begin]" + System.lineSeparator());
            bw.write(System.lineSeparator());

            int placeholder = 0; // placeholder for nthItem

            // order following the example
            subjects.streamOut(bw, placeholder);
            units.streamOut(bw, placeholder);
            students.streamOut(bw, placeholder);
            exams.streamOut(bw, placeholder);
            rooms.streamOut(bw, placeholder);
            venues.streamOut(bw, placeholder);
            sessions.streamOut(bw, placeholder);

            bw.write("[End]" + System.lineSeparator());
            bw.close();

            this.filename = filename;
            this.title = title;
            this.version = version;

            return true;
        } catch (IOException e) {
            System.out.println("Unable to save file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Load a registry from disk. This version will prompt the user for
     * a file to load.
     */
    public void loadFromFile() {
        FileChooser chooser = new FileChooser();
        java.io.File selectedFile = chooser.open(filename, Utilities.FileType.EBD);
        if (selectedFile != null) {
            loadFromFile(new Registry(), selectedFile.getAbsolutePath());
        }
    }

    /**
     * Load a registry from disk. This file format has to match the format
     * written by saveToFile. If the file cannot be loaded due to an
     * IOException or RuntimeException, terminates the app.
     *
     * @param registry the registry to fill
     * @param filename the filename to load from
     */
    public void loadFromFile(Registry registry, String filename) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));

            // title example: "Title: ExamBlock Data 2025"
            String titleLine = Utilities.getLine(br);
            String[] titleParts = Utilities.keyValuePair(titleLine);

            if (titleParts == null) {
                throw new RuntimeException("Invalid file format: missing Title");
            }

            this.title = titleParts[1];

            // read version example "Version: 1.0"
            String versionLine = Utilities.getLine(br);
            String[] versionParts = Utilities.keyValuePair(versionLine);

            if (versionParts == null) {
                throw new RuntimeException("Invalid file format: missing Version");
            }

            this.version = Utilities.toDouble(versionParts[1],
                    "Number format exception parsing version");

            Utilities.getLine(br);

            this.registry = registry;
            this.subjects = new SubjectList(registry);
            this.units = new UnitList(registry);
            this.students = new StudentList(registry);
            this.exams = new ExamList(registry);
            this.rooms = new RoomList(registry);
            this.venues = new VenueList(registry);
            this.sessions = new SessionList(registry);

            int placeholder = 0;

            subjects.streamIn(br, registry, placeholder);
            units.streamIn(br, registry, placeholder);
            students.streamIn(br, registry, placeholder);
            exams.streamIn(br, registry, placeholder);
            rooms.streamIn(br, registry, placeholder);
            venues.streamIn(br, registry, placeholder);
            sessions.streamIn(br, registry, placeholder);

            Utilities.getLine(br);
            br.close();

            this.filename = filename;
            notifyObservers("loaded");

        } catch (IOException | RuntimeException e) {
            System.out.println("Unable to load file: " + e.getMessage());
            System.exit(1);
        }
    }
}