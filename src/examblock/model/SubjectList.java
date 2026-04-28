package examblock.model;

/**
 * A collection object for holding and managing {@link Subject}s.
 */
public class SubjectList extends ListManager<Subject> {

    /**
     * constructor
     *
     * @param registry registry
     */
    public SubjectList(Registry registry) {
        super(Subject::new, registry, Subject.class);
    }

    /**
     * Get the first {@link Subject} with a matching {@code title}.
     *
     * @param title the {@code title} of the {@link Subject} to be found.
     * @return The first {@link Subject} with a matching {@code title}, if it exists.
     * @throws IllegalStateException throw an IllegalStateException if it can't
     *                               find a matching subject as that indicates there
     *                               is a misalignment of the executing state and
     *                               the complete list of possible subjects.
     */
    public Subject byTitle(String title) throws IllegalStateException {
        for (Subject subject : this.all()) {
            if (subject.getTitle().equals(title)) {
                return subject;
            }
        }
        throw new IllegalStateException("No such subject!");
    }

    /**
     * Returns detailed string representations of the contents of this
     * subject list.
     *
     * @return detailed string representations of the contents of this
     *         subject list.
     */
    public String getFullDetail() {
        StringBuilder subjectStrings = new StringBuilder();
        int counter = 1;
        for (Subject subject : this.all()) {
            subjectStrings.append(counter);
            subjectStrings.append(". ");
            subjectStrings.append(subject.getFullDetail());
            counter += 1;
        }
        return subjectStrings + "\n";
    }

    /**
     * Returns a brief string representation of the contents of this
     * subject list.
     *
     * @return a brief string representation of the contents of this
     *         subject list.
     */
    @Override
    public String toString() {
        StringBuilder subjectStrings = new StringBuilder();
        int counter = 1;
        for (Subject subject : this.all()) {
            subjectStrings.append(counter);
            subjectStrings.append(". ");
            subjectStrings.append(subject.toString());
            counter += 1;
        }
        return subjectStrings.toString();
    }
}