package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
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
    /** the exam venue for this session. */
    private Venue venue;

    /** the session number, unique within a venue. */
    private int sessionNumber;

    /** the date of this session. */
    private LocalDate day;

    /** the start time of this session. */
    private LocalTime start;

    /** the list of exams scheduled in this session. */
    private List<Exam> exams;

    /** the 2D grid of desks in this session. */
    private Desk[][] desks;

    /** the global object registry. */
    private Registry registry;

    /** the number of rows of desks. */
    private int rows;

    /** the number of columns of desks. */
    private int columns;

    /** the total number of desks available. */
    private int totalDesks;

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
        this.exams = new ArrayList<>();
        initializeDesks();

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
     * @throws RuntimeException runtime exception
     */
    public Session(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        this.registry = registry;
        streamIn(br, registry, nthItem);
        registry.add(this, Session.class);
    }

    /**
     * Used to write data to the disk.
     * The format of the text written to the stream must be matched exactly by
     * streamIn, so it is very important to format the output as described.
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
        this.registry = registry;
        this.exams = new ArrayList<>();

        int examCount = parseSessionHeader(br, nthItem);
        initializeDesks();

        for (int examIndex = 0; examIndex < examCount; examIndex++) {
            readExamAndDesks(br, nthItem);
        }
    }

    /**
     * Returns a detailed string representation of this exam
     *
     * @return a detailed string representation of this exam.
     */
    @Override
    public String getFullDetail() {
        // example: 1. Venue: V1+V2+V3, Session Number: 1,
        // Day: 2025-03-10, Start: 12:30, Student Count: 53, Exams: 2
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
        int count = 0;
        // Get all students from registry
        List<Student> allStudents = registry.getAll(Student.class);
        for (Exam exam : exams) {
            Subject examSubject = exam.getSubject();
            for (Student student : allStudents) {
                // match AARA Status
                if (student.isAara() == venue.isAara()) {
                    // check if the student take the subject
                    for (Subject studentSubject : student.getSubjects().all()) {
                        if (studentSubject.equals(examSubject)) {
                            count++;
                            break;
                        }
                    }
                }
            }
        }
        return count;
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
     * @param allExams the current set of Year 12 Exams.
     * @param cohort   all the Year 12 students.
     */
    public void allocateStudents(ExamList allExams, StudentList cohort) {
        detectClashes(cohort);

        int totalStudents = countStudents();
        if (totalStudents > totalDesks) {
            return;
        }

        // sort all students alphabetically
        List<Student> allStudents = cohort.all();
        sortStudentsAlphabetically(allStudents);

        if (exams.size() == 1) {
            // single exam (maximize spacing between students)
            int studentCols = (totalStudents + rows - 1) / rows;
            int emptyCols = columns - studentCols;
            int colGap = 0;
            if (studentCols > 1 && emptyCols % (studentCols - 1) == 0) {
                colGap = emptyCols / (studentCols - 1);
            }
            allocateExamStudents(exams.getFirst(), allStudents, 1, colGap);
        } else {
            // multiple exams -> inter-exam gaps take priority (note: see example in bb)
            // "Gaps between exams take priority over gaps between student columns."
            int totalStudentCols = 0;
            for (Exam exam : exams) {
                int count = countExamStudents(exam, allStudents);
                totalStudentCols += (count + rows - 1) / rows;
            }
            int emptyCols = columns - totalStudentCols;
            int interGap = emptyCols / (exams.size() - 1);
            int interRemainder = emptyCols % (exams.size() - 1);

            int nextCol = 0;
            int lastExamIndex = exams.size() - 1;
            for (int i = 0; i < exams.size(); i++) {
                if (i == lastExamIndex) {
                    // last exam: right-aligned so last student hits last desk
                    int lastExamStudents = countExamStudents(
                            exams.get(i), allStudents);
                    int lastStartDesk = totalDesks - lastExamStudents + 1;
                    allocateExamStudents(exams.get(i), allStudents,
                            lastStartDesk, 0);
                } else {
                    // other exams: left-to-right from calculated position
                    int startDesk = nextCol * rows + 1;
                    int count = countExamStudents(exams.get(i), allStudents);
                    int examCols = (count + rows - 1) / rows;
                    allocateExamStudents(exams.get(i), allStudents,
                            startDesk, 0);
                    nextCol += examCols + interGap
                            + (i < interRemainder ? 1 : 0);
                }
            }
        }
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

        int colWidth = 20;

        for (int row = 0; row < rows; row++) {
            StringBuilder deskLine = new StringBuilder();
            StringBuilder nameLine = new StringBuilder();
            StringBuilder givenLine = new StringBuilder();

            for (int col = 0; col < columns; col++) {
                Desk desk = desks[row][col];
                if (desk != null) {
                    deskLine.append(String.format("%-" + colWidth + "s",
                            "Desk " + desk.deskNumber() + ":"));
                    nameLine.append(String.format("%-" + colWidth + "s",
                            desk.deskFamilyName() != null
                                    ? desk.deskFamilyName() : ""));
                    givenLine.append(String.format("%-" + colWidth + "s",
                            desk.deskGivenAndInit() != null
                                    ? desk.deskGivenAndInit() : ""));
                }
            }

            sb.append(deskLine.toString().stripTrailing()).append(System.lineSeparator());
            sb.append(nameLine.toString().stripTrailing()).append(System.lineSeparator());
            sb.append(givenLine.toString().stripTrailing()).append(System.lineSeparator());
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
        return venue.venueId()
                + ": " + sessionNumber
                + ": " + day
                + " " + start;
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

    // HELPER FUNCTIONS FOR ALLOCATION

    // Detect students who are scheduled for multiple exams in this session
    // and flag them as clashes.
    private void detectClashes(StudentList cohort) {
        // Collect all exam subjects in this session
        List<Subject> examSubjects = new ArrayList<>();
        for (Exam exam : exams) {
            examSubjects.add(exam.getSubject());
        }

        // Check each student for multiple matches
        for (Student student : cohort.all()) {
            if (student.isAara() != venue.isAara()) {
                continue;
            }
            int matchCount = 0;
            for (Subject examSubject : examSubjects) {
                for (Subject studentSubject : student.getSubjects().all()) {
                    if (studentSubject.equals(examSubject)) {
                        matchCount++;
                        break;
                    }
                }
            }
            if (matchCount > 1) {
                System.out.println("CLASH: " + student.fullName()
                        + " is scheduled for " + matchCount
                        + " exams in this session!");
            }
        }
    }

    // Sort students alphabetically by family name, then given names.
    private void sortStudentsAlphabetically(List<Student> students) {
        students.sort(Comparator.comparing(Student::familyName));
    }

    // Allocate students for a single exam to desks, starting from nextDesk.
    private void allocateExamStudents(Exam exam, List<Student> allStudents,
                                     int nextDesk, int colGap) {
        Subject subject = exam.getSubject();
        int studentsInCol = 0;

        for (Student student : allStudents) {
            if (student.isAara() != venue.isAara()) {
                continue;
            }
            boolean takesSubject = false;
            for (Subject studentSubject : student.getSubjects().all()) {
                if (studentSubject.equals(subject)) {
                    takesSubject = true;
                    break;
                }
            }
            if (!takesSubject) {
                continue;
            }

            int col = (nextDesk - 1) / rows;
            int row = (nextDesk - 1) % rows;

            if (row < rows && col < columns && desks[row][col] != null) {
                desks[row][col].setStudent(student);
                desks[row][col].setExam(exam);
            }

            studentsInCol++;
            nextDesk++;

            // after fill the col, skip colGap columns
            if (studentsInCol == rows && colGap > 0) {
                nextDesk += colGap * rows;
                studentsInCol = 0;
            }
        }
    }

    // count exam students for a single exam, used for allocation
    private int countExamStudents(Exam exam, List<Student> allStudents) {
        int count = 0;
        for (Student student : allStudents) {
            if (student.isAara() != venue.isAara()) {
                continue;
            }
            for (Subject s : student.getSubjects().all()) {
                if (s.equals(exam.getSubject())) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    // HELPER FUNCTIONS FOR ALLOCATION (END)

    // parse the session header line and set venue, session number, day, start.
    private int parseSessionHeader(BufferedReader br, int nthItem) {
        String header = Utilities.getLine(br);
        if (header == null) {
            throw new RuntimeException("EOF reading Session #" + nthItem);
        }

        String[] headerParts = header.split("\\. ");
        int index = Utilities.toInt(headerParts[0],
                "Number format exception parsing Session " + nthItem + " header");
        if (index != nthItem) {
            throw new RuntimeException("Session index out of sync!");
        }

        int examCount = 0;
        String[] details = headerParts[1].split(",");
        for (String detail : details) {
            String[] pair = Utilities.keyValuePair(detail.trim());
            if (pair == null) {
                continue;
            }
            String detailKey = pair[0];
            String detailValue = pair[1];

            switch (detailKey) {
                case "Venue":
                    this.venue = registry.get(detailValue, Venue.class);
                    break;
                case "Session Number":
                    this.sessionNumber = Utilities.toInt(detailValue,
                            "Number format exception parsing Session "
                                    + nthItem + " Session Number");
                    break;
                case "Day":
                    this.day = Utilities.toLocalDate(detailValue,
                            "Date format error parsing Session " + nthItem + " Day");
                    break;
                case "Start":
                    this.start = Utilities.toLocalTime(detailValue,
                            "Time format error parsing Session " + nthItem + " Start");
                    break;
                case "Exams":
                    examCount = Utilities.toInt(detailValue,
                            "Number format exception parsing Session "
                                    + nthItem + " Exams");
                    break;
            }
        }

        return examCount;
    }

    // create empty desk grid that match venue dimensions and assign desk numbers
    private void initializeDesks() {
        this.rows = venue.getRows();
        this.columns = venue.getColumns();
        this.totalDesks = venue.deskCount();
        this.desks = new Desk[rows][columns];

        int deskNumber = 1;
        for (int col = 0; col < columns; col++) {
            for (int row = 0; row < rows; row++) {
                if (deskNumber <= totalDesks) {
                    desks[row][col] = new Desk(deskNumber);
                }
                deskNumber++;
            }
        }
    }

    // read one exam title and its desk allocations from the stream.
    private void readExamAndDesks(BufferedReader br, int nthItem) {
        // exam title example "Year 12 Internal Assessment Literature"
        String examTitle = Utilities.getLine(br);
        if (examTitle == null) {
            throw new RuntimeException("EOF reading Session " + nthItem + " exam");
        }

        Exam exam = registry.get(examTitle, Exam.class);
        exams.add(exam);

        // "[Desks: 36]"
        String desksHeader = Utilities.getLine(br);
        if (desksHeader == null) {
            throw new RuntimeException("EOF reading Session " + nthItem + " desks header");
        }

        String desksCountStr = desksHeader.substring(
                desksHeader.indexOf(":") + 2,
                desksHeader.indexOf("]"));
        int desksCount = Utilities.toInt(desksCountStr,
                "Number format exception parsing desk count");

        // read all desk allocation
        for (int deskIndex = 0; deskIndex < desksCount; deskIndex++) {
            readDeskAllocation(br, exam, nthItem);
        }
    }

    // Read a single desk allocation line and place it in the grid.
    private void readDeskAllocation(BufferedReader br, Exam exam, int nthItem)
            throws RuntimeException {
        String deskLine = Utilities.getLine(br);
        if (deskLine == null) {
            throw new RuntimeException("EOF reading desk in Session " + nthItem);
        }

        int readDeskNumber = 0;
        long readLui = 0;
        String readGivenAndInit = "";

        String[] deskDetails = deskLine.split(",");
        for (String detail : deskDetails) {
            String[] pair = Utilities.keyValuePair(detail.trim());
            if (pair == null) {
                readGivenAndInit = detail.trim();
                continue;
            }
            switch (pair[0]) {
                case "Desk":
                    readDeskNumber = Utilities.toInt(pair[1],
                            "Number format exception parsing desk number");
                    break;
                case "LUI":
                    readLui = Utilities.toLong(pair[1],
                            "Number format exception parsing desk LUI");
                    break;
            }
        }

        // place into desk in the grid
        int col = (readDeskNumber - 1) / venue.getRows();
        int row = (readDeskNumber - 1) % venue.getRows();
        if (desks[row][col] != null) {
            Student student = registry.get(String.valueOf(readLui), Student.class);
            desks[row][col].setStudent(student);
            desks[row][col].setGivenAndInit(readGivenAndInit);
            desks[row][col].setExam(exam);
        }
    }
}