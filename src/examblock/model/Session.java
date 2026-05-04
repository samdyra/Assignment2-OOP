package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * An object describing a single Exam Session. An exam "session" is a block
 * of time in a particular Venue, with zero or more Exams. Sessions are
 * numbered from 1 and unique in each venue, but not across venues. Session
 * number can be, but do not have to be, in chronological order of session
 * start times. That is, a new session may be inserted earlier into an
 * existing schedule. Session numbers do not have to necessarily be sequential.
 */
public class Session implements StreamManager, ManageableListItem {
    private Venue venue;
    private int sessionNumber;
    private LocalDate day;
    private LocalTime start;
    private List<Exam> exams;
    private Desk[][] desks;
    private Registry registry;

    /**
     * Constructs a new empty Exam Session for a particular Venue. The calling
     * process must check that the supplied session number is unique for this
     * venue. Session numbers do not have to be sequential, only unique. The
     * constructor must also prepare the empty (unassigned as yet) desks that
     * will be used in this session. (The session has the same rows and columns
     * of desks as the venue.)
     *
     * @param venue         the exam venue for the new session.
     * @param sessionNumber the number (unique by venue) of the new session.
     * @param day           the session date.
     * @param start         the start time of the exam window
     * @param registry      the global subject registry, needed to resolve
     *                      textual Subject names
     */
    public Session(Venue venue, int sessionNumber, LocalDate day,
                   LocalTime start, Registry registry) {
        this.venue = venue;
        this.sessionNumber = sessionNumber;
        this.day = day;
        this.start = start;
        this.registry = registry;

        // size of the venue
        int rows = this.venue.getRows();
        int columns = this.venue.getColumns();

        int deskNum = 1;

        for (int i = 0; i <= rows; i++) {
            for (int j = 0; j <= columns; j++) {
                if (deskNum <= venue.deskCount()) {
                    desks[i][j] = new Desk(deskNum);
                }
                deskNum++;
            }
        }

        registry.add(this, Session.class);
    }

    /**
     * Constructs a Session by reading a description from a text stream
     *
     * @param br       BufferedReader opened and ready to read from
     * @param registry the global registry, where we query and register new
     *                 list objects
     * @param nthItem  the index number of this serialized object
     * @throws IOException      on any read failure
     * @throws RuntimeException
     */
    public Session(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        this.streamIn(br, registry, nthItem);
        this.registry = registry;
        registry.add(this, Session.class);
    }

    /**
     * Used to write data to the disk.
     *
     * The format of the text written to the stream must be matched exactly by
     * streamIn, so it is very important to format the output as described.
     *
     * 1. Venue: V1+V2+V3, Session Number: 1, Day: 2025-03-10, Start: 12:30,
     * Student Count: 53, Exams: 2
     * Year 12 Internal Assessment Literature
     * [Desks: 36]
     * Desk: 1, LUI: 9999831170, Name: Ahmad, Tariq N.
     * ...
     * Year 12 Internal Assessment Essential English
     * [Desks: 17]
     * Desk: 64, LUI: 9999440022, Name: Brown, Noah J.
     * ...
     *
     * @param bw      writer, already opened. Your data should be written at
     *                the current file position
     * @param nthItem a number representing this item's position in the stream.
     *                Used for sanity checks
     * @throws IOException on any stream related issues
     */
    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        // venue header example:
        // 1. Venue: V1+V2+V3, Session Number: 1, Day: 2025-03-10, Start: 12:30,
        bw.write(nthItem + ". " + this.getFullDetail());

        // loop through exam
        for (Exam exam : exams) {
            // write exam name
            bw.write(exam.getShortTitle() + System.lineSeparator());

            // count desks
            int deskCount = 0;
            for (int col = 0; col < this.venue.getColumns(); col++) {
                for (int row = 0; row < this.venue.getRows(); row++) {
                    Desk desk = desks[row][col];
                    if (desk != null && desk.deskExam().equals(exam.abbrevShortTitle())) {
                        deskCount++;
                    }
                }
            }
            // write desk count example: [Desks: 36]
            bw.write("[Desks: " + deskCount + "]" + System.lineSeparator());

            // Write each desk assigned to this exam
            // example: Desk: 64, LUI: 9999440022, Name: Brown, Noah J.
            for (int col = 0; col < this.venue.getColumns(); col++) {
                for (int row = 0; row < this.venue.getRows(); row++) {
                    Desk desk = this.desks[row][col];
                    if (desk != null && desk.deskExam().equals(exam.abbrevShortTitle())) {
                        desk.streamOut(bw);
                    }
                }
            }
        }
    }

    /**
     * Used to read data from the disk. IOExceptions and RuntimeExceptions must
     * be allowed to propagate out to the calling method, which co-ordinates the
     * streaming. Any other exceptions should be converted to RuntimeExceptions
     * and rethrown.
     *
     * For the format of the text in the input stream, refer to the
     * {@code streamOut} documentation.
     *
     * @param br       reader, already opened.
     * @param registry the global object registry
     * @param nthItem  a number representing this item's position in the stream.
     *                 Used for sanity checks
     * @throws IOException      on any stream related issues
     * @throws RuntimeException on any logic related issues
     */
    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
    }

    /**
     * Returns a detailed string representation of this exam
     *
     * @return a detailed string representation of this exam.
     */
    @Override
    public String getFullDetail() {
        // example: 1. Venue: V1+V2+V3, Session Number: 1, Day: 2025-03-10, Start: 12:30, Student Count: 53, Exams: 2
        return "Venue: " + venue.venueId()
                + ", Session Number: " + sessionNumber
                + ", Day: " + day
                + ", Start: " + start
                + ", Student Count: " + countStudents()
                + ", Exams: " + exams.size()
                + System.lineSeparator();
    }

    /**
     * Return a unique string identifying us
     *
     * @return a unique string identifying us
     */
    @Override
    public String getId() {
        return venue.venueId() + "-" + sessionNumber;
    }

    /**
     * Gets the venue of this session.
     *
     * @return The venue of this session.
     */
    public Venue getVenue() {
        return this.venue;
    }

    /**
     * Gets the sessionNumber of this session.
     *
     * @return The sessionNumber of this session.
     */
    public int getSessionNumber() {
        return this.sessionNumber;
    }

    /**
     * Gets the date of this session.
     *
     * @return The date of this session.
     */
    public LocalDate getDate() {
        return this.day;
    }

    /**
     * Gets the start time of this session.
     *
     * @return The start time of this session.
     */
    public LocalTime getTime() {
        return this.start;
    }

    /**
     * Gets the list of exams being held in this session.
     *
     * @return The list of exams being held in this session.
     */
    public List<Exam> getExams() {
        return this.exams;
    }

    /**
     * Counts the number of student-exam pairs where the student's AARA status
     * matches the venue's AARA status and the student is enrolled in the
     * subject of an exam. A student may be counted multiple times if they
     * match multiple exams or subjects.
     *
     * @return the total count of matching student-exam pairs
     */
    public int countStudents() {
        return 0;
    }

    /**
     * Gets the desk at the specified row and column.
     *
     * @param row    the row index (0-based)
     * @param column the column index (0-based)
     * @return the desk at the given position
     */
    public Desk getDesk(int row, int column) {
        return desks[row][column];
    }

    /**
     * Return the total number of desks available here
     *
     * @return the total number of desks available here
     */
    public int getTotalDesks() {
        return venue.deskCount();
    }

    /**
     * Allocates an exam to this session (Venue and time).
     *
     * @param exam the exam to be allocated to this venue.
     */
    public void scheduleExam(Exam exam) {
        exams.add(exam);
    }

    /**
     * Removes the Allocation of an exam to this session (Venue and time).
     *
     * @param exam the exam to be deallocated from this venue.
     */
    public void removeExam(Exam exam) {
        exams.remove(exam);
    }

    /**
     * Allocates Students to Desks for every Exam in this Session.
     *
     * @param exams  the current set of Year 12 Exams.
     * @param cohort all the Year 12 students.
     */
    public void allocateStudents(ExamList exams, StudentList cohort) {
    }

    /**
     * Prints the layout of the desks in this session in the venue. Prints
     * a grid of the deskNumber, family name, and given name and initial
     * for each desk.
     */
    public void printDesks() {
        StringBuilder sb = new StringBuilder();
        printDesks(sb);
        System.out.println(sb);
    }

    /**
     * Appends the desk allocation to the provided StringBuilder. Replaces
     * PrintWriter output for Assignment 2.
     *
     * @param sb the StringBuilder to append to
     */
    public void printDesks(StringBuilder sb) {
        sb.append("Venue ").append(venue.venueId()).append(System.lineSeparator());
        for (int row = 0; row < venue.getRows(); row++) {
            for (int col = 0; col < venue.getColumns(); col++) {
                Desk desk = desks[row][col];
                if (desk != null) {
                    sb.append(String.format("%-20s", desk.toString()));
                }
            }
            sb.append(System.lineSeparator());
        }
    }

    /**
     * Returns a string representation of the session's state
     *
     * @return a string representation of the stats state
     */
    @Override
    public String toString() {
        return null;
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
        Session other = (Session) o;
        return other.venue.equals(this.venue)
                && other.sessionNumber == this.sessionNumber
                && other.day.equals(this.day)
                && other.start.equals(this.start);
    }

    /**
     * return the hash value of this object
     *
     * @return the hash value of this object
     */
    @Override
    public int hashCode() {
        return this.venue.hashCode()
                + 2 * this.sessionNumber
                + 3 * this.day.hashCode()
                + 5 * this.start.hashCode();
    }
}