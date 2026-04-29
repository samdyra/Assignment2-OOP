package examblock.model;

/**
 * A collection object for holding and managing {@link Unit}s.
 */
public class UnitList extends ListManager<Unit> {

    /**
     * constructor
     *
     * @param registry registry
     */
    public UnitList(Registry registry) {
        super(Unit::new, registry, Unit.class);
    }

    /**
     * Get the first {@link Unit} with a matching {@code Subject} and {@code unitId}.
     *
     * @param subjectTitle the {@code title} of the parent {@code Subject} of the
     *                     {@code Unit} to be found.
     * @param unitId       the unit identifier of the {@code Subject} {@code Unit}
     *                     to be found.
     * @return first {@link Unit} with a matching subject {@code title} and
     *         {@code unitId}, if it exists.
     * @throws IllegalStateException throw an IllegalStateException if it can't
     *                               find a matching unit as that indicates there
     *                               is a misalignment of the executing state and
     *                               the complete list of possible units.
     */
    public Unit getUnit(String subjectTitle, Character unitId)
            throws IllegalStateException {
        for (Unit unit : all()) {
            if (unit.getSubject().getTitle().equals(subjectTitle)
                    && unit.id().equals(unitId)) {
                return unit;
            }
        }
        throw new IllegalStateException("No such unit!");
    }

    /**
     * Returns detailed string representations of the contents of this
     * unit list.
     *
     * @return detailed string representations of the contents of this
     *         unit list.
     */
    public String getFullDetail() {
        StringBuilder unitStrings = new StringBuilder();
        int counter = 1;
        for (Unit unit : all()) {
            unitStrings.append(counter);
            unitStrings.append(". ");
            unitStrings.append(unit.getFullDetail());
            counter += 1;
        }
        return unitStrings + "\n";
    }

    /**
     * Returns a string representation of the contents of the unit list
     *
     * @return a string representation of the contents of the unit list
     */
    @Override
    public String toString() {
        StringBuilder unitStrings = new StringBuilder();
        int counter = 1;
        for (Unit unit : all()) {
            unitStrings.append(counter);
            unitStrings.append(". ");
            unitStrings.append(unit.toString());
            unitStrings.append("\n");
            counter += 1;
        }
        return unitStrings.toString();
    }
}