package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Represents an individual desk in an exam venue.
 */
public class Desk {
    private int deskNumber;
    private Student assignedStudent;

    /**
     * Constructs a Desk. Assigns the integer deskNumber as the numerical identifier
     * and assigns empty Strings to the names.
     *
     * @param deskNumber the non-zero positive integer desk number.
     */
    public Desk(int deskNumber) throws Exception {
        if (deskNumber <= 0) {
            throw new Exception("ID must not be positive number");
        }

        this.deskNumber = deskNumber;
        this.assignedStudent = "";
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
        return this.assignedStudent;
    }

    /**
     * Gets the LUI of the student assigned to this desk.
     *
     * @return The LUI of the student assigned to this desk.
     */
    public long deskLui() {
        return 0;
    }

    /**
     * Gets the family name of the student assigned to this desk.
     *
     * @return The family name of the student assigned to this desk.
     */
    public String deskFamilyName() {
        return null;
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
        return null;
    }

    /**
     * Allocate a student to this desk
     *
     * @param student student to assign
     */
    public void setStudent(Student student) {
    }

    /**
     * Manually change the allocated student's displayed name
     *
     * @param givenAndInit new name
     */
    public void setGivenAndInit(String givenAndInit) {
    }

    /**
     * Allocate an exam for this desk
     *
     * @param exam exam to allocate
     */
    public void setExam(Exam exam) {
    }

    /**
     * Return the exam being taken at this desk
     *
     * @return the exam at this desk
     */
    public String deskExam() {
        return null;
    }

    /**
     * Returns a string representation of this desk.
     * (Returns the desk number and any assigned student.)
     *
     * @return The string representation of this desk.
     */
    @Override
    public String toString() {
        return null;
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