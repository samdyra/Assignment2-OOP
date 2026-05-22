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

    // normal flow

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

        // B comes before M alphabetically
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

        // ordered Asam, Beni, Clark
        assertEquals("Asam", session.getDesk(0, 0).deskFamilyName());
        assertEquals("Beni", session.getDesk(1, 0).deskFamilyName());
        assertEquals("Clark", session.getDesk(2, 0).deskFamilyName());
    }

    // aara not included

    @Test
    public void testAllocateStudentsAaraNotIncluded() {
        Student aaraStudent = new Student(9999444444L, "Dwiputra", "Sam",
                1, 1, 2007, "Blue", true, registry);
        aaraStudent.addSubject(subject1);

        session.scheduleExam(exam1);

        ExamList examList = new ExamList(registry);
        StudentList studentList = new StudentList(registry);
        studentList.add(aaraStudent);

        session.allocateStudents(examList, studentList);

        assertTrue(session.getDesk(0, 0).deskFamilyName().isEmpty());
    }

    // wrong subject
    @Test
    public void testAllocateStudentsWrongSubjectNotIncluded() {
        Student wrongSubject = new Student(9999555555L, "Dwiputra", "Sam",
                1, 1, 2007, "Blue", false, registry);
        wrongSubject.addSubject(subject2);

        session.scheduleExam(exam1);

        ExamList examList = new ExamList(registry);
        StudentList studentList = new StudentList(registry);
        studentList.add(wrongSubject);

        session.allocateStudents(examList, studentList);

        assertTrue(session.getDesk(0, 0).deskFamilyName().isEmpty());
    }

    // col by col filling logic test
    @Test
    public void testAllocateStudentsColumnByColumn() {
        // 20 students fill 4 columns tightly in 5x5 venue (no spacing)
        for (int i = 0; i < 20; i++) {
            Student student = new Student(9999100000L + i,
                    "Student" + (char) ('A' + i), "Name" + String.format("%02d", i),
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

        // 4 columns fully packed (20 students / 5 rows = 4 columns)
        // With only 1 empty column, no equal spacing possible (1/3 gaps)
        // So students should be in consecutive columns
        assertFalse(session.getDesk(0, 0).deskFamilyName().isEmpty());
        assertFalse(session.getDesk(4, 0).deskFamilyName().isEmpty());
        assertFalse(session.getDesk(0, 1).deskFamilyName().isEmpty());
        assertFalse(session.getDesk(0, 2).deskFamilyName().isEmpty());
        assertFalse(session.getDesk(0, 3).deskFamilyName().isEmpty());
    }

    // multiple exam
    @Test
    public void testAllocateStudentsMultipleExams() {
        Student studentEnglish = new Student(9999111111L, "Dwiputra", "Mulia",
                1, 1, 2007, "Blue", false, registry);
        studentEnglish.addSubject(subject1);

        Student studentLit = new Student(9999222222L, "Sam", "Adams",
                2, 2, 2007, "Red", false, registry);
        studentLit.addSubject(subject2);

        session.scheduleExam(exam1);
        session.scheduleExam(exam2);

        ExamList examList = new ExamList(registry);
        StudentList studentList = new StudentList(registry);
        studentList.add(studentEnglish);
        studentList.add(studentLit);

        session.allocateStudents(examList, studentList);

        boolean foundMulia = false;
        boolean foundAdams = false;
        for (int col = 0; col < venue.getColumns(); col++) {
            for (int row = 0; row < venue.getRows(); row++) {
                Desk desk = session.getDesk(row, col);
                if ("Mulia".equals(desk.deskFamilyName())) {
                    foundMulia = true;
                }
                if ("Adams".equals(desk.deskFamilyName())) {
                    foundAdams = true;
                }
            }
        }
        assertTrue(foundMulia);
        assertTrue(foundAdams);
    }

    // clash detection test
    @Test
    public void testAllocateStudentsClashDetection() {
        Student clashStudent = new Student(9999111111L, "Dwiputra", "Mulia",
                1, 1, 2007, "Blue", false, registry);
        clashStudent.addSubject(subject1);
        clashStudent.addSubject(subject2);

        session.scheduleExam(exam1);
        session.scheduleExam(exam2);

        ExamList examList = new ExamList(registry);
        StudentList studentList = new StudentList(registry);
        studentList.add(clashStudent);

        session.allocateStudents(examList, studentList);

        boolean foundStudent = false;
        for (int col = 0; col < venue.getColumns(); col++) {
            for (int row = 0; row < venue.getRows(); row++) {
                Desk desk = session.getDesk(row, col);
                if ("Mulia".equals(desk.deskFamilyName())) {
                    foundStudent = true;
                }
            }
        }
        assertTrue(foundStudent);
    }



    // Individual Assignment 2 student-to-desk allocation clarifications (announcement from BB)
    @Test
    public void testAllocateStudentsSpacingWhenFewStudents() {
        // single exam, 6 student in 5x5 venu
        // 2 student columns 3 empty cols, 1 gap -> gap of 3
        // Expected to student cols at 0 and 4
        for (int i = 0; i < 6; i++) {
            Student student = new Student(9999100000L + i,
                    "Student", "Name" + String.format("%02d", i),
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

        // Find which columns have students
        int firstStudentCol = -1;
        int lastStudentCol = -1;
        for (int col = 0; col < 5; col++) {
            for (int row = 0; row < 5; row++) {
                if (!session.getDesk(row, col).deskFamilyName().isEmpty()) {
                    if (firstStudentCol == -1) {
                        firstStudentCol = col;
                    }
                    lastStudentCol = col;
                }
            }
        }
        // gap between student columns should be at least 3
        assertTrue(lastStudentCol - firstStudentCol >= 3);
    }

    @Test
    public void testAllocateNoWastedColumnsAtEnd() {
        // 10 students in 5x5 venue, 2 student columns needed
        // should spread  (not grouped at the start)
        for (int i = 0; i < 10; i++) {
            Student student = new Student(9999100000L + i,
                    // %02d leading zeros to 2 digits 00 01 etc
                    "Student", "Name" + String.format("%02d", i),
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

        // students should be spread beyond just columns 0 and 1
        boolean hasStudentBeyondCol1 = false;
        for (int col = 2; col < 5; col++) {
            for (int row = 0; row < 5; row++) {
                if (!session.getDesk(row, col).deskFamilyName().isEmpty()) {
                    hasStudentBeyondCol1 = true;
                }
            }
        }
        assertTrue(hasStudentBeyondCol1);
    }

    // mult exam with diff students should be separated (if possible)
    @Test
    public void testAllocateStudentsMultipleExamsSeparated() {
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

        int col1 = -1;
        int col2 = -1;
        for (int col = 0; col < venue.getColumns(); col++) {
            for (int row = 0; row < venue.getRows(); row++) {
                Desk desk = session.getDesk(row, col);
                if ("Adams".equals(desk.deskFamilyName())) {
                    col1 = col;
                }
                if ("Baker".equals(desk.deskFamilyName())) {
                    col2 = col;
                }
            }
        }
        assertTrue(col1 >= 0);
        assertTrue(col2 >= 0);
        assertNotEquals(col1, col2);
    }

    @Test
    public void testAllocateInterExamGapsPriority() {
        // 2 exams, 1 student each, 5 columns
        // 2 student cols, 3 empty → all 3 go to inter-exam gap
        Student s1 = new Student(9999111111L, "Alice", "Adams",
                1, 1, 2007, "Blue", false, registry);
        s1.addSubject(subject1);

        Student s2 = new Student(9999222222L, "Bob", "Baker",
                2, 2, 2007, "Red", false, registry);
        s2.addSubject(subject2);

        session.scheduleExam(exam1);
        session.scheduleExam(exam2);

        ExamList examList = new ExamList(registry);
        StudentList studentList = new StudentList(registry);
        studentList.add(s1);
        studentList.add(s2);

        session.allocateStudents(examList, studentList);

        int colAdams = -1;
        int colBaker = -1;
        for (int col = 0; col < 5; col++) {
            for (int row = 0; row < 5; row++) {
                Desk desk = session.getDesk(row, col);
                if ("Adams".equals(desk.deskFamilyName())) {
                    colAdams = col;
                }
                if ("Baker".equals(desk.deskFamilyName())) {
                    colBaker = col;
                }
            }
        }
        assertTrue(colAdams >= 0);
        assertTrue(colBaker >= 0);
        // All 3 empty columns should be inter-exam gap
        assertTrue(colBaker - colAdams >= 4);
    }

    // ##### Gaps between exam regions should not differ by more than one [empty desk].
    @Test
    public void testAllocateInterExamGapsDifferByAtMostOne() {
        // 3 exams, 1 student each, 5 columns
        // 3 student cols, 2 empty, 2 inter-gaps → each gap = 1
        // Expected: cols 0, 2, 4
        Subject subject3 = new Subject("Physics", "Physics desc.", registry);
        Exam exam3 = new Exam(subject3, Exam.ExamType.INTERNAL,
                10, 3, 2025, 8, 30, registry);

        Student s1 = new Student(9999111111L, "Alice", "Adams",
                1, 1, 2007, "Blue", false, registry);
        s1.addSubject(subject1);
        Student s2 = new Student(9999222222L, "Bob", "Baker",
                2, 2, 2007, "Red", false, registry);
        s2.addSubject(subject2);
        Student s3 = new Student(9999333333L, "Charlie", "Clark",
                3, 3, 2007, "Green", false, registry);
        s3.addSubject(subject3);

        session.scheduleExam(exam1);
        session.scheduleExam(exam2);
        session.scheduleExam(exam3);

        ExamList examList = new ExamList(registry);
        StudentList studentList = new StudentList(registry);
        studentList.add(s1);
        studentList.add(s2);
        studentList.add(s3);

        session.allocateStudents(examList, studentList);

        int col1 = -1;
        int col2 = -1;
        int col3 = -1;
        for (int col = 0; col < 5; col++) {
            for (int row = 0; row < 5; row++) {
                Desk desk = session.getDesk(row, col);
                if ("Adams".equals(desk.deskFamilyName())) {
                    col1 = col;
                }
                if ("Baker".equals(desk.deskFamilyName())) {
                    col2 = col;
                }
                if ("Clark".equals(desk.deskFamilyName())) {
                    col3 = col;
                }
            }
        }
        assertTrue(col1 >= 0);
        assertTrue(col2 >= 0);
        assertTrue(col3 >= 0);

        int gap1 = col2 - col1;
        int gap2 = col3 - col2;
        // Gaps must differ by at most 1
        assertTrue(Math.abs(gap1 - gap2) <= 1);
        // Each gap should be at least 1 (not packed together)
        assertTrue(gap1 >= 2);
        assertTrue(gap2 >= 2);
    }
}