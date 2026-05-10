package examblock.model;

import org.junit.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class UnitTest {
    private Unit unit;
    private Subject subject;
    private Registry registry;

    @Before
    public void setUp() {
        registry = new Registry();
        subject = new Subject("Accounting", "The study of finance.", registry);
        unit = new Unit(subject, '3', "Managing resources",
                "Manage the resources of a sole trader business.", registry);
    }

    // registry tests

    @Test
    public void testRegistryRegistration() {
        assertTrue(registry.contains(unit.getId(), Unit.class));
    }

    @Test
    public void testRegistryLookup() {
        Unit found = registry.get(unit.getId(), Unit.class);
        assertEquals(unit, found);
    }

    // constructor tests

    @Test
    public void testConstructor() {
        assertEquals("Managing resources", unit.getDescription());
        assertEquals(subject, unit.getSubject());
        assertEquals(Character.valueOf('3'), unit.id());
    }

    @Test
    public void testConstructorWithDirtyTitle() {
        Unit dirtyUnit = new Unit(subject, '4', "  some   title...  ",
                "desc.", new Registry());
        assertTrue(dirtyUnit.toString().contains("some title"));
    }

    @Test
    public void testConstructorWithExtraSpacesInTitle() {
        Unit dirtyUnit = new Unit(subject, '4', "managing   resources",
                "desc.", new Registry());
        assertEquals("managing resources",
                dirtyUnit.getId().substring(dirtyUnit.getId().lastIndexOf(": ") + 2));
    }

    @Test
    public void testConstructorWithDirtyDescription() {
        Unit dirtyUnit = new Unit(subject, '4', "Some title",
                "  the   study   of   things", new Registry());
        assertEquals("The study of things.", dirtyUnit.getDescription());
    }

    @Test
    public void testConstructorWithDescriptionMissingPeriod() {
        Unit dirtyUnit = new Unit(subject, '4', "Some title",
                "The study of things", new Registry());
        assertEquals("The study of things.", dirtyUnit.getDescription());
    }

    @Test
    public void testConstructorWithDescriptionLowercaseStart() {
        Unit dirtyUnit = new Unit(subject, '4', "Some title",
                "the study of things.", new Registry());
        assertEquals("The study of things.", dirtyUnit.getDescription());
    }

    // buffered reader constructor test

    @Test
    public void testBufferedReaderConstructor() throws IOException {
        String input = "1. ACCOUNTING\n"
                + "Accounting, Unit 4: The big picture\n"
                + "\"The complete process of preparing financial statements.\"\n";
        BufferedReader br = new BufferedReader(new StringReader(input));

        Unit brUnit = new Unit(br, registry, 1);

        assertEquals(subject, brUnit.getSubject());
        assertEquals(Character.valueOf('4'), brUnit.id());
        assertEquals("The complete process of preparing financial statements.",
                brUnit.getDescription());
    }

    @Test
    public void testBufferedReaderConstructorWithDirtyInput() throws IOException {
        String input = "1. ACCOUNTING\n"
                + "Accounting, Unit 4:   some   dirty   title...\n"
                + "\"  some   dirty   description\"\n";
        BufferedReader br = new BufferedReader(new StringReader(input));

        Unit brUnit = new Unit(br, registry, 1);

        assertEquals(subject, brUnit.getSubject());
        assertEquals(Character.valueOf('4'), brUnit.id());
        assertEquals("Some dirty description.", brUnit.getDescription());
    }

    // steamout/streamin tests

    @Test
    public void testStreamOut() throws IOException {
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        unit.streamOut(bw, 1);
        bw.flush();

        String output = sw.toString();
        assertTrue(output.contains("1. "));
        assertTrue(output.contains("ACCOUNTING"));
        assertTrue(output.contains("Accounting, Unit 3: Managing resources"));
        assertTrue(output.contains("\"Manage the resources of a sole trader business.\""));
    }

    @Test
    public void testStreamInAndOutRoundTrip() throws IOException {
        // write out
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        unit.streamOut(bw, 1);
        bw.flush();

        // read back in (need subject in registry for lookup)
        Registry roundTripRegistry = new Registry();
        new Subject("Accounting", "The study of finance.", roundTripRegistry);
        BufferedReader br = new BufferedReader(new StringReader(sw.toString()));
        Unit roundTrippedUnit = new Unit(br, roundTripRegistry, 1);

        assertEquals(unit.id(), roundTrippedUnit.id());
        assertEquals(unit.getDescription(), roundTrippedUnit.getDescription());
        assertEquals(unit.getSubject().getTitle(), roundTrippedUnit.getSubject().getTitle());
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInWrongIndex() throws IOException {
        String input = "5. ACCOUNTING\n"
                + "Accounting, Unit 3: Managing resources\n"
                + "\"Some description.\"\n";
        BufferedReader br = new BufferedReader(new StringReader(input));
        new Unit(br, registry, 1);
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInEmptyInput() throws IOException {
        String input = "";
        BufferedReader br = new BufferedReader(new StringReader(input));
        new Unit(br, registry, 1);
    }

    // others
    @Test
    public void testToString() {
        String result = unit.toString();
        assertTrue(result.contains("3"));
        assertTrue(result.contains("Managing resources"));
    }

    @Test
    public void testGetId() {
        String id = unit.getId();
        assertTrue(id.contains("Accounting"));
        assertTrue(id.contains("Unit 3"));
        assertTrue(id.contains("Managing resources"));
    }

    @Test
    public void testGetFullDetail() {
        String detail = unit.getFullDetail();
        assertTrue(detail.contains("Accounting, Unit 3: Managing resources"));
        assertTrue(detail.contains("\"Manage the resources of a sole trader business.\""));
    }

    @Test
    public void testGetSubject() {
        assertEquals(subject, unit.getSubject());
    }

    @Test
    public void testGetDescription() {
        assertEquals("Manage the resources of a sole trader business.",
                unit.getDescription());
    }

    // equals/hashcode tests

    @Test
    public void testEqualsSameObject() {
        assertEquals(unit, unit);
    }

    @Test
    public void testEqualsMatchingUnit() {
        Registry otherRegistry = new Registry();
        Subject otherSubject = new Subject("Accounting", "The study of finance.", otherRegistry);
        Unit otherUnit = new Unit(otherSubject, '3', "Managing resources",
                "Manage the resources of a sole trader business.", otherRegistry);
        assertEquals(unit, otherUnit);
    }

    @Test
    public void testEqualsDifferentUnitId() {
        Unit otherUnit = new Unit(subject, '4', "Managing resources",
                "Manage the resources of a sole trader business.", new Registry());
        assertNotEquals(unit, otherUnit);
    }

    @Test
    public void testEqualsDifferentDescription() {
        Unit otherUnit = new Unit(subject, '3', "Managing resources",
                "Different description.", new Registry());
        assertNotEquals(unit, otherUnit);
    }

    @Test
    public void testEqualsNull() {
        assertNotEquals(null, unit);
    }

    @Test
    public void testEqualsDifferentClass() {
        assertNotEquals("not a unit", unit);
    }

    @Test
    public void testHashCodeEquality() {
        Registry otherRegistry = new Registry();
        Subject otherSubject = new Subject("Accounting", "The study of finance.", otherRegistry);
        Unit otherUnit = new Unit(otherSubject, '3', "Managing resources",
                "Manage the resources of a sole trader business.", otherRegistry);
        assertEquals(unit.hashCode(), otherUnit.hashCode());
    }

    @Test
    public void testHashCodeInequality() {
        Unit differentUnit = new Unit(subject, '4', "Different title",
                "Different description.", new Registry());
        assertNotEquals(unit.hashCode(), differentUnit.hashCode());
    }

    // toTableRow test
    @Test
    public void testToTableRow() {
        Object[] row = unit.toTableRow();
        assertNotNull(row);
        assertTrue(row.length > 0);
    }
}