package examblock.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A collection object for holding and managing {@link Session}s.
 */
public class SessionList extends ListManager<Session> {

    /**
     * constructor
     *
     * @param registry registry
     */
    public SessionList(Registry registry) {
        super(Session::new, registry, Session.class);
    }

    /**
     * Find the sessionNumber of a session at a particular time in a given
     * Venue. Return zero if no session exists at that time.
     *
     * @param venue the venue object for the session we are looking for.
     * @param day   the session date.
     * @param start the start time of the exam session.
     * @return the sessionNumber of a session at a particular time in a
     *         given Venue, else zero.
     */
    public int getSessionNumber(Venue venue, LocalDate day, LocalTime start) {
        for (Session session : all()) {
            if (session.getVenue().equals(venue)
                    && session.getDate().equals(day)
                    && session.getTime().equals(start)) {
                return session.getSessionNumber();
            }
        }
        return 0;
    }

    /**
     * Get the Session with a matching Venue and sessionNumber.
     *
     * @param venue         the Venue for which the session is to be found.
     * @param sessionNumber the sessionNumber of the Session you are looking for.
     * @return The first Session with a matching Venue and sessionNumber,
     *         if it exists.
     * @throws IllegalStateException throw an IllegalStateException if it can't
     *                               find any such session as that indicates there
     *                               is a potential misalignment of the executing
     *                               state and the complete list of all items.
     */
    public Session getSession(Venue venue, int sessionNumber)
            throws IllegalStateException {
        for (Session session : all()) {
            if (session.getVenue().equals(venue)
                    && session.getSessionNumber() == sessionNumber) {
                return session;
            }
        }
        throw new IllegalStateException("No such session!");
    }

    /**
     * Get the Session with a matching Venue and Exam scheduled.
     *
     * @param venue the Venue for which the session is to be found.
     * @param exam  (one of) the exam(s) that has been allocated to this
     *              session in this venue.
     * @return The first Session with a matching Venue and Exam, if it exists.
     * @throws IllegalStateException throw an IllegalStateException if it can't
     *                               find any such session as that indicates there
     *                               is a potential misalignment of the executing
     *                               state and the complete list of all items.
     */
    public Session getSession(Venue venue, Exam exam)
            throws IllegalStateException {
        for (Session session : all()) {
            if (session.getVenue().equals(venue)
                    && session.getExams().contains(exam)) {
                return session;
            }
        }
        throw new IllegalStateException("No such session!");
    }

    /**
     * Find or create this session and work out how many students in total.
     * If there is no existing session, prints: "There is currently no exam session
     * in that venue at that time."
     * and also prints: "Creating a session..." and creates a suitable session.
     * When creating the new session, we first determine the next unique session number
     * for this venue (suggest you may want to use a private helper method to do this).
     * If this is the very first session created for this venue, uses session number 1.
     * Session numbers do not have to be sequential, only unique. i.e. the first session may be
     * in the middle of the week, the next at the end of the week and the next at the beginning -
     * but the session numbers must still be unique, e.g. 3,6,1,7,5,2.
     *
     * @param venue          the exam venue for the session.
     * @param exam           the exam to be allocated to this session in this venue.
     * @param numberStudents the number of students to be allocated to this session.
     * @return The total number of students that will be in the session if
     *         numberStudents is added.
     */
    public int getSessionNewTotal(Venue venue, Exam exam, int numberStudents) {
        int sessionNumber = getSessionNumber(venue, exam.getDate(), exam.getTime());
        Session session;

        if (sessionNumber == 0) {
            // no existing session
            System.out.println("There is currently no exam session in that venue at that time.");
            System.out.println("Creating a session...");

            // find next unique session number for this venue
            int nextNumber = getNextSessionNumber(venue);

            session = new Session(venue, nextNumber, exam.getDate(),
                    exam.getTime(), getRegistry());

            add(session);
        } else {
            session = getSession(venue, sessionNumber);
        }

        // count existing students in the session
        int existingStudents = session.countStudents();

        if (existingStudents > 0) {
            System.out.println("There are already " + existingStudents
                    + " students who will be taking an exam in that venue; along with the "
                    + numberStudents + " students for this exam.");
        }

        int total = existingStudents + numberStudents;
        System.out.println("That's a total of " + total + " students.");
        return total;
    }

    /**
     * Find this session and work out how many students in total. Unlike
     * getSessionNewTotal, this method does not create a new session if one
     * does not already exist, it just looks for an existing session. If
     * there is no existing session, returns zero.
     *
     * @param venue where the session might be
     * @param exam  the exam that might be scheduled in the session
     * @return the total number of students in the session, or zero if
     *         there is no session.
     */
    public int getExistingSessionTotal(Venue venue, Exam exam) {
        int sessionNumber = getSessionNumber(venue, exam.getDate(), exam.getTime());
        if (sessionNumber == 0) {
            return 0;
        }
        Session session = getSession(venue, sessionNumber);
        return session.countStudents();
    }

    /**
     * Allocates an exam to an existing session (Venue and time). Prints
     * the title of the subject exam added to the identifier of the venue.
     *
     * @param venue the exam venue for the new session.
     * @param exam  the exam to be allocated to this venue.
     */
    public void scheduleExam(Venue venue, Exam exam) {
        int sessionNumber = getSessionNumber(venue, exam.getDate(), exam.getTime());
        Session session = getSession(venue, sessionNumber);
        session.scheduleExam(exam);
        System.out.println(exam.getSubject().getTitle()
                + " exam added to " + venue.venueId() + ".");
    }

    /**
     * Deallocates an exam from an existing session (Venue and time). Prints
     * the title of the subject exam removed from the identifier of the venue.
     *
     * @param venue the exam venue for the new session.
     * @param exam  the exam to be allocated to this venue.
     */
    public void removeExam(Venue venue, Exam exam) {
        int sessionNumber = getSessionNumber(venue, exam.getDate(), exam.getTime());
        Session session = getSession(venue, sessionNumber);
        session.removeExam(exam);
        System.out.println(exam.getSubject().getTitle()
                + " exam removed from " + venue.venueId() + ".");
    }

    /**
     * Creates a new list holding references to those Sessions for a given
     * Venue in this SessionList.
     *
     * @param venue the exam venue for the list of items.
     * @return A new list holding references to all the items in this sessionList.
     */
    public List<Session> forVenue(Venue venue) {
        List<Session> venueSessionList = new ArrayList<>();
        for (Session session : all()) {
            if (session.getVenue().equals(venue)) {
                venueSessionList.add(session);
            }
        }
        return venueSessionList;
    }

    /**
     * Return a string containing enough information to rebuild an item
     *
     * @return the string representation of the object
     */
    public String getFullDetail() {
        StringBuilder sessionStrings = new StringBuilder();
        int counter = 1;
        for (Session session : all()) {
            sessionStrings.append(counter);
            sessionStrings.append(". ");
            sessionStrings.append(session.getFullDetail());
            counter += 1;
        }
        return sessionStrings + "\n";
    }

    /**
     * Find the next unique session number for a venue.
     *
     * @param venue the venue to find the next session number for
     * @return the next unique session number
     */
    private int getNextSessionNumber(Venue venue) {
        int maxNumber = 0;
        for (Session session : all()) {
            if (session.getVenue().equals(venue)
                    && session.getSessionNumber() > maxNumber) {
                maxNumber = session.getSessionNumber();
            }
        }
        return maxNumber + 1;
    }
}