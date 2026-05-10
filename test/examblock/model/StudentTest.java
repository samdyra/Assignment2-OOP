package examblock.model;

import org.junit.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class StudentTest {
    private Student student;
    private Registry registry;
    private Subject subject1;
    private Subject subject2;

    @Before
    public void setUp() {
        registry = new Registry();
        subject1 = new Subject("Essential English", "English course.", registry);
        subject2 = new Subject("Essential Mathematics", "Maths course.", registry);
        student = new Student(9999365663L, "Liam Alexander", "Smith",
                8, 12, 2007, "Blue", false, registry);
        student.addSubject(subject1);
        student.addSubject(subject2);
    }

    // registry tests
    @Test
    public void testRegistryRegistration() {
        assertTrue(registry.contains(String.valueOf(9999365663L), Student.class));
    }

    @Test
    public void testRegistryLookup() {
        Student found = registry.get(String.valueOf(9999365663L), Student.class);
        assertEquals(student, found);
    }

    // constructor tests

    @Test
    public void testConstructor() {
        assertEquals(Long.valueOf(9999365663L), student.getLui());
        assertEquals("Liam Alexander", student.givenNames());
        assertEquals("Smith", student.familyName());
        assertEquals("Blue", student.getHouse());
        assertFalse(student.isAara());
    }

    @Test
    public void testConstructorNoAaraDefaults() {
        Student noAaraStudent = new Student(9999111111L, "Dwiputra", "Mulia",
                1, 1, 2007, "Red", new Registry());
        assertFalse(noAaraStudent.isAara());
    }

    @Test
    public void testConstructorWithAara() {
        Student aaraStudent = new Student(9999222222L, "Dwiputra", "Mulia",
                1, 1, 2007, "Green", true, new Registry());
        assertTrue(aaraStudent.isAara());
    }

    @Test
    public void testConstructorWithDirtyGivenNames() {
        Student dirtyStudent = new Student(9999333333L, "Dwiputra45 Sam!!!",
                "Mulia", 1, 5, 2008, "Yellow", new Registry());
        assertEquals("Dwiputra Sam", dirtyStudent.givenNames());
    }

    @Test
    public void testConstructorWithExtraSpacesInName() {
        Student dirtyStudent = new Student(9999444444L, "  Dwiputra   Sam  ",
                "Mulia", 25, 5, 2008, "Red", new Registry());
        assertEquals("Dwiputra Sam", dirtyStudent.givenNames());
    }

    // buffered reader constructor test

    @Test
    public void testBufferedReaderConstructor() throws IOException {
        String input = "1. LIAM ALEXANDER SMITH\n"
                + "LUI: 9999365663, Family Name: Smith, Given Name(s): Liam Alexander, "
                + "Date of Birth: 2007-12-08, House: Blue, AARA: false\n"
                + "Subjects: Essential English, Essential Mathematics\n";
        BufferedReader br = new BufferedReader(new StringReader(input));

        Student brStudent = new Student(br, registry, 1);

        assertEquals(Long.valueOf(9999365663L), brStudent.getLui());
        assertEquals("Smith", brStudent.familyName());
        assertEquals("Liam Alexander", brStudent.givenNames());
        assertEquals("Blue", brStudent.getHouse());
        assertFalse(brStudent.isAara());
    }

    @Test
    public void testBufferedReaderConstructorWithDirtyInput() throws IOException {
        String input = "1. JOHN MICHAEL HALL\n"
                + "LUI: 9999572152, Family Name: Hall, Given Name(s): John   Michael, "
                + "Date of Birth: 2008-05-25, House: Red, AARA: false\n"
                + "Subjects: Essential English, Essential Mathematics\n";
        BufferedReader br = new BufferedReader(new StringReader(input));

        Student brStudent = new Student(br, registry, 1);

        assertEquals("John Michael", brStudent.givenNames());
        assertEquals("Hall", brStudent.familyName());
    }

    // sanitise name tests (direct test)
    @Test
    public void testSanitiseNameNormal() {
        String result = student.sanitiseName("Dwiputra Sam");
        assertEquals("Dwiputra Sam", result);
    }

    @Test
    public void testSanitiseNameWithNumbers() {
        String result = student.sanitiseName("Dwiputra45 Sam!!!");
        assertEquals("Dwiputra Sam", result);
    }

    @Test
    public void testSanitiseNameWithExtraSpaces() {
        String result = student.sanitiseName("  Dwiputra   Sam  ");
        assertEquals("Dwiputra Sam", result);
    }

    @Test
    public void testSanitiseNameWithHyphen() {
        String result = student.sanitiseName("Walsh-Bennett");
        assertEquals("Walsh-Bennett", result);
    }

    @Test
    public void testSanitiseNameWithApostrophe() {
        String result = student.sanitiseName("O'Bennett");
        assertEquals("O'Bennett", result);
    }

    // name methods tests
    @Test
    public void testFirstName() {
        assertEquals("Liam", student.firstName());
    }

    @Test
    public void testFamilyName() {
        assertEquals("Smith", student.familyName());
    }

    @Test
    public void testShortName() {
        assertEquals("Liam Smith", student.shortName());
    }

    @Test
    public void testFullName() {
        assertEquals("Liam Alexander Smith", student.fullName());
    }

    @Test
    public void testGivenNames() {
        assertEquals("Liam Alexander", student.givenNames());
    }

    // other getters
    @Test
    public void testGetDob() {
        assertEquals(2007, student.getDob().getYear());
        assertEquals(12, student.getDob().getMonthValue());
        assertEquals(8, student.getDob().getDayOfMonth());
    }

    @Test
    public void testGetHouse() {
        assertEquals("Blue", student.getHouse());
    }

    @Test
    public void testIsAara() {
        assertFalse(student.isAara());
    }

    @Test
    public void testGetLui() {
        assertEquals(Long.valueOf(9999365663L), student.getLui());
    }

    // setter tests
    @Test
    public void testChangeLui() {
        student.changeLui(9999000000L);
        assertEquals(Long.valueOf(9999000000L), student.getLui());
    }

    @Test
    public void testSetGiven() {
        student.setGiven("Dwiputra Sam");
        assertEquals("Dwiputra Sam", student.givenNames());
    }

    @Test
    public void testSetGivenWithDirtyInput() {
        student.setGiven("  Dwiputra123   Sam!!!  ");
        assertEquals("Dwiputra Sam", student.givenNames());
    }

    @Test
    public void testSetFamily() {
        student.setFamily("Mulia");
        assertEquals("Mulia", student.familyName());
    }

    @Test
    public void testSetFamilyWithDirtyInput() {
        student.setFamily("  Mulia123  ");
        assertEquals("Mulia", student.familyName());
    }

    // subject/exam management tests
    @Test
    public void testAddSubject() {
        Subject newSubject = new Subject("Biology", "Study of life.", registry);
        student.addSubject(newSubject);
        assertTrue(student.getSubjects().all().contains(newSubject));
    }

    @Test
    public void testRemoveSubject() {
        student.removeSubject(subject1);
        assertFalse(student.getSubjects().all().contains(subject1));
    }

    @Test
    public void testGetSubjects() {
        assertNotNull(student.getSubjects());
        assertEquals(2, student.getSubjects().size());
    }

    @Test
    public void testGetExams() {
        assertNotNull(student.getExams());
    }

    // steamout/streamin tests

    @Test
    public void testStreamOut() throws IOException {
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        student.streamOut(bw, 1);
        bw.flush();

        String output = sw.toString();
        assertTrue(output.contains("1. LIAM ALEXANDER SMITH"));
        assertTrue(output.contains("LUI: 9999365663"));
        assertTrue(output.contains("Family Name: Smith"));
        assertTrue(output.contains("Given Name(s): Liam Alexander"));
        assertTrue(output.contains("House: Blue"));
        assertTrue(output.contains("AARA: false"));
        assertTrue(output.contains("Subjects: "));
    }

    @Test
    public void testStreamInAndOutRoundTrip() throws IOException {
        // write out
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        student.streamOut(bw, 1);
        bw.flush();

        // read back in
        Registry roundTripRegistry = new Registry();
        new Subject("Essential English", "English course.", roundTripRegistry);
        new Subject("Essential Mathematics", "Maths course.", roundTripRegistry);
        BufferedReader br = new BufferedReader(new StringReader(sw.toString()));
        Student roundTrippedStudent = new Student(br, roundTripRegistry, 1);

        assertEquals(student.getLui(), roundTrippedStudent.getLui());
        assertEquals(student.familyName(), roundTrippedStudent.familyName());
        assertEquals(student.givenNames(), roundTrippedStudent.givenNames());
        assertEquals(student.getHouse(), roundTrippedStudent.getHouse());
        assertEquals(student.isAara(), roundTrippedStudent.isAara());
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInWrongIndex() throws IOException {
        String input = "5. LIAM ALEXANDER SMITH\n"
                + "LUI: 9999365663, Family Name: Smith, Given Name(s): Liam Alexander, "
                + "Date of Birth: 2007-12-08, House: Blue, AARA: false\n"
                + "Subjects: Essential English\n";
        BufferedReader br = new BufferedReader(new StringReader(input));
        new Student(br, registry, 1);
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInEmptyInput() throws IOException {
        String input = "";
        BufferedReader br = new BufferedReader(new StringReader(input));
        new Student(br, registry, 1);
    }

    // others
    @Test
    public void testToString() {
        assertTrue(student.toString().contains("LIAM ALEXANDER SMITH"));
    }

    @Test
    public void testGetId() {
        assertEquals(String.valueOf(9999365663L), student.getId());
    }

    @Test
    public void testGetFullDetail() {
        String detail = student.getFullDetail();
        assertTrue(detail.contains("LUI: 9999365663"));
        assertTrue(detail.contains("Family Name: Smith"));
        assertTrue(detail.contains("Given Name(s): Liam Alexander"));
    }

    // equals/hashcode tests

    @Test
    public void testEqualsSameObject() {
        assertEquals(student, student);
    }

    @Test
    public void testEqualsMatchingStudent() {
        Registry otherRegistry = new Registry();
        Student otherStudent = new Student(9999365663L, "Liam Alexander", "Smith",
                8, 12, 2007, "Blue", false, otherRegistry);
        assertEquals(student, otherStudent);
    }

    @Test
    public void testEqualsDifferentLui() {
        Student otherStudent = new Student(9999000000L, "Dwiputra Sam", "Mulia",
                8, 12, 2007, "Blue", false, new Registry());
        assertNotEquals(student, otherStudent);
    }

    @Test
    public void testEqualsDifferentName() {
        Student otherStudent = new Student(9999365663L, "Dwiputra Sam", "Mulia",
                8, 12, 2007, "Blue", false, new Registry());
        assertNotEquals(student, otherStudent);
    }

    @Test
    public void testEqualsNull() {
        assertNotEquals(null, student);
    }

    @Test
    public void testEqualsDifferentClass() {
        assertNotEquals("not a student", student);
    }

    @Test
    public void testHashCodeEquality() {
        Registry otherRegistry = new Registry();
        Student otherStudent = new Student(9999365663L, "Liam Alexander", "Smith",
                8, 12, 2007, "Blue", false, otherRegistry);
        assertEquals(student.hashCode(), otherStudent.hashCode());
    }

    @Test
    public void testHashCodeInequality() {
        Student differentStudent = new Student(9999000000L, "Dwiputra", "Mulia",
                1, 1, 2007, "Green", new Registry());
        assertNotEquals(student.hashCode(), differentStudent.hashCode());
    }

    // toTableRow test
    @Test
    public void testToTableRow() {
        Object[] row = student.toTableRow();
        assertNotNull(row);
        assertTrue(row.length > 0);
    }
}