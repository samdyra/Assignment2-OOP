package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Represents an individual desk in an exam venue.
 */
public class Desk {
    private int deskNumber;
    private String familyName;
    private String givenAndInit;
    private long lui;
    private String examName;

    /**
     * Constructs a Desk. Assigns the integer deskNumber as the numerical identifier
     * and assigns empty Strings to the names.
     *
     * @param deskNumber the non-zero positive integer desk number.
     */
    public Desk(int deskNumber) {
        this.deskNumber = deskNumber;
        this.familyName = "";
        this.givenAndInit = "";
        this.lui = 0;
        this.examName = "";
    }
    /**
     * Gets the number of this desk.
     *
     * @return The number of this desk.
     */
    public int deskNumber() {
        return this.deskNumber;
    }

    /**
     * Return the student allocated to this desk
     *
     * @return the Student, or an empty string if not allocated
     */
    public String deskStudent() {
        return familyName.isEmpty() ? "" : familyName + ", " + givenAndInit;
    }

    /**
     * Gets the LUI of the student assigned to this desk.
     *
     * @return The LUI of the student assigned to this desk.
     */
    public long deskLui() {
        return this.lui;
    }

    /**
     * Gets the family name of the student assigned to this desk.
     *
     * @return The family name of the student assigned to this desk.
     */
    public String deskFamilyName() {
        return this.familyName;
    }

    /**
     * Gets the first given name and initial of the student assigned to this desk.
     * Gets the first given name, a space, the initial of first middle name, if any,
     * with a full stop after the initial (if present) of the student assigned to
     * this desk.
     *
     * @return The first given name and initial of the student assigned to this desk.
     */
    public String deskGivenAndInit() {
        return this.givenAndInit;
    }

    /**
     * Allocate a student to this desk
     *
     * @param student student to assign
     */
    public void setStudent(Student student) {
        this.lui = student.getLui();
        this.familyName = student.familyName();
        // first name and middle initial
        String[] names = student.givenNames().split(" ");
        if (names.length > 1) {
            this.givenAndInit = names[0] + " " + names[1].charAt(0) + ".";
        } else {
            this.givenAndInit = names[0];
        }
    }

    /**
     * Manually change the allocated student's displayed name
     *
     * @param givenAndInit new name
     */
    public void setGivenAndInit(String givenAndInit) {
        this.givenAndInit = givenAndInit;
    }

    /**
     * Allocate an exam for this desk
     *
     * @param exam exam to allocate
     */
    public void setExam(Exam exam) {
        this.examName = exam.abbrevShortTitle();
    }

    /**
     * Return the exam being taken at this desk
     *
     * @return the exam at this desk
     */
    public String deskExam() {
        return this.examName;
    }

    /**
     * Returns a string representation of this desk.
     * (Returns the desk number and any assigned student.)
     *
     * @return The string representation of this desk.
     */
    @Override
    public String toString() {
        if (familyName.isEmpty()) {
            return "Desk " + deskNumber + ":";
        }
        return "Desk " + deskNumber + ": " + familyName + ", " + givenAndInit;
    }

    /**
     * Write a string representation of this desk to disk.
     *
     * @param bw stream to write to
     * @throws IOException on any IO related issues
     */
    public void streamOut(BufferedWriter bw) throws IOException {
    }

    /**
     * Read itself from an input stream
     *
     * @param br       stream to read from
     * @param examName Use this as the exam name value (saves duplicating it
     *                 for each desk)
     * @throws IOException on any IO related issues
     */
    public void streamIn(BufferedReader br, String examName) throws IOException {
    }
}