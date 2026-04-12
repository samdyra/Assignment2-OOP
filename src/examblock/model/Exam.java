package examblock.model;

import examblock.view.components.Verbose;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * An object describing a single Year 12 Exam.
 */
public class Exam implements StreamManager, ManageableListItem {

    /**
     * An enum for the ExamType (INTERNAL or EXTERNAL).
     */
    public enum ExamType {
        /**
         * Internal assessment, conducted by the school.
         */
        INTERNAL,
        /**
         * External assessment, conducted by the QCAA.
         */
        EXTERNAL
    }

    /**
     * The Subject.
     */
    private Subject subject;

    /**
     * INTERNAL or EXTERNAL.
     */
    private ExamType examType;

    /**
     * Optional paper number (null or 1 or 2).
     */
    private Character paper;

    /**
     * Optional subtitle e.g. "Technology Free".
     */
    private String subtitle;

    /**
     * Optional unit ID if only one unit applicable.
     */
    private Character unit;

    /**
     * The date of the exam.
     */
    private LocalDate examDate;

    /**
     * The time of the exam.
     */
    private LocalTime examTime;

    /**
     * Constructs an {@code Exam} with minimal details.
     *
     * @param subject  the exam Subject.
     * @param examType one of INTERNAL or EXTERNAL.
     * @param day      the integer day of the date of the exam, which must
     *                 be a valid day for the month and year provided.
     * @param month    the integer month of the date of the exam, which
     *                 must be between 1 - 12 inclusive.
     * @param year     the 4-digit integer year of the date of the exam,
     *                 which must be 2025 or greater.
     * @param hour     the 2-digit integer hour of the start of the exam window,
     *                 in 24-hour time, which must be between 7 and 17.
     * @param minute   the integer minute of the start of the exam window,
     *                 which must be between 0 - 59 inclusive.
     * @param registry the global registry, where we query and register new list objects
     */
    public Exam(Subject subject, ExamType examType,
                int day, int month, int year, int hour, int minute, Registry registry) {
        this(subject, examType, '\0', "", '\0', day, month, year, hour, minute, registry);
    }

    /**
     * Constructs an {@code Exam} with the optional unit specified.
     *
     * @param subject  the exam Subject.
     * @param examType one of INTERNAL or EXTERNAL.
     * @param unit     an optional unit ID if only one unit is applicable.
     * @param day      the integer day of the date of the exam, which must
     *                 be a valid day for the month and year provided.
     * @param month    the integer month of the date of the exam, which
     *                 must be between 1 - 12 inclusive.
     * @param year     the 4-digit integer year of the date of the exam,
     *                 which must be 2025 or greater.
     * @param hour     the 2-digit integer hour of the start of the exam window,
     *                 in 24-hour time, which must be between 7 and 17.
     * @param minute   the integer minute of the start of the exam window,
     *                 which must be between 0 - 59 inclusive.
     * @param registry the global registry, where we query and register new list objects
     */
    public Exam(Subject subject, ExamType examType, Character unit,
                int day, int month, int year, int hour, int minute, Registry registry) {
        this(subject, examType, '\0', "", unit, day, month, year, hour, minute, registry);
    }

    /**
     * Constructs an {@code Exam} with paper number and subtitle but no unit specified.
     *
     * @param subject  the exam Subject.
     * @param examType one of INTERNAL or EXTERNAL.
     * @param paper    an optional paper number (null or 1 or 2).
     * @param subtitle an optional subtitle e.g. "Technology Free".
     * @param day      the integer day of the date of the exam, which must
     *                 be a valid day for the month and year provided.
     * @param month    the integer month of the date of the exam, which
     *                 must be between 1 - 12 inclusive.
     * @param year     the 4-digit integer year of the date of the exam,
     *                 which must be 2025 or greater.
     * @param hour     the 2-digit integer hour of the start of the exam window,
     *                 in 24-hour time, which must be between 7 and 17.
     * @param minute   the integer minute of the start of the exam window,
     *                 which must be between 0 - 59 inclusive.
     * @param registry the global registry, where we query and register new list objects
     */
    public Exam(Subject subject, ExamType examType, Character paper, String subtitle,
                int day, int month, int year, int hour, int minute, Registry registry) {
        this(subject, examType, paper, subtitle, '\0', day, month, year, hour, minute,
                registry);
    }

    /**
     * Constructs an {@code Exam} with all optional details provided.
     *
     * @param subject  the exam Subject.
     * @param examType one of INTERNAL or EXTERNAL.
     * @param paper    an optional paper number (null or 1 or 2).
     * @param subtitle an optional subtitle e.g. "Technology Free".
     * @param unit     an optional unit ID if only one unit is applicable.
     * @param day      the integer day of the date of the exam, which must
     *                 be a valid day for the month and year provided.
     * @param month    the integer month of the date of the exam, which
     *                 must be between 1 - 12 inclusive.
     * @param year     the 4-digit integer year of the date of the exam,
     *                 which must be 2025 or greater.
     * @param hour     the 2-digit integer hour of the start of the exam window,
     *                 in 24-hour time, which must be between 7 and 17.
     * @param minute   the integer minute of the start of the exam window,
     *                 which must be between 0 - 59 inclusive.
     * @param registry the global registry, where we register new exams
     */
    public Exam(Subject subject, ExamType examType, Character paper, String subtitle,
                Character unit, int day, int month, int year, int hour, int minute,
                Registry registry) {
        this.subject = subject;
        this.examType = examType;
        this.paper = paper;
        this.subtitle = subtitle;
        this.unit = unit;

        this.examDate = LocalDate.of(year, month, day);
        this.examTime = LocalTime.of(hour, minute);

        registry.add(this, Exam.class);
    }

    /**
     * Constructs an Exam by reading a description from a text stream
     *
     * @param br       BufferedReader opened and ready to read from
     * @param registry the global registry, where we query and register new list objects
     * @param nthItem  the index number of this serialized object
     * @throws IOException on any read failure
     */
    public Exam(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {

        streamIn(br, registry, nthItem);

        registry.add(this, Exam.class);
    }

    /**
     * Used to write data to the disk.<br>
     * <br>
     * The format of the text written to the stream must be matched exactly by streamIn, so it
     * is very important to format the output as described.<br>
     * <br>
     * IF THERE IS NO PAPER AND SUBTITLE:<br>
     * 4. Year 12 Internal Assessment General Mathematics<br>
     * Subject: General Mathematics, Exam Type: INTERNAL, Unit: 3, Exam Date: 2025-03-11 08:30<br>
     * <br>
     * IF THERE IS A PAPER AND SUBTITLE:<br>
     * 5. Year 12 Internal Assessment Mathematical Methods Paper 1<br>
     * Subject: Mathematical Methods, Exam Type: INTERNAL, Paper: 1, Subtitle: Technology Free, Unit: 3, Exam Date: 2025-03-11 12:30<br>
     * <br>
     *
     * @param bw      writer, already opened. Your data should be written at the current
     *                file position
     * @param nthItem a number representing this item's position in the stream. Used for sanity
     *                checks
     * @throws IOException on any stream related issues
     */
    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        String text = nthItem + ". " + this + System.lineSeparator()
                + getFullDetail();

        bw.write(text);
    }

    /**
     * Used to read data from the disk. IOExceptions and RuntimeExceptions must be allowed
     * to propagate out to the calling method, which co-ordinates the streaming. Any other
     * exceptions should be converted to RuntimeExceptions and rethrown.<br>
     * <br>
     * For the format of the text in the input stream, refer to the {@code streamOut} documentation.
     *
     * @param br       reader, already opened.
     * @param registry the global object registry
     * @param nthItem  a number representing this item's position in the stream. Used for sanity
     *                 checks
     * @throws IOException      on any stream related issues
     * @throws RuntimeException on any logic related issues
     */
    @Override
    public void streamIn(BufferedReader br,
                         Registry registry,
                         int nthItem) throws IOException, RuntimeException {

        //    1. ENGLISH

        String heading = Utilities.getLine(br);
        if (heading == null) {
            throw new RuntimeException("EOF reading Exam #" + nthItem);
        }

        var bits = heading.split("\\. ");
        int index = Utilities.toInt(bits[0], "Number format exception parsing Exam "
                + nthItem
                + " header");

        if (index != nthItem) {
            throw new RuntimeException("Exam index out of sync!");
        }

        String line = Utilities.getLine(br);
        if (line == null) {
            throw new RuntimeException("EOF reading Exam #" + nthItem);
        }

        var details = line.split(",");
        int detailIndex = 0;

        this.paper = '\0';
        this.subtitle = "";

        while (detailIndex < details.length) {

            String thisPair = details[detailIndex++].trim();
            var nextDetailPair = Utilities.keyValuePair(thisPair);

            switch (Objects.requireNonNull(nextDetailPair)[0]) {
                case "Subject":
                    String subjectName = nextDetailPair[1];
                    this.subject = registry.get(subjectName, Subject.class);
                    break;

                case "Exam Type":
                    String examTypeName = nextDetailPair[1];
                    this.examType = Exam.ExamType.valueOf(examTypeName);
                    break;

                case "Paper":
                    this.paper = nextDetailPair[1].charAt(0);
                    break;

                case "Subtitle":
                    this.subtitle = nextDetailPair[1];
                    break;

                case "Unit":
                    this.unit = nextDetailPair[1].charAt(0);
                    break;

                case "Exam Date":
                    String[] dateParts = nextDetailPair[1].split(" ");
                    this.examDate = Utilities.toLocalDate(dateParts[0],
                            "Date format error parsing Exam " + nthItem + " Date");
                    this.examTime = Utilities.toLocalTime(dateParts[1],
                            "Time format error parsing Exam " + nthItem + " Date");
                    break;

            }
        }

        if (paper == '\0' && !subtitle.isEmpty() || paper != '\0' && subtitle.isEmpty()) {
            throw new RuntimeException("Paper and Subtitle must be both given, "
                    + "or neither given, parsing Exam " + nthItem);
        }

        if (Verbose.isVerbose()) {
            System.out.println("Loaded Exam: " + this);
        }
    }

    /**
     * Returns a detailed string representation of this exam. The output needs to include
     * the subject,
     * exam type,
     * paper (if applicable),
     * subtitle (if applicable),
     * the unit (if applicable), and
     * the exam date and time.
     *
     * @return a detailed string representation of this exam.
     */
    @Override
    public String getFullDetail() {
        StringBuilder text = new StringBuilder();
        text.append("Subject: ");
        text.append(subject.getTitle());
        text.append(", Exam Type: ");
        text.append(examType.toString());
        if (paper == '1' || paper == '2') {
            text.append(", Paper:");
            text.append(" ");
            text.append(paper);
        }
        if (!subtitle.isEmpty()) {
            text.append(", Subtitle:");
            text.append(" ");
            text.append(subtitle);
        }
        if (unit != null) {
            text.append(", Unit: ");
            text.append(unit);
        }
        text.append(", Exam Date: ");
        text.append(examDate);
        text.append(" ");
        text.append(examTime);

        text.append(System.lineSeparator());
        return text.toString();
    }

    /**
     * return an Object[] containing class values suitable for use in the view model.
     * The objects you return depend on the fields you have in the Exams page of the exam table.
     *
     * @return an Object[] containing class values suitable for use in the view model
     */
    @Override
    public Object[] toTableRow() {
        return new Object[]{examType == ExamType.INTERNAL ? "✔" : "",
                abbrevShortTitle(), examDate, examTime};
    }

    /**
     * return an Object[] containing class values suitable for use in the view model.
     * The objects you return depend on the fields you have in the Exams page of the tabbed pane.
     *
     * @return an array of objects suitable for insertion into a JTable
     */
    @Override
    public Object[] toLongTableRow() {
        return new Object[]{getSubject().getTitle(),
                examType.toString(), paper == null ? "" : String.valueOf(paper),
                subtitle,
                String.valueOf(unit),
                getDate().toString(),
                getTime().toString()};
    }

    /**
     * Return a unique string identifying us
     *
     * @return a unique string identifying us
     */
    @Override
    public String getId() {
        return getShortTitle();
    }

    /**
     * Gets the subject of the exam.
     *
     * @return subject of the exam
     */
    public Subject getSubject() {
        return this.subject;
    }

    /**
     * Gets the full title of the exam.
     * Provides the exam type,
     * and then on a new line, the exam subject, and any paper identifier (if one exists),
     * and then on a new line, any subtitle (only if present).
     *
     * @return the full text title of the exam, for example
     * "Year 12 Internal Assessment\nGeneral Mathematics Paper 1\nTechnology Free"
     */
    public String getTitle() {
        StringBuilder title = new StringBuilder();
        title.append("Year 12 ");
        if (examType == ExamType.EXTERNAL) {
            title.append("External ");
        } else {
            title.append("Internal ");
        }
        title.append("Assessment");
        title.append(System.lineSeparator());

        title.append(subject.getTitle());

        if (paper != '\0') {
            title.append(" Paper ");
            title.append(paper);
            title.append(System.lineSeparator());
        }

        if (!subtitle.isEmpty()) {
            title.append(subtitle);
        }
        return title.toString();
    }

    /**
     * Gets the short title of the exam with no subtitle.
     * Provides type, subject, and any paper identifier (if more than one), all on one line.
     *
     * @return text title of the exam like "Year 12 Internal Assessment General Mathematics Paper 1"
     */
    public String getShortTitle() {
        StringBuilder title = new StringBuilder();
        title.append("Year 12 ");
        if (examType == ExamType.EXTERNAL) {
            title.append("External ");
        } else {
            title.append("Internal ");
        }
        title.append("Assessment ");
        title.append(subject.getTitle());
        if (paper != '\0') {
            title.append(" Paper ");
            title.append(paper);
        }
        return title.toString();
    }

    /**
     * Gets the date of this exam.
     *
     * @return the date of this exam.
     */
    public LocalDate getDate() {
        return examDate;
    }

    /**
     * Gets the start time of this exam's window.
     *
     * @return the start time of this exam's window.
     */
    public LocalTime getTime() {
        return examTime;
    }

    /**
     * Returns an exam title of just the title plus paper if applicable
     *
     * @return the title plus paper if applicable, like "Mathematical Methods Paper 1"
     */
    public String abbrevShortTitle() {
        String title = subject.getTitle();
        if (paper != '\0') {
            title += " Paper " + paper;
        }
        return title;
    }

    /**
     * Returns a brief string representation of the exam.
     *
     * @return a brief string representation of the exam.
     */
    @Override
    public String toString() {
        return this.getShortTitle();
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
        Exam other = (Exam) o;
        return other.subject.getFullDetail().equals(this.subject.getFullDetail())
                && other.examType == this.examType
                && other.paper == this.paper
                && other.subtitle.equals(this.subtitle)
                && other.unit == this.unit
                && Objects.equals(other.examDate, this.examDate)
                && Objects.equals(other.examTime, this.examTime);
    }

    /**
     * return the hash value of this object
     *
     * @return the hash value of this object
     */
    @Override
    public int hashCode() {
        return this.subject.getFullDetail().hashCode()
                + 2 * this.examType.hashCode()
                + 3 * this.paper.hashCode()
                + 5 * this.subtitle.hashCode()
                + 7 * this.unit.hashCode()
                + 11 * this.examDate.hashCode()
                + 13 * this.examTime.hashCode();
    }
}
