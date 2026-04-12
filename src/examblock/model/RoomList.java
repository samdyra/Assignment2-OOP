package examblock.model;

/**
 * A collection object for holding and managing {@link Room}s.
 */
public class RoomList extends ListManager<Room> {

    /**
     * constructor
     *
     * @param registry registry
     */
    public RoomList(Registry registry) {
        super(Room::new, registry, Room.class);
    }

    /**
     * Get the first {@link Room} with a matching {@code id}.
     *
     * @param id - the {@code id} of the {@link Room} to be found.
     * @return The first {@link Room} with a matching {@code id}, if it exists.
     * @throws IllegalStateException - throw an IllegalStateException if it can't
     *                               find a matching room as that indicates there is a misalignment
     *                               of the executing state and the complete list of possible rooms.
     */
    public Room getRoom(String id) throws IllegalStateException {
        for (Room room : all()) {
            if (room.roomId().equals(id)) {
                return room;
            }
        }
        throw new IllegalStateException("No such room!");
    }

    /**
     * Returns detailed string representations of the contents of this room list.
     *
     * @return Detailed string representations of the contents of this room list.
     */
    public String getFullDetail() {

        StringBuilder roomStrings = new StringBuilder();
        int counter = 1;
        for (Room room : all()) {
            roomStrings.append(counter);
            roomStrings.append(". ");
            roomStrings.append(room.getFullDetail());
            roomStrings.append("\n");
            counter += 1;
        }
        return roomStrings + "\n";
    }

    /**
     * Returns a brief string representation of the contents of this room list.
     *
     * @return A brief string representation of the contents of this room list.
     */
    @Override
    public String toString() {

        StringBuilder roomStrings = new StringBuilder();
        int counter = 1;
        for (Room room : all()) {
            roomStrings.append(counter);
            roomStrings.append(". ");
            roomStrings.append(room.toString());
            roomStrings.append("\n");
            counter += 1;
        }
        return roomStrings.toString();
    }

}
