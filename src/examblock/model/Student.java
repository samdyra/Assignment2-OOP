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
    private int day;
    private int month;
    private int year;
    private boolean aara;
    private String house;

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
        this.lui = lui;
        this.givenNames = givenNames;
        this.familyName = familyName;
        this.day = day;
        this.month = month;
        this.year = year;
        this.house = house;

        registry.add(this, Student.class);
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
    }

    /**
     * Constructs an Exam by reading a description from a text stream
     *
     * @param br       BufferedReader opened and ready to read from
     * @param registry the global object registry, needed to resolve textual
     *                 Subject names
     * @param nthItem  the index number of this serialized object
     * @throws IOException      on any read failure
     * @throws RuntimeException
     */
    public Student(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
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
        return null;
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
     * Creates and returns a string representation of this student's
     * detailed state.
     *
     * @return the string representation of this student's detailed state.
     */
    @Override
    public String getFullDetail() {
        return null;
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
        return null;
    }

    /**
     * Return a unique string identifying us
     *
     * @return a unique string identifying us
     */
    @Override
    public String getId() {
        return null;
    }

    /**
     * Change the LUI of the student.
     *
     * @param lui the student's 10-digit Learner Unique Identifier (LUI).
     *            The LUI must be unique to each student throughout the
     *            entire cohort.
     */
    public void changeLui(Long lui) {
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
    }

    /**
     * Gets the LUI of this student.
     *
     * @return the 10-digit LUI of this student as a Long.
     */
    public Long getLui() {
        return null;
    }

    /**
     * Gets the given name(s) of this student.
     *
     * @return the given name(s) of this student.
     */
    public String givenNames() {
        return null;
    }

    /**
     * Gets the first given name of this student.
     *
     * @return the first given name of this student.
     */
    public String firstName() {
        return null;
    }

    /**
     * Gets the family name of this student.
     *
     * @return the family name of this student.
     */
    public String familyName() {
        return null;
    }

    /**
     * Gets the first given name and family name of this student.
     *
     * @return the first given name and family name of this student.
     */
    public String shortName() {
        return null;
    }

    /**
     * Gets all the given name(s) and family name of this student.
     *
     * @return all the given name(s) and family name of this student.
     */
    public String fullName() {
        return null;
    }

    /**
     * Gets the date of birth of this student.
     *
     * @return the date of birth of this student.
     */
    public LocalDate getDob() {
        return null;
    }

    /**
     * Gets the house colour of this student.
     *
     * @return the house colour of this student.
     */
    public String getHouse() {
        return null;
    }

    /**
     * Returns true if this student is an AARA student. (i.e. the student
     * requires Access Arrangements and Reasonable Adjustments).
     *
     * @return true if this student is an AARA student, false otherwise.
     */
    public Boolean isAara() {
        return null;
    }

    /**
     * Gets the SubjectList for this student.
     *
     * @return the reference to this student's SubjectList.
     */
    public SubjectList getSubjects() {
        return null;
    }

    /**
     * Gets the ExamList for this student.
     *
     * @return the reference to this student's ExamList.
     */
    public ExamList getExams() {
        return null;
    }

    /**
     * Adds a subject to this student.
     *
     * @param subject the Subject being added to this student.
     */
    public void addSubject(Subject subject) {
    }

    /**
     * Adds a unit to this student.
     *
     * @param unit the Unit being added to this student.
     */
    public void addUnit(Unit unit) {
    }

    /**
     * Adds an exam to this student.
     *
     * @param exam the Exam being added to this student.
     */
    public void addExam(Exam exam) {
    }

    /**
     * Removes a subject from this student and unadjusts the student exams
     * for that subject.
     *
     * @param subject the Subject being removed from this student.
     */
    public void removeSubject(Subject subject) {
    }

    /**
     * Creates and returns a string representation of this student's
     * basic state.
     *
     * @return the string representation of this student's basic state.
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
        return false;
    }

    /**
     * return the hash value of this object
     *
     * @return the hash value of this object
     */
    @Override
    public int hashCode() {
        return 0;
    }
}