package examblock.model;

import org.junit.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class SubjectTest {
    private Subject subject;
    private Registry registry;

    @Before
    public void setUp() {
        registry = new Registry();
        subject = new Subject("Accounting", "The study of the management of financial resources of the public sector, businesses, and individuals.", registry);
    }

    // registry tests
    @Test
    public void testRegistryRegistration() {
        assertTrue(registry.contains("Accounting", Subject.class));
    }

    @Test
    public void testRegistryLookup() {
        Subject found = registry.get("Accounting", Subject.class);
        assertEquals(subject, found);
    }

    // constructor tests

    @Test
    public void testConstructor() {
        assertEquals("Accounting", subject.getTitle());
        assertEquals("The study of the management of financial resources of the public sector, businesses, and individuals.", subject.getDescription());
    }

    @Test
    public void testConstructorWithDirtyTitle() {
        Subject dirtySubject = new Subject("  biology  ", "desc.", new Registry());
        assertEquals("Biology", dirtySubject.getTitle());
    }

    @Test
    public void testConstructorWithExtraSpacesInTitle() {
        Subject dirtySubject = new Subject("some   accounting", "desc", registry);
        assertEquals("Some Accounting", dirtySubject.getTitle());
    }

    @Test
    public void testConstructorWithTrailingDotsInTitle() {
        Subject dirtySubject = new Subject("Chemistry...", "desc.", new Registry());
        assertEquals("Chemistry", dirtySubject.getTitle());
    }

    @Test
    public void testConstructorWithMixedCaseTitle() {
        Subject dirtySubject = new Subject("pHySiCs", "desc.", new Registry());
        assertEquals("Physics", dirtySubject.getTitle());
    }

    @Test
    public void testConstructorWithDirtyDescription() {
        Subject dirtySubject = new Subject("Chemistry",
                "  the   study   of   chemistry", new Registry());
        assertEquals("The study of chemistry.", dirtySubject.getDescription());
    }

    @Test
    public void testConstructorWithDescriptionMissingPeriod() {
        Subject dirtySubject = new Subject("Physics",
                "The study of physics", new Registry());
        assertEquals("The study of physics.", dirtySubject.getDescription());
    }

    @Test
    public void testConstructorWithDescriptionLowercaseStart() {
        Subject dirtySubject = new Subject("Drama",
                "the study of drama.", new Registry());
        assertEquals("The study of drama.", dirtySubject.getDescription());
    }

    // buffered reader constructor test

    @Test
    public void testBufferedReaderConstructor() throws IOException {
        String input = "1. BIOLOGY\n"
                + "Biology\n"
                + "\"The study of living systems.\"\n";
        BufferedReader br = new BufferedReader(new StringReader(input));

        Subject brSubject = new Subject(br, new Registry(), 1);

        assertEquals("Biology", brSubject.getTitle());
        assertEquals("The study of living systems.", brSubject.getDescription());
        assertEquals("Biology", brSubject.getId());
    }

    @Test
    public void testBufferedReaderConstructorWithDirtyInput() throws IOException {
        String input = "1. CHEMISTRY\n"
                + "  CHEMISTRY...\n"
                + "\"  The   study   of   chemistry\"\n";
        BufferedReader br = new BufferedReader(new StringReader(input));

        Subject brSubject = new Subject(br, new Registry(), 1);

        assertEquals("Chemistry", brSubject.getTitle());
        assertEquals("The study of chemistry.", brSubject.getDescription());
    }


    // sanitise title tests (direct test, not from constructor)
    @Test
    public void testSanitiseTitleWithNumbers() {
        String result = subject.sanitiseTitle("Mathematics 2");
        assertEquals("Mathematics 2", result);
    }

    @Test
    public void testSanitiseTitleWithRomanNumerals() {
        String result = subject.sanitiseTitle("Unit IV");
        assertEquals("Unit IV", result);
    }

    @Test
    public void testSanitiseTitleWithInternalPunctuation() {
        String result = subject.sanitiseTitle("English & Literature");
        assertTrue(result.contains("&"));
    }

    @Test
    public void testSanitiseTitleAllDirtyCombined() {
        String result = subject.sanitiseTitle("  eNgLiSh   aNd   LiTeRaTuRe   eXtEnSiOn...");
        assertEquals("English And Literature Extension", result);
    }

    // direct sanitise desc test
    @Test
    public void testSanitiseDescriptionDirect() {
        String result = subject.sanitiseDescription("  the   study   of   things");
        assertEquals("The study of things.", result);
    }

    @Test
    public void testSanitiseDescriptionAlreadyClean() {
        String result = subject.sanitiseDescription("Already clean description.");
        assertEquals("Already clean description.", result);
    }


    // steamout/streamin tests

    @Test
    public void testStreamOut() throws IOException {
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        subject.streamOut(bw, 1);
        bw.flush();

        String output = sw.toString();
        assertTrue(output.contains("1. "));
        assertTrue(output.contains("ACCOUNTING"));
        assertTrue(output.contains("Accounting"));
        assertTrue(output.contains("\"The study of the management of financial resources of the public sector, businesses, and individuals.\""));
    }

    @Test
    public void testStreamInAndOutRoundTrip() throws IOException {
        // write out
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        subject.streamOut(bw, 1);
        bw.flush();

        // read back in
        BufferedReader br = new BufferedReader(new StringReader(sw.toString()));
        Subject roundTrippedSubject = new Subject(br, new Registry(), 1);

        assertEquals(subject.getTitle(), roundTrippedSubject.getTitle());
        assertEquals(subject.getDescription(), roundTrippedSubject.getDescription());
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInWrongIndex() throws IOException {
        String input = "5. ACCOUNTING\nAccounting\n\"Some description.\"\n";
        BufferedReader br = new BufferedReader(new StringReader(input));
        new Subject(br, new Registry(), 1);
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInEmptyInput() throws IOException {
        String input = "";
        BufferedReader br = new BufferedReader(new StringReader(input));
        new Subject(br, new Registry(), 1);
    }

    // others
    @Test
    public void testToString() {
        assertTrue(subject.toString().contains("ACCOUNTING"));
    }

    @Test
    public void testGetId() {
        assertEquals("Accounting", subject.getId());
    }

    @Test
    public void testGetFullDetail() {
        String fullDetail = subject.getFullDetail();
        assertTrue(fullDetail.contains("ACCOUNTING"));
        assertTrue(fullDetail.contains("\"The study of the management of financial resources of the public sector, businesses, and individuals.\""));
    }

    // equals/hashcode tests

    @Test
    public void testEqualsSameObject() {
        assertEquals(subject, subject);
    }

    @Test
    public void testEqualsMatchingSubject() {
        Subject otherSubject = new Subject("Accounting",
                "The study of the management of financial resources of the public sector, businesses, and individuals.",
                new Registry());
        assertEquals(subject, otherSubject);
    }

    @Test
    public void testEqualsDifferentTitle() {
        Subject otherSubject = new Subject("diffent title",
                "The study of the management of financial resources of the public sector, businesses, and individuals.",
                new Registry());
        assertNotEquals(subject, otherSubject);
    }

    @Test
    public void testEqualsDifferentDescription() {
        Subject otherSubject = new Subject("Accounting",
                "different desc.",
                new Registry());
        assertNotEquals(subject, otherSubject);
    }

    @Test
    public void testEqualsNull() {
        assertNotEquals(null, subject);
    }

    @Test
    public void testEqualsDifferentClass() {
        assertNotEquals("Accounting", subject);
    }

    @Test
    public void testHashCodeEquality() {
        Registry otherRegistry = new Registry();
        Subject otherSubject = new Subject("Accounting",
                "The study of the management of financial resources of the public sector, businesses, and individuals.",
                otherRegistry);
        assertEquals(subject.hashCode(), otherSubject.hashCode());
    }

    @Test
    public void testHashCodeInequality() {
        Subject differentSubject = new Subject("Biology", "The study of life.", new Registry());
        assertNotEquals(subject.hashCode(), differentSubject.hashCode());
    }


    @Test
    public void testToTableRow() {
        Object[] row = subject.toTableRow();
        assertNotNull(row);
        assertTrue(row.length > 0);
        assertEquals("Accounting", row[0]);
    }
}