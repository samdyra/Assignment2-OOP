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
    }

    /**
     * Returns a detailed string representation of this venue
     *
     * @return a detailed string representation of this venue.
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
     * Gets the identifier of the venue.
     *
     * @return The identifier of the venue.
     */
    public String venueId() {
        return null;
    }

    /**
     * Gets a copy of the list of rooms that make up this venue.
     *
     * @return a new list containing the rooms in this venue.
     */
    public List<Room> getRooms() {
        return null;
    }

    /**
     * Gets the number of rows of Desks in this venue.
     *
     * @return The number of rows of desks in this venue.
     */
    public int getRows() {
        return 0;
    }

    /**
     * Gets the number of columns of Desks in this venue.
     *
     * @return The number of columns of desks in this venue.
     */
    public int getColumns() {
        return 0;
    }

    /**
     * Gets the total number of desks in the venue (may be less than
     * rows x columns).
     *
     * @return The total number of desks in the venue.
     */
    public int deskCount() {
        return 0;
    }

    /**
     * Is this an AARA venue?
     *
     * @return True if this is an AARA venue.
     */
    public boolean isAara() {
        return false;
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
        return false;
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
        return false;
    }

    /**
     * Returns a string representation of this venue. (Returns the venue
     * identifier.)
     *
     * @return The string representation of this venue.
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