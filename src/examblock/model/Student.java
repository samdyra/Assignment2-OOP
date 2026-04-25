package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;

/**
 * An object describing a single Year 12 Student.
 */
public class Student implements StreamManager, ManageableListItem {
    private Long lui;
    private String givenNames;
    private String familyName;
    private LocalDate dob;
    private Boolean aara;
    private String house;
    private SubjectList subjects;
    private ExamList exams;

    /**
     * Constructs a new Student object with no AARA requirements by default.
     *
     * @param lui        the student's 10-digit Learner Unique Identifier (LUI).
     *                   The LUI must be unique to each student throughout the
     *                   entire cohort.
     * @param givenNames the initial given names for the student, which must be
     *                   a single string with one or more names. Names must contain
     *                   only alphabetic characters, hyphens, or apostrophes; and
     *                   multiple names must be separated by one or more spaces.
     *                   Any leading and trailing spaces are ignored.
     * @param familyName the initial family name for the student, which must be
     *                   a single string with one or more names. Names must contain
     *                   only alphabetic characters, hyphens, or apostrophes; and
     *                   multiple names must be separated by one or more spaces.
     *                   Any leading and trailing spaces are ignored.
     * @param day        the integer day of the date of birth for the student,
     *                   which must be a valid day for the month and year provided.
     * @param month      the integer month of the date of birth for the student,
     *                   which must be between 1 - 12 inclusive.
     * @param year       the 4-digit integer year of the date of birth for the
     *                   student, which must be between 1965 and 2015.
     * @param house      the initial house colour for the student, which must be
     *                   one of: Blue, Green, Red, White, or Yellow.
     * @param registry   the global registry, needed to resolve textual object names
     */
    public Student(Long lui, String givenNames, String familyName,
                   int day, int month, int year, String house,
                   Registry registry) {
        this(lui, givenNames, familyName, day, month, year, house, false, registry);
    }

    /**
     * Constructs a new Student object with AARA requirements. Overloaded
     * constructor for a new Student requiring access arrangements and
     * reasonable adjustments.
     *
     * @param lui        the student's 10-digit Learner Unique Identifier (LUI).
     *                   The LUI must be unique to each student throughout the
     *                   entire cohort.
     * @param givenNames the initial given names for the student, which must be
     *                   a single string with one or more names. Names must contain
     *                   only alphabetic characters, hyphens, or apostrophes; and
     *                   multiple names must be separated by one or more spaces.
     *                   Any leading and trailing spaces are ignored.
     * @param familyName the initial family name for the student, which must be
     *                   a single string with one or more names. Names must contain
     *                   only alphabetic characters, hyphens, or apostrophes; and
     *                   multiple names must be separated by one or more spaces.
     *                   Any leading and trailing spaces are ignored.
     * @param day        the integer day of the date of birth for the student,
     *                   which must be a valid day for the month and year provided.
     * @param month      the integer month of the date of birth for the student,
     *                   which must be between 1 - 12 inclusive.
     * @param year       the 4-digit integer year of the date of birth for the
     *                   student, which must be between 1965 and 2015.
     * @param house      the initial house colour for the student, which must be
     *                   one of: Blue, Green, Red, White, or Yellow.
     * @param aara       the initial aara setting for the student, true or false:
     *                   true requires AARA adjustments, false does not.
     * @param registry   the global registry, needed to resolve textual object names
     */
    public Student(Long lui, String givenNames, String familyName,
                   int day, int month, int year, String house,
                   Boolean aara, Registry registry) {
        this.lui = lui;
        this.givenNames = sanitiseName(givenNames);
        this.familyName = sanitiseName(familyName);
        this.dob = LocalDate.of(year, month, day);
        this.house = house;
        this.aara = aara;

        this.subjects = new SubjectList(registry);
        this.exams = new ExamList(registry);

        registry.add(this, Student.class);
    }

    /**
     * Constructs an Exam by reading a description from a text stream
     *
     * @param br       BufferedReader opened and ready to read from
     * @param registry the global object registry, needed to resolve textual
     *                 Subject names
     * @param nthItem  the index number of this serialized object
     * @throws IOException      on any read failure
     * @throws RuntimeException on any runtime exception
     */
    public Student(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        streamIn(br, registry, nthItem);
        registry.add(this, Student.class);
    }

    /**
     * Return a string from the input string complying with the following rules
     * a single string with one or more names.
     * names must contain only alphabetic characters, hyphens, or apostrophes
     * multiple names must be separated by one or more spaces.
     * any leading and trailing spaces are ignored
     *
     * @param text the string to sanitise
     * @return the sanitised string
     */
    public String sanitiseName(String text) {
        // remove chars that are not alphabetic, hyphens, apostrophes, or spaces
        text = text.replaceAll("[^a-zA-Z\\-' ]", "");
        // Handle extra spaces
        text = text.trim().replaceAll("\\s+", " ");
        return text;
    }

    /**
     * Used to write data to the disk.
     *
     * The format of the text written to the stream must be matched exactly by
     * streamIn, so it is very important to format the output as described.
     *
     * 1. LIAM ALEXANDER SMITH
     * LUI: 9999365663, Family Name: Smith, Given Name(s): Liam Alexander,
     * Date of Birth: 2007-12-08, House: Blue, AARA: false
     * Subjects: Essential English, Essential Mathematics, Ancient History,
     * Industrial Technology Skills, Trade Course, Another Trade Course
     *
     * @param bw      writer, already opened. Your data should be written at
     *                the current file position
     * @param nthItem a number representing this item's position in the stream.
     *                Used for sanity checks
     * @throws IOException on any stream related issues
     */
    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        String fullName = givenNames + " " + familyName;
        bw.write(nthItem + ". " + fullName.toUpperCase() + System.lineSeparator());
        bw.write("LUI: " + lui
                + ", Family Name: " + familyName
                + ", Given Name(s): " + givenNames
                + ", Date of Birth: " + dob
                + ", House: " + house
                + ", AARA: " + aara
                + System.lineSeparator());

        StringBuilder sb = new StringBuilder("Subjects: ");
        boolean firstSubject = true;
        for (Subject s : subjects.all()) {
            // only add leading comma in the second and the next subjects (if any)
            if (!firstSubject) {
                sb.append(", ");
            }
            sb.append(s.getTitle());
            firstSubject = false;
        }
        bw.write(sb + System.lineSeparator());
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
        this.subjects = new SubjectList(registry);
        this.exams = new ExamList(registry);

        // firstLine example: "1. LIAM ALEXANDER SMITH"
        String header = Utilities.getLine(br);
        if (header == null) {
            throw new RuntimeException("EOF reading Student #" + nthItem);
        }

        // split by period and space
        String[] partsOfHeader = header.split("\\. ");
        int index = Utilities.toInt(partsOfHeader[0], "Number format exception parsing Student "
                + nthItem + " header");
        if (index != nthItem) {
            throw new RuntimeException("Student index out of sync!");
        }

        // second line example: LUI: 9999365663, Family Name: Smith, Given Name(s): Liam Alexander,
        // Date of Birth: 2007-12-08, House: Blue, AARA: false
        String studentDetails = Utilities.getLine(br);
        if (studentDetails == null) {
            throw new RuntimeException("EOF reading Student #" + nthItem);
        }

        // get each parts of the student details
        String[] partsOfDetails = studentDetails.split(",");
        for (String detail : partsOfDetails) {
            String[] pairOfDetail = Utilities.keyValuePair(detail.trim());
            if (pairOfDetail == null) {
                continue;
            }
            switch (pairOfDetail[0]) {
                case "LUI":
                    this.lui = Utilities.toLong(pairOfDetail[1],
                            "Number format exception parsing Student " + nthItem + " LUI");
                    break;
                case "Family Name":
                    this.familyName = sanitiseName(pairOfDetail[1]);
                    break;
                case "Given Name(s)":
                    this.givenNames = sanitiseName(pairOfDetail[1]);
                    break;
                case "Date of Birth":
                    this.dob = Utilities.toLocalDate(pairOfDetail[1],
                            "Date format error parsing Student " + nthItem + " DOB");
                    break;
                case "House":
                    this.house = pairOfDetail[1];
                    break;
                case "AARA":
                    this.aara = Utilities.toBoolean(pairOfDetail[1],
                            "Boolean format error parsing Student " + nthItem + " AARA");
                    break;
            }
        }

        // line 3 example: "Subjects: Essential English, Essential Mathematics, ..."
        String subjectLine = Utilities.getLine(br);
        if (subjectLine == null) {
            throw new RuntimeException("EOF reading Student #" + nthItem);
        }

        String[] subjectParts = Utilities.keyValuePair(subjectLine);
        if (subjectParts != null) {
            String[] subjectNames = subjectParts[1].split(",");
            for (String name : subjectNames) {
                // get the subject from registry
                Subject subject = registry.get(name.trim(), Subject.class);
                subjects.add(subject);
            }
        }
    }

    /**
     * Creates and returns a string representation of this student's
     * detailed state.
     *
     * @return the string representation of this student's detailed state.
     */
    @Override
    public String getFullDetail() {
        StringBuilder sb = new StringBuilder();
        sb.append("LUI: ").append(lui)
                .append(", Family Name: ").append(familyName)
                .append(", Given Name(s): ").append(givenNames)
                .append(", Date of Birth: ").append(dob)
                .append(", House: ").append(house)
                .append(", AARA: ").append(aara)
                .append(System.lineSeparator());

        sb.append("Subjects: ");
        boolean firstSubject = true;
        for (Subject s : subjects.all()) {
            // only add leading comma in the second and the next subjects (if any)
            if (!firstSubject) {
                sb.append(", ");
            }
            sb.append(s.getTitle());
            firstSubject = false;
        }
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    /**
     * return an Object[] containing class values suitable for use in the
     * view model
     *
     * @return an Object[] containing class values suitable for use in the
     *         view model
     */
    @Override
    public Object[] toTableRow() {
        return new Object[]{familyName, givenNames, lui, house, aara};
    }

    /**
     * Return a unique string identifying us
     *
     * @return a unique string identifying us
     */
    @Override
    public String getId() {
        return String.valueOf(lui);
    }

    /**
     * Change the LUI of the student.
     *
     * @param lui the student's 10-digit Learner Unique Identifier (LUI).
     *            The LUI must be unique to each student throughout the
     *            entire cohort.
     */
    public void changeLui(Long lui) {
        this.lui = lui;
    }

    /**
     * Sets the given names of the student.
     *
     * @param givenNames the new given names for the student, which must be
     *                   a single string with one or more names. Names must
     *                   contain only alphabetic characters, hyphens, or
     *                   apostrophes; and multiple names must be separated
     *                   by one or more spaces. Any leading and trailing
     *                   spaces are ignored.
     */
    public void setGiven(String givenNames) {
        this.givenNames = sanitiseName(givenNames);
    }

    /**
     * Sets the family name of the student.
     *
     * @param familyName the new family name for the student, which must be
     *                   a single string with one or more names. Names must
     *                   contain only alphabetic characters, hyphens, or
     *                   apostrophes; and multiple names must be separated
     *                   by one or more spaces. Any leading and trailing
     *                   spaces are ignored.
     */
    public void setFamily(String familyName) {
        this.familyName = sanitiseName(familyName);
    }

    /**
     * Gets the LUI of this student.
     *
     * @return the 10-digit LUI of this student as a Long.
     */
    public Long getLui() {
        return this.lui;
    }

    /**
     * Gets the given name(s) of this student.
     *
     * @return the given name(s) of this student.
     */
    public String givenNames() {
        return this.givenNames;
    }

    /**
     * Gets the first given name of this student.
     *
     * @return the first given name of this student.
     */
    public String firstName() {
        return this.givenNames.split(" ")[0];
    }

    /**
     * Gets the family name of this student.
     *
     * @return the family name of this student.
     */
    public String familyName() {
        return this.familyName;
    }

    /**
     * Gets the first given name and family name of this student.
     *
     * @return the first given name and family name of this student.
     */
    public String shortName() {
        return firstName() + " " + familyName;
    }

    /**
     * Gets all the given name(s) and family name of this student.
     *
     * @return all the given name(s) and family name of this student.
     */
    public String fullName() {
        return givenNames + " " + familyName;
    }

    /**
     * Gets the date of birth of this student.
     *
     * @return the date of birth of this student.
     */
    public LocalDate getDob() {
        return this.dob;
    }

    /**
     * Gets the house colour of this student.
     *
     * @return the house colour of this student.
     */
    public String getHouse() {
        return this.house;
    }

    /**
     * Returns true if this student is an AARA student. (i.e. the student
     * requires Access Arrangements and Reasonable Adjustments).
     *
     * @return true if this student is an AARA student, false otherwise.
     */
    public Boolean isAara() {
        return this.aara;
    }

    /**
     * Gets the SubjectList for this student.
     *
     * @return the reference to this student's SubjectList.
     */
    public SubjectList getSubjects() {
        return this.subjects;
    }

    /**
     * Gets the ExamList for this student.
     *
     * @return the reference to this student's ExamList.
     */
    public ExamList getExams() {
        return this.exams;
    }

    /**
     * Adds a subject to this student.
     *
     * @param subject the Subject being added to this student.
     */
    public void addSubject(Subject subject) {
        subjects.add(subject);
    }

    /**
     * Adds a unit to this student.
     *
     * @param unit the Unit being added to this student.
     */
    public void addUnit(Unit unit) {
        // student has no unit list, add the unit's parent subject instead
        addSubject(unit.getSubject());
    }

    /**
     * Adds an exam to this student.
     *
     * @param exam the Exam being added to this student.
     */
    public void addExam(Exam exam) {
        exams.add(exam);
    }

    /**
     * Removes a subject from this student and unadjusts the student exams
     * for that subject.
     *
     * @param subject the Subject being removed from this student.
     */
    public void removeSubject(Subject subject) {
        subjects.remove(subject);
        // remove exams associated with this subject
        for (Exam exam : exams.all()) {
            if (exam.getSubject().equals(subject)) {
                exams.remove(exam);
            }
        }
    }

    /**
     * Creates and returns a string representation of this student's
     * basic state.
     *
     * @return the string representation of this student's basic state.
     */
    @Override
    public String toString() {
        return fullName().toUpperCase() + System.lineSeparator();
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
        Student other = (Student) o;
        return other.lui.equals(this.lui)
                && other.givenNames.equals(this.givenNames)
                && other.familyName.equals(this.familyName)
                && other.dob.equals(this.dob)
                && other.house.equals(this.house)
                && other.aara.equals(this.aara);
    }

    /**
     * return the hash value of this object
     *
     * @return the hash value of this object
     */
    @Override
    public int hashCode() {
        return this.lui.hashCode()
                + 2 * this.givenNames.hashCode()
                + 3 * this.familyName.hashCode()
                + 5 * this.dob.hashCode()
                + 7 * this.house.hashCode()
                + 11 * this.aara.hashCode();
    }
}