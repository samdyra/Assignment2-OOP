package examblock.model;

import java.util.List;

/**
 * A collection object for holding and managing {@link Venue}s.
 */
public class VenueList extends ListManager<Venue> {

    /**
     * constructor
     *
     * @param registry registry
     */
    public VenueList(Registry registry) {
        super(Venue::new, registry, Venue.class);
    }

    /**
     * Adds a {@link Venue} to this list of {@link Venue}s.
     *
     * @param venue the venue object being added to this list.
     */
    public void addVenue(Venue venue) {
        add(venue);
    }

    /**
     * Removes a given {@link Venue} from the {@code VenueList}.
     *
     * @param venue the venue to remove from this list.
     */
    public void removeVenue(Venue venue) {
        remove(venue);
    }

    /**
     * Get the first {@link Venue} with a matching {@code id}.
     *
     * @param id the identifier of the {@link Venue} to be found.
     * @return first {@link Venue} with a matching {@code id}, if it exists.
     * @throws IllegalStateException throw an IllegalStateException if it can't
     *                               find a matching venue as that indicates there
     *                               is a misalignment of the executing state and
     *                               the complete list of possible items.
     */
    public Venue getVenue(String id) throws IllegalStateException {
        for (Venue venue : all()) {
            if (venue.venueId().equals(id)) {
                return venue;
            }
        }
        throw new IllegalStateException("No such venue!");
    }

    /**
     * Allocates {@link Student}s to {@link Desk}s for every {@link Session}
     * in every {@link Venue}.
     *
     * @param sessions the current set of exam sessions allocated to items.
     * @param exams    the current set of Year 12 Exams.
     * @param cohort   all the Year 12 students.
     */
    public void allocateStudents(SessionList sessions, ExamList exams,
                                 StudentList cohort) {
        for (Venue venue : all()) {
            List<Session> venueSessions = sessions.forVenue(venue);
            for (Session session : venueSessions) {
                session.allocateStudents(exams, cohort);
            }
        }
    }

    /**
     * Print the allocations of {@link Student}s to {@link Desk}s for every
     * {@link Session} in every {@link Venue}.
     *
     * @param sessions the current set of exam sessions allocated to items.
     */
    public void printAllocations(SessionList sessions) {
        StringBuilder sb = new StringBuilder();
        writeAllocations(sb, sessions);
        System.out.println(sb);
    }

    /**
     * Write the allocations of {@link Student}s to {@link Desk}s for every
     * {@link Session} in every {@link Venue} to a String.
     *
     * @param sb       the StringBuilder to write to
     * @param sessions the current set of exam sessions allocated to items.
     */
    public void writeAllocations(StringBuilder sb, SessionList sessions) {
        for (Venue venue : all()) {
            List<Session> venueSessions = sessions.forVenue(venue);
            for (Session session : venueSessions) {
                sb.append("Venue ").append(venue.venueId())
                        .append(" Session ").append(session.getSessionNumber())
                        .append(System.lineSeparator());
                session.printDesks(sb);
                sb.append(System.lineSeparator());
            }
        }
    }

    /**
     * Returns detailed string representations of the contents of this
     * venue list.
     *
     * @return detailed string representations of the contents of this
     *         venue list.
     */
    public String getFullDetail() {
        StringBuilder venueStrings = new StringBuilder();
        int counter = 1;
        for (Venue venue : all()) {
            venueStrings.append(counter);
            venueStrings.append(". ");
            venueStrings.append(venue.getFullDetail());
            counter += 1;
        }
        return venueStrings + "\n";
    }

    /**
     * Returns a brief string representation of the contents of this
     * venue list.
     *
     * @return a brief string representation of the contents of this
     *         venue list.
     */
    @Override
    public String toString() {
        StringBuilder venueStrings = new StringBuilder();
        int counter = 1;
        for (Venue venue : all()) {
            venueStrings.append(counter);
            venueStrings.append(". ");
            venueStrings.append(venue.toString());
            venueStrings.append("\n");
            counter += 1;
        }
        return venueStrings.toString();
    }
}