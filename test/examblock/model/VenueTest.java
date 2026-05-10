package examblock.model;

import org.junit.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import static org.junit.Assert.*;

public class VenueTest {
    private Venue venue;
    private Registry registry;
    private Room room1;
    private RoomList roomList;

    @Before
    public void setUp() {
        registry = new Registry();
        room1 = new Room("R1", registry);
        roomList = new RoomList(registry);
        roomList.add(room1);
        venue = new Venue("V1", 1, roomList, 5, 5, 25, false, registry);
    }

    // registry tests
    @Test
    public void testRegistryRegistration() {
        assertTrue(registry.contains("V1", Venue.class));
    }

    @Test
    public void testRegistryLookup() {
        Venue found = registry.get("V1", Venue.class);
        assertEquals(venue, found);
    }

    // constructor tests

    @Test
    public void testConstructor() {
        assertEquals("V1", venue.venueId());
        assertEquals(5, venue.getRows());
        assertEquals(5, venue.getColumns());
        assertEquals(25, venue.deskCount());
        assertFalse(venue.isAara());
    }

    @Test
    public void testConstructorMultiRoom() {
        Registry multiRegistry = new Registry();
        Room multiRoom1 = new Room("S101", multiRegistry);
        Room multiRoom2 = new Room("S102", multiRegistry);
        RoomList multiRoomList = new RoomList(multiRegistry);
        multiRoomList.add(multiRoom1);
        multiRoomList.add(multiRoom2);
        Venue multiVenue = new Venue("W1+W2", 2, multiRoomList, 3, 5, 15, true, multiRegistry);

        assertEquals("W1+W2", multiVenue.venueId());
        assertEquals(2, multiVenue.getRooms().size());
        assertEquals(3, multiVenue.getRows());
        assertEquals(5, multiVenue.getColumns());
        assertEquals(15, multiVenue.deskCount());
        assertTrue(multiVenue.isAara());
    }

    @Test
    public void testConstructorAaraVenue() {
        Registry aaraRegistry = new Registry();
        Room aaraRoom = new Room("S101", aaraRegistry);
        RoomList aaraRoomList = new RoomList(aaraRegistry);
        aaraRoomList.add(aaraRoom);
        Venue aaraVenue = new Venue("W1", 1, aaraRoomList, 1, 5, 5, true, aaraRegistry);

        assertTrue(aaraVenue.isAara());
        assertEquals(5, aaraVenue.deskCount());
    }

    // buffered reader constructor test

    @Test
    public void testBufferedReaderConstructor() throws IOException {
        Registry brRegistry = new Registry();
        new Room("R1", brRegistry);
        String input = "1. V1 (25 Non-AARA desks)\n"
                + "Room Count: 1, Rooms: R1, Rows: 5, Columns: 5, Desks: 25, AARA: false\n";
        BufferedReader br = new BufferedReader(new StringReader(input));

        Venue brVenue = new Venue(br, brRegistry, 1);

        assertEquals("V1", brVenue.venueId());
        assertEquals(5, brVenue.getRows());
        assertEquals(5, brVenue.getColumns());
        assertEquals(25, brVenue.deskCount());
        assertFalse(brVenue.isAara());
    }

    @Test
    public void testBufferedReaderConstructorMultiRoom() throws IOException {
        Registry brRegistry = new Registry();
        new Room("S101", brRegistry);
        new Room("S102", brRegistry);
        String input = "1. W1+W2 (15 AARA desks)\n"
                + "Room Count: 2, Rooms: S101 S102, Rows: 3, Columns: 5, Desks: 15, AARA: true\n";
        BufferedReader br = new BufferedReader(new StringReader(input));

        Venue brVenue = new Venue(br, brRegistry, 1);

        assertEquals("W1+W2", brVenue.venueId());
        assertEquals(2, brVenue.getRooms().size());
        assertEquals(3, brVenue.getRows());
        assertEquals(5, brVenue.getColumns());
        assertEquals(15, brVenue.deskCount());
        assertTrue(brVenue.isAara());
    }

    // getter tests

    @Test
    public void testVenueId() {
        assertEquals("V1", venue.venueId());
    }

    @Test
    public void testGetRooms() {
        List<Room> rooms = venue.getRooms();
        assertNotNull(rooms);
        assertEquals(1, rooms.size());
        assertEquals("R1", rooms.get(0).roomId());
    }

    @Test
    public void testGetRoomsReturnsCopy() {
        List<Room> rooms1 = venue.getRooms();
        List<Room> rooms2 = venue.getRooms();
        assertNotSame(rooms1, rooms2);
    }

    @Test
    public void testGetRows() {
        assertEquals(5, venue.getRows());
    }

    @Test
    public void testGetColumns() {
        assertEquals(5, venue.getColumns());
    }

    @Test
    public void testDeskCount() {
        assertEquals(25, venue.deskCount());
    }

    @Test
    public void testIsAara() {
        assertFalse(venue.isAara());
    }

    // checkVenueType tests

    @Test
    public void testCheckVenueTypeMatching() {
        assertTrue(venue.checkVenueType(false));
    }

    @Test
    public void testCheckVenueTypeNotMatching() {
        assertFalse(venue.checkVenueType(true));
    }

    // willFit tests

    @Test
    public void testWillFitEnoughDesks() {
        assertTrue(venue.willFit(20));
    }

    @Test
    public void testWillFitExactDesks() {
        assertTrue(venue.willFit(25));
    }

    @Test
    public void testWillFitTooManyStudents() {
        assertFalse(venue.willFit(30));
    }

    // steamout/streamin tests

    @Test
    public void testStreamOut() throws IOException {
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        venue.streamOut(bw, 1);
        bw.flush();

        String output = sw.toString();
        assertTrue(output.contains("1. V1"));
        assertTrue(output.contains("25"));
        assertTrue(output.contains("Non-AARA"));
        assertTrue(output.contains("Room Count: 1"));
        assertTrue(output.contains("Rooms: R1"));
        assertTrue(output.contains("Rows: 5"));
        assertTrue(output.contains("Columns: 5"));
        assertTrue(output.contains("Desks: 25"));
        assertTrue(output.contains("AARA: false"));
    }

    @Test
    public void testStreamInAndOutRoundTrip() throws IOException {
        // write out
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        venue.streamOut(bw, 1);
        bw.flush();

        // read back in
        Registry roundTripRegistry = new Registry();
        new Room("R1", roundTripRegistry);
        BufferedReader br = new BufferedReader(new StringReader(sw.toString()));
        Venue roundTrippedVenue = new Venue(br, roundTripRegistry, 1);

        assertEquals(venue.venueId(), roundTrippedVenue.venueId());
        assertEquals(venue.getRows(), roundTrippedVenue.getRows());
        assertEquals(venue.getColumns(), roundTrippedVenue.getColumns());
        assertEquals(venue.deskCount(), roundTrippedVenue.deskCount());
        assertEquals(venue.isAara(), roundTrippedVenue.isAara());
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInWrongIndex() throws IOException {
        String input = "5. V1 (25 Non-AARA desks)\n"
                + "Room Count: 1, Rooms: R1, Rows: 5, Columns: 5, Desks: 25, AARA: false\n";
        BufferedReader br = new BufferedReader(new StringReader(input));
        new Venue(br, registry, 1);
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInEmptyInput() throws IOException {
        String input = "";
        BufferedReader br = new BufferedReader(new StringReader(input));
        new Venue(br, registry, 1);
    }

    // others

    @Test
    public void testToString() {
        assertEquals("V1", venue.toString());
    }

    @Test
    public void testGetId() {
        assertEquals("V1", venue.getId());
    }

    @Test
    public void testGetFullDetail() {
        String detail = venue.getFullDetail();
        assertTrue(detail.contains("Room Count: 1"));
        assertTrue(detail.contains("Rooms: R1"));
        assertTrue(detail.contains("Rows: 5"));
        assertTrue(detail.contains("Columns: 5"));
        assertTrue(detail.contains("Desks: 25"));
        assertTrue(detail.contains("AARA: false"));
    }

    // equals/hashcode tests

    @Test
    public void testEqualsSameObject() {
        assertEquals(venue, venue);
    }

    @Test
    public void testEqualsMatchingVenue() {
        Registry otherRegistry = new Registry();
        Room otherRoom = new Room("R1", otherRegistry);
        RoomList otherRoomList = new RoomList(otherRegistry);
        otherRoomList.add(otherRoom);
        Venue otherVenue = new Venue("V1", 1, otherRoomList, 5, 5, 25, false, otherRegistry);
        assertEquals(venue, otherVenue);
    }

    @Test
    public void testEqualsDifferentId() {
        Registry otherRegistry = new Registry();
        Room otherRoom = new Room("R2", otherRegistry);
        RoomList otherRoomList = new RoomList(otherRegistry);
        otherRoomList.add(otherRoom);
        Venue otherVenue = new Venue("V2", 1, otherRoomList, 5, 5, 25, false, otherRegistry);
        assertNotEquals(venue, otherVenue);
    }

    @Test
    public void testEqualsDifferentDeskCount() {
        Registry otherRegistry = new Registry();
        Room otherRoom = new Room("R1", otherRegistry);
        RoomList otherRoomList = new RoomList(otherRegistry);
        otherRoomList.add(otherRoom);
        Venue otherVenue = new Venue("V1", 1, otherRoomList, 5, 5, 20, false, otherRegistry);
        assertNotEquals(venue, otherVenue);
    }

    @Test
    public void testEqualsNull() {
        assertNotEquals(null, venue);
    }

    @Test
    public void testEqualsDifferentClass() {
        assertNotEquals("not a venue", venue);
    }

    @Test
    public void testHashCodeEquality() {
        Registry otherRegistry = new Registry();
        Room otherRoom = new Room("R1", otherRegistry);
        RoomList otherRoomList = new RoomList(otherRegistry);
        otherRoomList.add(otherRoom);
        Venue otherVenue = new Venue("V1", 1, otherRoomList, 5, 5, 25, false, otherRegistry);
        assertEquals(venue.hashCode(), otherVenue.hashCode());
    }

    @Test
    public void testHashCodeInequality() {
        Registry otherRegistry = new Registry();
        Room otherRoom = new Room("R2", otherRegistry);
        RoomList otherRoomList = new RoomList(otherRegistry);
        otherRoomList.add(otherRoom);
        Venue otherVenue = new Venue("V2", 1, otherRoomList, 3, 3, 9, true, otherRegistry);
        assertNotEquals(venue.hashCode(), otherVenue.hashCode());
    }

    // toTableRow test
    @Test
    public void testToTableRow() {
        Object[] row = venue.toTableRow();
        assertNotNull(row);
        assertTrue(row.length > 0);
    }
}