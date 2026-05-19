package examblock.view;

import examblock.model.*;
import examblock.view.components.ListboxAlternatingRowRenderer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * the V in MVC model. Drives and owns the UI
 */
public class ExamBlockView implements ModelObserver {

    /**
     * Document listener implementation
     */
    public abstract static class SimpleDocumentListener implements DocumentListener {
        /** called when data changes */
        public abstract void update(DocumentEvent e);

        @Override
        public void insertUpdate(DocumentEvent e) { update(e); }

        @Override
        public void removeUpdate(DocumentEvent e) { update(e); }

        @Override
        public void changedUpdate(DocumentEvent e) { update(e); }
    }

    private JFrame frame;
    private ExamBlockModel model;
    private Registry registry;

    // Top left: exam table
    private JTable examTable;
    private DefaultTableModel examTableModel;

    // Top middle: session tree
    private JTree tree;
    private DefaultMutableTreeNode sessionRoot;
    private DefaultMutableTreeNode venueRoot;

    // Top right: buttons
    private JButton finaliseButton;
    private JButton addButton;
    private JButton clearButton;

    // Bottom: tabbed pane
    private JTabbedPane tabbedPane;

    // Mappings
    private Map<Integer, Exam> examMap;
    private Map<DefaultMutableTreeNode, Session> sessionNodeMap;
    private Map<DefaultMutableTreeNode, Venue> venueNodeMap;
    private Map<DefaultMutableTreeNode, Exam> examNodeMap;

    /**
     * Constructor
     *
     * @param registry registry
     */
    public ExamBlockView(Registry registry) {
        this.registry = registry;
        this.examMap = new HashMap<>();
        this.sessionNodeMap = new HashMap<>();
        this.venueNodeMap = new HashMap<>();
        this.examNodeMap = new HashMap<>();

        frame = new JFrame("Exam Block Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLayout(new BorderLayout());

        frame.add(createTopPanel(), BorderLayout.CENTER);
        frame.add(createBottomPanel(), BorderLayout.SOUTH);
    }

    /**
     * create the top panel
     *
     * @return the panel object
     */
    public JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());

        // Left: exam table with label
        JPanel examPanel = new JPanel(new BorderLayout());
        JLabel examLabel = new JLabel("1. Select an Exam / Unit",
                SwingConstants.CENTER);
        examPanel.add(examLabel, BorderLayout.NORTH);

        examTableModel = new DefaultTableModel(
                new String[]{"Int.", "Subject", "Date", "Time", "AARA", "Non."}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        examTable = new JTable(examTableModel);
        examTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        examTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });

        JScrollPane examScroll = new JScrollPane(examTable);
        examPanel.add(examScroll, BorderLayout.CENTER);

        // Middle: session tree with label
        JPanel sessionPanel = new JPanel(new BorderLayout());
        JLabel sessionLabel = new JLabel("2. Select a Session / Venue",
                SwingConstants.CENTER);
        sessionPanel.add(sessionLabel, BorderLayout.NORTH);

        sessionRoot = new DefaultMutableTreeNode("Sessions");
        venueRoot = new DefaultMutableTreeNode("Create a new session");
        sessionRoot.add(venueRoot);
        tree = new JTree(sessionRoot);
        tree.addTreeSelectionListener(e -> updateButtonStates());

        JScrollPane treeScroll = new JScrollPane(tree);
        sessionPanel.add(treeScroll, BorderLayout.CENTER);

        // Right: buttons with label
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JLabel goLabel = new JLabel("3. Go");
        goLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(goLabel);
        buttonPanel.add(Box.createVerticalStrut(20));

        finaliseButton = new JButton("Finali...");
        finaliseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        finaliseButton.setEnabled(false);
        buttonPanel.add(finaliseButton);
        buttonPanel.add(Box.createVerticalStrut(40));

        addButton = new JButton("Add");
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.setEnabled(false);
        buttonPanel.add(addButton);
        buttonPanel.add(Box.createVerticalStrut(40));

        clearButton = new JButton("Clear");
        clearButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearButton.setEnabled(false);
        buttonPanel.add(clearButton);

        // Assemble top panel
        JSplitPane leftSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                examPanel, sessionPanel);
        leftSplit.setDividerLocation(600);

        topPanel.add(leftSplit, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        return topPanel;
    }

    /**
     * create the bottom panel
     *
     * @return the panel
     */
    public JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        tabbedPane = new JTabbedPane();

        // Create empty tabs
        tabbedPane.addTab("Subjects", new JScrollPane(new JTable()));
        tabbedPane.addTab("Exams", new JScrollPane(new JTable()));
        tabbedPane.addTab("Units", new JScrollPane(new JTable()));
        tabbedPane.addTab("Students", new JScrollPane(new JTable()));
        tabbedPane.addTab("Rooms", new JScrollPane(new JTable()));
        tabbedPane.addTab("Venues", new JScrollPane(new JTable()));

        bottomPanel.add(tabbedPane, BorderLayout.CENTER);
        bottomPanel.setPreferredSize(new Dimension(1200, 300));
        return bottomPanel;
    }

    /**
     * Show the window after construction
     */
    public void display() {
        frame.setVisible(true);
    }

    private void updateButtonStates() {
        boolean examSelected = examTable.getSelectedRow() >= 0;
        boolean venueSelected = tree.getSelectionPath() != null;

        clearButton.setEnabled(examSelected);
        addButton.setEnabled(examSelected && venueSelected);
        finaliseButton.setEnabled(hasUnfinalisedSessions());
    }

    // ==================== Update methods ====================

    /**
     * remove old content and load new content
     *
     * @param exams new list of exams
     */
    public void updateExamTable(ExamList exams) {
        examTableModel.setRowCount(0);
        examMap.clear();
        int index = 0;
        for (Exam exam : exams.all()) {
            Subject examSubject = exam.getSubject();
            int aaraCount = 0;
            int nonAaraCount = 0;

            // Use model's student list, not stale registry
            for (Student student : model.getStudents().all()) {
                for (Subject studentSubject : student.getSubjects().all()) {
                    if (studentSubject.equals(examSubject)) {
                        if (student.isAara()) {
                            aaraCount++;
                        } else {
                            nonAaraCount++;
                        }
                        break;
                    }
                }
            }

            Object[] row = exam.toTableRow();
            examTableModel.addRow(new Object[]{
                    row[0], row[1], row[2], row[3], aaraCount, nonAaraCount
            });
            addExamToExamMap(index++, exam);
        }
    }

    /**
     * update the session tree
     *
     * @param sessions sessions
     * @param venues   venues
     */
    public void updateTree(SessionList sessions, VenueList venues) {
        sessionRoot.removeAllChildren();
        venueNodeMap.clear();
        sessionNodeMap.clear();

        // Existing sessions group
        if (sessions.size() > 0) {
            DefaultMutableTreeNode existingNode = new DefaultMutableTreeNode(
                    "Existing sessions (" + sessions.size() + ")");

            for (Session session : sessions.all()) {
                // Session node: date, time, venue
                DefaultMutableTreeNode sessionNode = new DefaultMutableTreeNode(
                        session.getDate() + " at " + session.getTime()
                                + " in " + session.getVenue().venueId());
                addSessionToSessionNodeMap(sessionNode, session);

                // Exams group
                DefaultMutableTreeNode examsNode = new DefaultMutableTreeNode(
                        "Exams (" + session.getExams().size() + ")");

                for (Exam exam : session.getExams()) {
                    // Count students for this exam in this venue
                    int studentCount = 0;
                    for (Student student : model.getStudents().all()) {
                        if (student.isAara() == session.getVenue().isAara()) {
                            for (Subject studentSubject : student.getSubjects().all()) {
                                if (studentSubject.equals(exam.getSubject())) {
                                    studentCount++;
                                    break;
                                }
                            }
                        }
                    }

                    DefaultMutableTreeNode examNode = new DefaultMutableTreeNode(
                            exam.abbrevShortTitle() + " (" + studentCount + " students)");
                    examsNode.add(examNode);
                }

                sessionNode.add(examsNode);
                existingNode.add(sessionNode);
            }
            sessionRoot.add(existingNode);
        }

        // Create a new session with venues
        venueRoot = new DefaultMutableTreeNode("Create a new session");
        for (Venue venue : venues.all()) {
            DefaultMutableTreeNode venueNode = new DefaultMutableTreeNode(
                    venue.venueId() + " (" + venue.deskCount()
                            + (venue.isAara() ? " AARA" : " Non-AARA")
                            + " desks)");
            addVenueToVenueNodeMap(venueNode, venue);
            venueRoot.add(venueNode);
        }
        sessionRoot.add(venueRoot);

        ((DefaultTreeModel) tree.getModel()).reload();
        // Expand all nodes
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    /**
     * new data for the Subjects page of the tabbed view
     *
     * @param subjects new SubjectList
     */
    public void updateSubjectPage(SubjectList subjects) {
        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"Title", "Description"}, 0);
        for (Subject subject : subjects.all()) {
            tableModel.addRow(new Object[]{subject.getTitle(),
                    subject.getDescription()});
        }
        tabbedPane.setComponentAt(0, new JScrollPane(new JTable(tableModel)));
    }

    /**
     * new data for this page of the tabbed view
     *
     * @param exams new List of data
     */
    public void updateExamPage(ExamList exams) {
        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"Subject", "Type", "Paper", "Subtitle",
                        "Unit", "Date", "Time"}, 0);
        for (Exam exam : exams.all()) {
            tableModel.addRow(exam.toLongTableRow());
        }
        tabbedPane.setComponentAt(1, new JScrollPane(new JTable(tableModel)));
    }

    /**
     * new data for this page of the tabbed view
     *
     * @param units new List of data
     */
    public void updateUnitPage(UnitList units) {
        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"Subject", "UnitID", "Title", "Description"}, 0);
        for (Unit unit : units.all()) {
            tableModel.addRow(unit.toTableRow());
        }
        tabbedPane.setComponentAt(2, new JScrollPane(new JTable(tableModel)));
    }

    /**
     * new data for this page of the tabbed view
     *
     * @param students new List of data
     */
    public void updateStudentPage(StudentList students) {
        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"LUI", "Full Name", "AARA", "Date of Birth", "Subjects"}, 0);
        for (Student student : students.all()) {
            tableModel.addRow(student.toTableRow());
        }
        tabbedPane.setComponentAt(3, new JScrollPane(new JTable(tableModel)));
    }

    /**
     * new data for this page of the tabbed view
     *
     * @param rooms new List of data
     */
    public void updateRoomPage(RoomList rooms) {
        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"Room ID"}, 0);
        for (Room room : rooms.all()) {
            tableModel.addRow(room.toTableRow());
        }
        tabbedPane.setComponentAt(4, new JScrollPane(new JTable(tableModel)));
    }

    /**
     * new data for this page of the tabbed view
     *
     * @param venues new List of data
     */
    public void updateVenuPage(VenueList venues) {
        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"Venue ID", "Desks", "AARA"}, 0);
        for (Venue venue : venues.all()) {
            tableModel.addRow(venue.toTableRow());
        }
        tabbedPane.setComponentAt(5, new JScrollPane(new JTable(tableModel)));
    }

    /**
     * here we receive the notifications that model sent us
     *
     * @param property whatever it is
     */
    @Override
    public void modelChanged(String property) {
        if (model == null) {
            return;
        }
        switch (property) {
            case "loaded":
                frame.setTitle("Exam Block Manager - " + model.getTitle()
                        + " (v" + model.getVersion() + ")");
                updateExamTable(model.getExams());
                updateSubjectPage(model.getSubjects());
                updateExamPage(model.getExams());
                updateUnitPage(model.getUnits());
                updateStudentPage(model.getStudents());
                updateRoomPage(model.getRooms());
                updateVenuPage(model.getVenues());
                updateTree(model.getSessions(), model.getVenues());
                break;
            case "title":
                frame.setTitle("Exam Block Manager - " + model.getTitle()
                        + " (v" + model.getVersion() + ")");
                break;
            case "finalised":
                updateTree(model.getSessions(), model.getVenues());
                break;
            default:
                break;
        }
    }

    /**
     * model initialisation
     *
     * @param model reference to the model
     */
    public void setModel(ExamBlockModel model) {
        this.model = model;
    }

    // ==================== Listener methods ====================

    public void addFinaliseButtonListener(ActionListener listener) {
        finaliseButton.addActionListener(listener);
    }

    public void addAddButtonListener(ActionListener listener) {
        addButton.addActionListener(listener);
    }

    public void addClearButtonListener(ActionListener listener) {
        clearButton.addActionListener(listener);
    }

    // ==================== Getters ====================

    public JFrame getFrame() { return frame; }
    public JTable getExamTable() { return examTable; }
    public JTree getTree() { return tree; }
    public JTabbedPane getTabbedPane() { return tabbedPane; }
    public JButton getFinaliseButton() { return finaliseButton; }
    public JButton getAddButton() { return addButton; }
    public JButton getClearButton() { return clearButton; }
    public DefaultMutableTreeNode getSessionRoot() { return sessionRoot; }
    public DefaultMutableTreeNode getVenueRoot() { return venueRoot; }
    public DefaultTableModel getExamTableModel() { return examTableModel; }

    public void setTitle(String title) {
        frame.setTitle("Exam Block Manager - " + title);
    }

    public void setVersion(double version) {
        // Update title bar with new version
    }

    public int[] getSelectedExamRows() {
        int row = examTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return new int[]{row};
    }

    public DefaultMutableTreeNode getSelectedTreeNode() {
        if (tree.getSelectionPath() == null) {
            return null;
        }
        return (DefaultMutableTreeNode) tree.getSelectionPath()
                .getLastPathComponent();
    }

    public void removeAllSelections() {
        examTable.clearSelection();
        tree.clearSelection();
        examMap.clear();
        updateButtonStates();
    }

    public boolean hasUnfinalisedSessions() {
        for (int i = 0; i < sessionRoot.getChildCount(); i++) {
            DefaultMutableTreeNode child =
                    (DefaultMutableTreeNode) sessionRoot.getChildAt(i);
            if (child == venueRoot) {
                continue;
            }
            // Check if any exam node has no desk children
            for (int j = 0; j < child.getChildCount(); j++) {
                DefaultMutableTreeNode examNode =
                        (DefaultMutableTreeNode) child.getChildAt(j);
                if (examNode.getChildCount() == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    // ==================== Map methods ====================

    public void addExamToExamMap(int index, Exam exam) {
        examMap.put(index, exam);
    }

    public Exam getExamFromExamMap(int index) {
        return examMap.get(index);
    }

    public Exam getExamFromExamNodeMap(DefaultMutableTreeNode examNode) {
        return examNodeMap.get(examNode);
    }

    public void addSessionToSessionNodeMap(DefaultMutableTreeNode sessionNode,
                                           Session session) {
        sessionNodeMap.put(sessionNode, session);
    }

    public Session getSessionFromSessionNodeMap(
            DefaultMutableTreeNode sessionNode) {
        return sessionNodeMap.get(sessionNode);
    }

    public void addVenueToVenueNodeMap(DefaultMutableTreeNode venueNode,
                                       Venue venue) {
        venueNodeMap.put(venueNode, venue);
    }

    public Venue getVenueFromVenueNodeMap(DefaultMutableTreeNode venueNode) {
        return venueNodeMap.get(venueNode);
    }
}