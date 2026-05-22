package examblock.model;

import org.junit.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.Assert.*;

public class SessionTest {
    private Session session;
    private Registry registry;
    private Venue venue;
    private Room room;
    private RoomList roomList;
    private Subject subject1;
    private Subject subject2;
    private Exam exam1;
    private Exam exam2;

    @Before
    public void setUp() {
        registry = new Registry();

        // set up room and venue
        room = new Room("R1", registry);
        roomList = new RoomList(registry);
        roomList.add(room);
        venue = new Venue("V1", 1, roomList, 5, 5, 25, false, registry);

        // subjects
        subject1 = new Subject("English", "english desc.", registry);
        subject2 = new Subject("Literature", "literature desc.", registry);
        // setup exams
        exam1 = new Exam(subject1, Exam.ExamType.INTERNAL,
                10, 3, 2025, 8, 30, registry);
        exam2 = new Exam(subject2, Exam.ExamType.INTERNAL,
                10, 3, 2025, 12, 30, registry);

        // session
        session = new Session(venue, 1,
                LocalDate.of(2025, 3, 10),
                LocalTime.of(8, 30),
                registry);
    }

    // registry tests
    @Test
    public void testRegistryRegistration() {
        assertTrue(registry.contains(session.getId(), Session.class));
    }

    @Test
    public void testRegistryLookup() {
        Session found = registry.get(session.getId(), Session.class);
        assertEquals(session, found);
    }

    // constructor tests

    @Test
    public void testConstructor() {
        assertEquals(venue, session.getVenue());
        assertEquals(1, session.getSessionNumber());
        assertEquals(LocalDate.of(2025, 3, 10), session.getDate());
        assertEquals(LocalTime.of(8, 30), session.getTime());
    }

    @Test
    public void testConstructorEmptyExams() {
        assertTrue(session.getExams().isEmpty());
    }

    @Test
    public void testConstructorDesksInitialised() {
        assertEquals(25, session.getTotalDesks());
        // check desk at first post exists
        assertNotNull(session.getDesk(0, 0));
    }

    @Test
    public void testConstructorDeskNumbering() {
        // desk numbering goes col by col, start from front - back
        // desk 1 is at (0,0)
        assertEquals(1, session.getDesk(0, 0).deskNumber());
        // 1,0
        assertEquals(2, session.getDesk(1, 0).deskNumber());
        // 0,1
        assertEquals(6, session.getDesk(0, 1).deskNumber());
    }

    // getter tests

    @Test
    public void testGetVenue() {
        assertEquals(venue, session.getVenue());
    }

    @Test
    public void testGetSessionNumber() {
        assertEquals(1, session.getSessionNumber());
    }

    @Test
    public void testGetDate() {
        assertEquals(LocalDate.of(2025, 3, 10), session.getDate());
    }

    @Test
    public void testGetTime() {
        assertEquals(LocalTime.of(8, 30), session.getTime());
    }

    @Test
    public void testGetTotalDesks() {
        assertEquals(25, session.getTotalDesks());
    }

    @Test
    public void testGetDesk() {
        Desk desk = session.getDesk(0, 0);
        assertNotNull(desk);
        assertEquals(1, desk.deskNumber());
    }

    // schedule/remove exam tests

    @Test
    public void testScheduleExam() {
        session.scheduleExam(exam1);
        List<Exam> exams = session.getExams();
        assertEquals(1, exams.size());
        assertTrue(exams.contains(exam1));
    }

    @Test
    public void testScheduleMultipleExams() {
        session.scheduleExam(exam1);
        session.scheduleExam(exam2);
        List<Exam> exams = session.getExams();
        assertEquals(2, exams.size());
    }

    @Test
    public void testRemoveExam() {
        session.scheduleExam(exam1);
        session.removeExam(exam1);
        assertTrue(session.getExams().isEmpty());
    }

    // countStudents tests

    @Test
    public void testCountStudentsEmpty() {
        assertEquals(0, session.countStudents());
    }

    @Test
    public void testCountStudentsWithExamAndStudents() {
        // add a student who takes English
        Student student = new Student(9999111111L, "Dwiputra", "Mulia",
                1, 1, 2007, "Blue", false, registry);
        student.addSubject(subject1);

        session.scheduleExam(exam1);
        assertEquals(1, session.countStudents());
    }

    @Test
    public void testCountStudentsAaraMismatch() {
        // aara student in non-AARA
        Student aaraStudent = new Student(9999222222L, "Dwiputra", "Sam",
                1, 1, 2007, "Green", true, registry);
        aaraStudent.addSubject(subject1);

        session.scheduleExam(exam1);
        assertEquals(0, session.countStudents()); // should not be counted (not aara)
    }

    @Test
    public void testCountStudentsMultipleExams() {
        Student student1 = new Student(9999333333L, "Dwiputra", "Sam",
                1, 1, 2007, "Blue", false, registry);
        student1.addSubject(subject1);

        Student student2 = new Student(9999444444L, "Sam", "Mulia",
                2, 2, 2007, "Red", false, registry);
        student2.addSubject(subject2);

        session.scheduleExam(exam1);
        session.scheduleExam(exam2);
        assertEquals(2, session.countStudents());
    }

    // getFullDetail / getId / toString tests

    @Test
    public void testGetId() {
        assertEquals("V1-1", session.getId());
    }

    @Test
    public void testGetFullDetail() {
        session.scheduleExam(exam1);
        String detail = session.getFullDetail();
        assertTrue(detail.contains("Venue: V1"));
        assertTrue(detail.contains("Session Number: 1"));
        assertTrue(detail.contains("Day: 2025-03-10"));
        assertTrue(detail.contains("Start: 08:30"));
        assertTrue(detail.contains("Exams: 1"));
    }

    @Test
    public void testToString() {
        String result = session.toString();
        assertNotNull(result);
    }

    // streamout/streamin tests

    @Test
    public void testStreamOutNoExams() throws IOException {
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        session.streamOut(bw, 1);
        bw.flush();

        String output = sw.toString();
        assertTrue(output.contains("1. "));
        assertTrue(output.contains("Venue: V1"));
        assertTrue(output.contains("Session Number: 1"));
        assertTrue(output.contains("Exams: 0"));
    }

    @Test
    public void testStreamOutWithExam() throws IOException {
        Student student = new Student(9999555555L, "Dwiputra Sam", "Mulia",
                1, 1, 2007, "Blue", false, registry);
        student.addSubject(subject1);
        session.scheduleExam(exam1);

        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        session.streamOut(bw, 1);
        bw.flush();

        String output = sw.toString();
        assertTrue(output.contains("Venue: V1"));
        assertTrue(output.contains("Exams: 1"));
        assertTrue(output.contains("Year 12 Internal Assessment English"));
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInWrongIndex() throws IOException {
        String input = "5. Venue: V1, Session Number: 1, Day: 2025-03-10, "
                + "Start: 08:30, Student Count: 0, Exams: 0\n";
        BufferedReader br = new BufferedReader(new StringReader(input));
        new Session(br, registry, 1);
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInEmptyInput() throws IOException {
        String input = "";
        BufferedReader br = new BufferedReader(new StringReader(input));
        new Session(br, registry, 1);
    }

    // equals/hashcode tests

    @Test
    public void testEqualsSameObject() {
        assertEquals(session, session);
    }

    @Test
    public void testEqualsMatchingSession() {
        Registry otherRegistry = new Registry();
        Room otherRoom = new Room("R1", otherRegistry);
        RoomList otherRoomList = new RoomList(otherRegistry);
        otherRoomList.add(otherRoom);
        Venue otherVenue = new Venue("V1", 1, otherRoomList, 5, 5, 25, false, otherRegistry);
        Session otherSession = new Session(otherVenue, 1,
                LocalDate.of(2025, 3, 10),
                LocalTime.of(8, 30),
                otherRegistry);
        assertEquals(session, otherSession);
    }

    @Test
    public void testEqualsDifferentVenue() {
        Registry otherRegistry = new Registry();
        Room otherRoom = new Room("R2", otherRegistry);
        RoomList otherRoomList = new RoomList(otherRegistry);
        otherRoomList.add(otherRoom);
        Venue otherVenue = new Venue("V2", 1, otherRoomList, 5, 5, 25, false, otherRegistry);
        Session otherSession = new Session(otherVenue, 1,
                LocalDate.of(2025, 3, 10),
                LocalTime.of(8, 30),
                otherRegistry);
        assertNotEquals(session, otherSession);
    }

    @Test
    public void testEqualsDifferentSessionNumber() {
        Session otherSession = new Session(venue, 2,
                LocalDate.of(2025, 3, 10),
                LocalTime.of(8, 30),
                new Registry());
        assertNotEquals(session, otherSession);
    }

    @Test
    public void testEqualsDifferentDate() {
        Session otherSession = new Session(venue, 1,
                LocalDate.of(2025, 3, 11),
                LocalTime.of(8, 30),
                new Registry());
        assertNotEquals(session, otherSession);
    }

    @Test
    public void testEqualsNull() {
        assertNotEquals(null, session);
    }

    @Test
    public void testEqualsDifferentClass() {
        assertNotEquals("not a session", session);
    }

    @Test
    public void testHashCodeEquality() {
        Registry otherRegistry = new Registry();
        Room otherRoom = new Room("R1", otherRegistry);
        RoomList otherRoomList = new RoomList(otherRegistry);
        otherRoomList.add(otherRoom);
        Venue otherVenue = new Venue("V1", 1, otherRoomList, 5, 5, 25, false, otherRegistry);
        Session otherSession = new Session(otherVenue, 1,
                LocalDate.of(2025, 3, 10),
                LocalTime.of(8, 30),
                otherRegistry);
        assertEquals(session.hashCode(), otherSession.hashCode());
    }

    @Test
    public void testHashCodeInequality() {
        Registry otherRegistry = new Registry();
        Room otherRoom = new Room("R2", otherRegistry);
        RoomList otherRoomList = new RoomList(otherRegistry);
        otherRoomList.add(otherRoom);
        Venue otherVenue = new Venue("V2", 1, otherRoomList, 3, 3, 9, true, otherRegistry);
        Session otherSession = new Session(otherVenue, 2,
                LocalDate.of(2025, 4, 15),
                LocalTime.of(12, 30),
                otherRegistry);
        assertNotEquals(session.hashCode(), otherSession.hashCode());
    }

    // complex stuffs regarding desk allocation algo

    // allocateStudents tests

    @Test
    public void testAllocateStudentsBasic() {
        Student student1 = new Student(9999111111L, "Dwiputra Sam", "Mulia",
                1, 1, 2007, "Blue", false, registry);
        student1.addSubject(subject1);

        Student student2 = new Student(9999222222L, "Adam", "Brown",
                2, 2, 2007, "Red", false, registry);
        student2.addSubject(subject1);

        session.scheduleExam(exam1);

        ExamList examList = new ExamList(registry);
        StudentList studentList = new StudentList(registry);
        studentList.add(student1);
        studentList.add(student2);

        session.allocateStudents(examList, studentList);

        // b comes before M alphabetically
        // first desk should be brown not sam
        assertEquals("Brown", session.getDesk(0, 0).deskFamilyName());
    }

    @Test
    public void testAllocateStudentsAlphabeticalOrder() {
        Student studentA = new Student(9999111111L, "Sam", "Asam",
                1, 1, 2007, "Blue", false, registry);
        studentA.addSubject(subject1);

        Student studentB = new Student(9999222222L, "Sam", "Clark",
                2, 2, 2007, "Red", false, registry);
        studentB.addSubject(subject1);

        Student studentC = new Student(9999333333L, "Sam", "Beni",
                3, 3, 2007, "Green", false, registry);
        studentC.addSubject(subject1);

        session.scheduleExam(exam1);

        ExamList examList = new ExamList(registry);
        StudentList studentList = new StudentList(registry);
        studentList.add(studentA);
        studentList.add(studentB);
        studentList.add(studentC);

        session.allocateStudents(examList, studentList);

        // Alphabetical: Asam, Beni, Clark
        assertEquals("Asam", session.getDesk(0, 0).deskFamilyName());
        assertEquals("Beni", session.getDesk(1, 0).deskFamilyName());
        assertEquals("Clark", session.getDesk(2, 0).deskFamilyName());
    }

    @Test
    public void testAllocateStudentsAaraNotIncluded() {
        // aara student not be placed in non-AARA venue
        Student aaraStudent = new Student(9999444444L, "Dwiputra", "Sam",
                1, 1, 2007, "Blue", true, registry);
        aaraStudent.addSubject(subject1);

        session.scheduleExam(exam1);

        ExamList examList = new ExamList(registry);
        StudentList studentList = new StudentList(registry);
        studentList.add(aaraStudent);

        session.allocateStudents(examList, studentList);

        // should be empty
        assertTrue(session.getDesk(0, 0).deskFamilyName().isEmpty());
    }

    @Test
    public void testAllocateStudentsWrongSubjectNotIncluded() {
        // Student taking a different subject should not be placed
        Student wrongSubject = new Student(9999555555L, "Dwiputra", "Sam",
                1, 1, 2007, "Blue", false, registry);
        wrongSubject.addSubject(subject2); // literature

        session.scheduleExam(exam1); // english exam

        ExamList examList = new ExamList(registry);
        StudentList studentList = new StudentList(registry);
        studentList.add(wrongSubject);

        session.allocateStudents(examList, studentList);

        assertTrue(session.getDesk(0, 0).deskFamilyName().isEmpty());
    }

    @Test
    public void testAllocateStudentsColumnByColumn() {
        // 5 rows and desk 6 should be on row col 0,1
        // mock student
        for (int i = 0; i < 6; i++) {
            Student student = new Student(9999100000L + i,
                    "Student" + (char) ('A' + i), "Name" + (char) ('A' + i),
                    1, 1, 2007, "Blue", false, registry);
            student.addSubject(subject1);
        }

        session.scheduleExam(exam1);

        ExamList examList = new ExamList(registry);
        StudentList studentList = new StudentList(registry);
        for (Student student : registry.getAll(Student.class)) {
            studentList.add(student);
        }

        session.allocateStudents(examList, studentList);

        // 5 row in the first col
        assertFalse(session.getDesk(0, 0).deskFamilyName().isEmpty());
        assertFalse(session.getDesk(4, 0).deskFamilyName().isEmpty());
        // next should be in second col
        assertFalse(session.getDesk(0, 1).deskFamilyName().isEmpty());
    }

    // multiple exams in same session tests

    @Test
    public void testAllocateStudentsMultipleExams() {
        // Two exams in the same session
        Student studentEnglish = new Student(9999111111L, "Dwiputra", "Mulia",
                1, 1, 2007, "Blue", false, registry);
        studentEnglish.addSubject(subject1); // English

        Student studentLit = new Student(9999222222L, "Sam", "Adams",
                2, 2, 2007, "Red", false, registry);
        studentLit.addSubject(subject2); // Literature

        session.scheduleExam(exam1); // English
        session.scheduleExam(exam2); // Literature

        ExamList examList = new ExamList(registry);
        StudentList studentList = new StudentList(registry);
        studentList.add(studentEnglish);
        studentList.add(studentLit);

        session.allocateStudents(examList, studentList);

        // Both students should be assigned somewhere
        boolean foundMulia = false;
        boolean foundAdams = false;
        for (int col = 0; col < venue.getColumns(); col++) {
            for (int row = 0; row < venue.getRows(); row++) {
                Desk desk = session.getDesk(row, col);
                if (desk != null) {
                    if ("Mulia".equals(desk.deskFamilyName())) {
                        foundMulia = true;
                    }
                    if ("Adams".equals(desk.deskFamilyName())) {
                        foundAdams = true;
                    }
                }
            }
        }
        assertTrue(foundMulia);
        assertTrue(foundAdams);
    }

    // clash detection test

    @Test
    public void testAllocateStudentsClashDetection() {
        // Student takes BOTH subjects that have exams in this session
        Student clashStudent = new Student(9999111111L, "Dwiputra", "Mulia",
                1, 1, 2007, "Blue", false, registry);
        clashStudent.addSubject(subject1); // English
        clashStudent.addSubject(subject2); // Literature

        session.scheduleExam(exam1); // English
        session.scheduleExam(exam2); // Literature

        ExamList examList = new ExamList(registry);
        StudentList studentList = new StudentList(registry);
        studentList.add(clashStudent);

        // Should not throw, just print a warning
        session.allocateStudents(examList, studentList);

        // Student should still be allocated (clash is flagged, not prevented)
        boolean foundStudent = false;
        for (int col = 0; col < venue.getColumns(); col++) {
            for (int row = 0; row < venue.getRows(); row++) {
                Desk desk = session.getDesk(row, col);
                if (desk != null && "Mulia".equals(desk.deskFamilyName())) {
                    foundStudent = true;
                }
            }
        }
        assertTrue(foundStudent);
    }

    // spacing tests

    @Test
    public void testAllocateStudentsSpacingWhenFewStudents() {
        // 2 students in 25 desks (should have gaps between them)
        Student student1 = new Student(9999111111L, "Alice", "Adams",
                1, 1, 2007, "Blue", false, registry);
        student1.addSubject(subject1);

        Student student2 = new Student(9999222222L, "Bob", "Baker",
                2, 2, 2007, "Red", false, registry);
        student2.addSubject(subject1);

        session.scheduleExam(exam1);

        ExamList examList = new ExamList(registry);
        StudentList studentList = new StudentList(registry);
        studentList.add(student1);
        studentList.add(student2);

        session.allocateStudents(examList, studentList);

        // find their desk numbers
        int desk1 = -1;
        int desk2 = -1;
        for (int col = 0; col < venue.getColumns(); col++) {
            for (int row = 0; row < venue.getRows(); row++) {
                Desk desk = session.getDesk(row, col);
                if (desk != null && "Adams".equals(desk.deskFamilyName())) {
                    desk1 = desk.deskNumber();
                }
                if (desk != null && "Baker".equals(desk.deskFamilyName())) {
                    desk2 = desk.deskNumber();
                }
            }
        }
        assertTrue(desk1 > 0);
        assertTrue(desk2 > 0);
        // students should have gaps (more than 1)
        assertTrue((desk2 - desk1) > 1);
    }

    @Test
    public void testAllocateStudentsMultipleExamsSeparated() {
        // students from different exams should be separated
        Student studentEnglish = new Student(9999111111L, "Alice", "Adams",
                1, 1, 2007, "Blue", false, registry);
        studentEnglish.addSubject(subject1);

        Student studentLit = new Student(9999222222L, "Bob", "Baker",
                2, 2, 2007, "Red", false, registry);
        studentLit.addSubject(subject2);

        session.scheduleExam(exam1);
        session.scheduleExam(exam2);

        ExamList examList = new ExamList(registry);
        StudentList studentList = new StudentList(registry);
        studentList.add(studentEnglish);
        studentList.add(studentLit);

        session.allocateStudents(examList, studentList);

        // find student cols
        int col1 = -1;
        int col2 = -1;
        for (int col = 0; col < venue.getColumns(); col++) {
            for (int row = 0; row < venue.getRows(); row++) {
                Desk desk = session.getDesk(row, col);
                if (desk != null && "Adams".equals(desk.deskFamilyName())) {
                    col1 = col;
                }
                if (desk != null && "Baker".equals(desk.deskFamilyName())) {
                    col2 = col;
                }
            }
        }
        assertTrue(col1 >= 0);
        assertTrue(col2 >= 0);
        // students from different exams should be in different columns (if many desks)
        assertNotEquals(col1, col2);
    }
}