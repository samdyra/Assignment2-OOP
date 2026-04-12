package examblock.model;

import java.util.Optional;

/**
 * A collection object for holding and managing {@link Exam}s.
 */
public class ExamList extends ListManager<Exam> {

    /**
     * constructor
     *
     * @param registry registry
     */
    public ExamList(Registry registry) {
        super(Exam::new, registry, Exam.class);
    }

    /**
     * Get the first {@link Exam} with a matching {@link Subject} {@code title}.
     *
     * @param title the {@code title} of the {@link Exam}'s {@link Subject} to be found.
     * @return first {@link Exam} with a matching {@link Subject} {@code title}, if it exists.
     * @throws IllegalStateException - throw an IllegalStateException if it can't
     *                               find a matching exam as that indicates there is a misalignment
     *                               of the executing state and the complete list of possible exams.
     */
    public Exam bySubjectTitle(String title) throws IllegalStateException {

        Optional<Exam> item = all()
                .stream()
                .filter(e -> e
                        .getSubject()
                        .getTitle()
                        .equals(title))
                .findFirst();

        if (item.isPresent()) {
            return item.get();
        }

        throw new IllegalStateException("No such exam!");
    }

    /**
     * Returns detailed string representations of the contents of this exam list.
     *
     * @return detailed string representations of the contents of this exam list.
     */
    public String getFullDetail() {
        StringBuilder text = new StringBuilder();
        int counter = 1;
        for (Exam exam : getItems()) {
            text.append(counter);
            text.append(". ");
            text.append(exam.getSubject().toString().toUpperCase());
            text.append(System.lineSeparator());
            text.append(exam.getFullDetail());
            text.append(System.lineSeparator());
            counter += 1;
        }
        return text.toString();
    }

    /**
     * Finds an exam given it's short title
     *
     * @param shortTitle the exam to find
     * @return the exam matching the short title, which looks like this:
     * "Year 12 Internal Assessment The Exam Name Paper N"
     * @throws IllegalStateException if the exam can't be found
     */
    public Exam byShortTitle(String shortTitle) throws IllegalStateException {
        Optional<Exam> exam = all()
                .stream()
                .filter(e -> e.getShortTitle().equals(shortTitle))
                .findFirst();

        if (exam.isPresent()) {
            return exam.get();
        } else {
            throw new IllegalStateException("No such exam!");
        }
    }

    /**
     * Returns a string representation of the contents of the exam manager
     *
     * @return a string representation of the contents of the exam manager
     */
    @Override
    public String toString() {
        StringBuilder examStrings = new StringBuilder();
        int counter = 1;
        for (Exam exam : getItems()) {
            examStrings.append(counter);
            examStrings.append(". ");
            examStrings.append(exam.toString());
            examStrings.append("\n");
            counter += 1;
        }
        return examStrings.toString();
    }
}