package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an exam venue, consisting of one or more Rooms.
 */
public class Venue implements StreamManager, ManageableListItem {
    private String id;
    private int roomCount;
    private RoomList rooms;
    private int rows;
    private int columns;
    private int totalDesks;
    private boolean aara;



    /**
     * Constructs a new Venue object, consisting of one or more Rooms. Where a
     * venue consists of multiple rooms, these are typically contiguous with the
     * room-dividers removed to make a single large venue. Seating plans for
     * venues ARE DIFFERENT to the plans for the individual rooms and SOME
     * joined rooms MAY fit more desks than the individual rooms would have.
     *
     * @param id         a String identifier for the venue (e.g. "E101" or "L1+L2").
     * @param roomCount  the number of rooms used in the venue; must be one of
     *                   1, 2, or 3.
     * @param rooms      the list of room objects - there must be at least one room.
     * @param rows       the number of rows of Desks, rows run across the room,
     *                   counted front to back.
     * @param columns    the number of columns of Desks, columns run front to back,
     *                   counted left to right.
     * @param totalDesks the total available Desks (may be less than rows x columns).
     * @param aara       the venue is to be used for AARA exam sessions.
     * @param registry   the global object registry, needed to resolve textual
     *                   Subject names
     */
    public Venue(String id, int roomCount, RoomList rooms, int rows,
                 int columns, int totalDesks, boolean aara,
                 Registry registry) {
        this.id = id;
        this.roomCount = roomCount;
        this.rooms = rooms;
        this.rows = rows;
        this.columns = columns;
        this.totalDesks = totalDesks;
        this.aara = aara;
        registry.add(this, Venue.class);
    }

    /**
     * Constructs a Venue by reading a description from a text stream
     *
     * @param br       BufferedReader opened and ready to read from
     * @param registry the global object registry, needed to resolve textual
     *                 Subject names
     * @param nthItem  the index number of this serialized object
     * @throws IOException      on any read failure
     * @throws RuntimeException
     */
    public Venue(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        streamIn(br, registry, nthItem);
        registry.add(this, Venue.class);
    }

    /**
     * Used to write data to the disk.
     *
     * The format of the text written to the stream must be matched exactly by
     * streamIn, so it is very important to format the output as described.
     *
     * 7. W1+W2 (15 desks)
     * Room Count: 2, Rooms: S101 S102, Rows: 3, Columns: 5, Desks: 15,
     * AARA: true
     *
     * @param bw      writer, already opened. Your data should be written at
     *                the current file position
     * @param nthItem a number representing this item's position in the stream.
     *                Used for sanity checks
     * @throws IOException on any stream related issues
     */
    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        // example line 1 "1. V1 (25 Non-AARA desks)"
        String handleAARAText = this.aara ? " AARA" : " Non-AARA";

        bw.write(nthItem + ". " + id + " (" + totalDesks
                + handleAARAText + " desks)"
                + System.lineSeparator());

        // example line 2 "Room Count: 1, Rooms: R1, Rows: 5, Columns: 5, Desks: 25, AARA: false"

        // build room names
        StringBuilder roomNames = new StringBuilder();
        boolean firstRoom = true;
        for (Room r : rooms.all()) {
            if (!firstRoom) {
                roomNames.append(" ");
            }
            roomNames.append(r.roomId());
            firstRoom = false;
        }

        bw.write("Room Count: " + roomCount
                + ", Rooms: " + roomNames
                + ", Rows: " + rows
                + ", Columns: " + columns
                + ", Desks: " + totalDesks
                + ", AARA: " + aara
                + System.lineSeparator());
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
     * @throws RuntimeException on any logic related issues
     */
    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem)
            throws RuntimeException {
        // line 1 example: "1. V1 (25 Non-AARA desks)"
        String header = Utilities.getLine(br);
        if (header == null) {
            throw new RuntimeException("EOF reading Venue #" + nthItem);
        }

        String[] partsOfHeader = header.split("\\. ");
        int index = Utilities.toInt(partsOfHeader[0], "Number format exception parsing Venue "
                + nthItem + " header");
        if (index != nthItem) {
            throw new RuntimeException("Venue index out of sync!");
        }

        // get venue id, example: 4. V1+V2+V3 (80 Non-AARA desks)
        // get the V1+V2+V3
        String[] idParts = partsOfHeader[1].split(" ");
        this.id = idParts[0];

        // Line 2 example : "Room Count: 1, Rooms: S101, Rows: 5, Columns: 5, Desks: 25, AARA: false"
        String roomDetails = Utilities.getLine(br);
        if (roomDetails == null) {
            throw new RuntimeException("EOF reading Venue #" + nthItem);
        }

        this.rooms = new RoomList(registry);

        String[] details = roomDetails.split(",");
        for (String detail : details) {
            String[] pair = Utilities.keyValuePair(detail.trim());
            if (pair == null) {
                continue;
            }

            String roomDetailKey = pair[0];
            String roomDetailValue = pair[1];

            switch (roomDetailKey) {
                case "Room Count":
                    this.roomCount = Utilities.toInt(pair[1],
                            "Number format exception parsing Venue " + nthItem + " Room Count");
                    break;
                case "Rooms":
                    // rooms are space separated: "R1 R2 R3"
                    String[] roomNames = roomDetailValue.split(" ");
                    for (String roomName : roomNames) {
                        Room room = registry.get(roomName.trim(), Room.class);
                        rooms.add(room);
                    }
                    break;
                case "Rows":
                    this.rows = Utilities.toInt(roomDetailValue,
                            "Number format exception parsing Venue " + nthItem + " Rows");
                    break;
                case "Columns":
                    this.columns = Utilities.toInt(roomDetailValue,
                            "Number format exception parsing Venue " + nthItem + " Columns");
                    break;
                case "Desks":
                    this.totalDesks = Utilities.toInt(roomDetailValue,
                            "Number format exception parsing Venue " + nthItem + " Desks");
                    break;
                case "AARA":
                    this.aara = Utilities.toBoolean(roomDetailValue,
                            "Boolean format exception parsing Venue " + nthItem + " AARA");
                    break;
            }
        }
    }

    /**
     * Returns a detailed string representation of this venue
     *
     * @return a detailed string representation of this venue.
     */
    @Override
    public String getFullDetail() {
        StringBuilder roomNames = new StringBuilder();
        boolean firstRoom = true;
        for (Room r : rooms.all()) {
            if (!firstRoom) {
                roomNames.append(" ");
            }
            roomNames.append(r.roomId());
            firstRoom = false;
        }
        return "Room Count: " + roomCount
                + ", Rooms: " + roomNames
                + ", Rows: " + rows
                + ", Columns: " + columns
                + ", Desks: " + totalDesks
                + ", AARA: " + aara
                + System.lineSeparator();
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
        return new Object[]{id, totalDesks, aara};
    }

    /**
     * Return a unique string identifying us
     *
     * @return a unique string identifying us
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Gets the identifier of the venue.
     *
     * @return The identifier of the venue.
     */
    public String venueId() {
        return this.id;
    }

    /**
     * Gets a copy of the list of rooms that make up this venue.
     *
     * @return a new list containing the rooms in this venue.
     */
    public List<Room> getRooms() {
        return new ArrayList<>(this.rooms.all());
    }

    /**
     * Gets the number of rows of Desks in this venue.
     *
     * @return The number of rows of desks in this venue.
     */
    public int getRows() {
        return this.rows;
    }

    /**
     * Gets the number of columns of Desks in this venue.
     *
     * @return The number of columns of desks in this venue.
     */
    public int getColumns() {
        return this.columns;
    }

    /**
     * Gets the total number of desks in the venue (may be less than
     * rows x columns).
     *
     * @return The total number of desks in the venue.
     */
    public int deskCount() {
        return this.totalDesks;
    }

    /**
     * Is this an AARA venue?
     *
     * @return True if this is an AARA venue.
     */
    public boolean isAara() {
        return this.aara;
    }

    /**
     * Check if the venue type is AARA or not. Print the appropriate message
     * if the type doesn't match. Messages are: This is an AARA venue. This
     * is NOT an AARA venue.
     *
     * @param aara the venue is to be used for AARA exam sessions.
     * @return True if this venue is the same AARA type as the parameter.
     */
    public boolean checkVenueType(boolean aara) {
        if (this.aara != aara) {
            if (this.aara) {
                System.out.println("This is an AARA venue.");
            } else {
                System.out.println("This is NOT an AARA venue.");
            }
            return false;
        }
        return true;
    }

    /**
     * Checks if numberStudents will fit in this venue. Otherwise, print the
     * message: This venue only has (totalDesks) desks, (numberStudents)
     * students will not fit in this venue!
     *
     * @param numberStudents the number of students to test if they can fit
     *                       in this venue.
     * @return True if numberStudents will fit in this venue.
     */
    public boolean willFit(int numberStudents) {
        if (numberStudents > totalDesks) {
            System.out.println("This venue only has " + totalDesks
                    + " desks, " + numberStudents
                    + " students will not fit in this venue!");
            return false;
        }
        return true;
    }

    /**
     * Returns a string representation of this venue. (Returns the venue
     * identifier.)
     *
     * @return The string representation of this venue.
     */
    @Override
    public String toString() {
        return this.id;
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
        Venue other = (Venue) o;
        return other.id.equals(this.id)
                && other.roomCount == this.roomCount
                && other.rows == this.rows
                && other.columns == this.columns
                && other.totalDesks == this.totalDesks
                && other.aara == this.aara;
    }

    /**
     * return the hash value of this object
     *
     * @return the hash value of this object
     */
    @Override
    public int hashCode() {
        return this.id.hashCode()
                + 2 * this.roomCount
                + 3 * this.rows
                + 5 * this.columns
                + 7 * this.totalDesks
                + 11 * Boolean.hashCode(this.aara);
    }
}