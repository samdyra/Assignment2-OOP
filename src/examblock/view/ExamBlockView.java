package examblock.view;

import examblock.model.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * the V in MVC model. Drives and owns the UI
 */
public class ExamBlockView implements ModelObserver {

    /**
     * Document listener implementation
     */
    public abstract static class SimpleDocumentListener implements DocumentListener {

        /**
         * called when data changes
         *
         * @param e the particular event that happened
         */
        public abstract void update(DocumentEvent e);

        @Override
        public void insertUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update(e);
        }
    }

    /** The top level window frame. */
    private JFrame frame;

    /** Reference to the model in MVC. */
    private ExamBlockModel model;

    /** The global registry for all items. */
    private final Registry registry;

    /** The exam table in the top left panel. */
    private JTable examTable;

    /** The table model for the exam table. */
    private DefaultTableModel examTableModel;

    /** The session/venue tree in the top middle panel. */
    private JTree tree;

    /** The root node of the session tree. */
    private DefaultMutableTreeNode sessionRoot;

    /** The venue root node under "Create a new session". */
    private DefaultMutableTreeNode venueRoot;

    /** The Finalise button in the Go panel. */
    private JButton finaliseButton;

    /** The Add button in the Go panel. */
    private JButton addButton;

    /** The Clear button in the Go panel. */
    private JButton clearButton;

    /** The tabbed pane in the bottom panel. */
    private JTabbedPane tabbedPane;

    /** Mapping from exam table row index to Exam object. */
    private Map<Integer, Exam> examMap;

    /** Mapping from tree session node to Session object. */
    private Map<DefaultMutableTreeNode, Session> sessionNodeMap;

    /** Mapping from tree venue node to Venue object. */
    private Map<DefaultMutableTreeNode, Venue> venueNodeMap;

    /** Mapping from tree exam node to Exam object. */
    private Map<DefaultMutableTreeNode, Exam> examNodeMap;

    /** Whether changes have been made since last finalise. */
    private boolean dirty;

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
        this.dirty = false;

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
                new String[]{"Int.", "Subject", "Date", "Time", "AARA",
                        "Non."}, 0) {
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

        // ,iddle: session tree with label
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

        // right: buttons with label
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

        // assemble top panel
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

    /**
     * Update button enabled states based on current selections.
     */
    private void updateButtonStates() {
        boolean examSelected = examTable.getSelectedRow() >= 0;
        boolean venueSelected = tree.getSelectionPath() != null;

        clearButton.setEnabled(examSelected);
        addButton.setEnabled(examSelected && venueSelected);
        finaliseButton.setEnabled(dirty);
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
                for (Subject studentSubject
                        : student.getSubjects().all()) {
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
            DefaultMutableTreeNode existingNode =
                    new DefaultMutableTreeNode(
                            "Existing sessions (" + sessions.size() + ")");

            for (Session session : sessions.all()) {
                // Session node: date, time, venue
                DefaultMutableTreeNode sessionNode =
                        new DefaultMutableTreeNode(
                                session.getDate() + " at "
                                        + session.getTime()
                                        + " in "
                                        + session.getVenue().venueId());
                addSessionToSessionNodeMap(sessionNode, session);

                // Exams group
                DefaultMutableTreeNode examsNode =
                        new DefaultMutableTreeNode(
                                "Exams (" + session.getExams().size()
                                        + ")");

                for (Exam exam : session.getExams()) {
                    // Count students for this exam in this venue
                    int studentCount = 0;
                    for (Student student : model.getStudents().all()) {
                        if (student.isAara()
                                == session.getVenue().isAara()) {
                            for (Subject studentSubject
                                    : student.getSubjects().all()) {
                                if (studentSubject.equals(
                                        exam.getSubject())) {
                                    studentCount++;
                                    break;
                                }
                            }
                        }
                    }

                    DefaultMutableTreeNode examNode =
                            new DefaultMutableTreeNode(
                                    exam.abbrevShortTitle() + " ("
                                            + studentCount
                                            + " students)");
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
            DefaultMutableTreeNode venueNode =
                    new DefaultMutableTreeNode(
                            venue.venueId() + " (" + venue.deskCount()
                                    + (venue.isAara()
                                    ? " AARA" : " Non-AARA")
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
        JTable table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        autoSizeColumns(table);
        tabbedPane.setComponentAt(0, new JScrollPane(table));
    }


    /**
     * new data for this page of the tabbed view
     *
     * @param exams new List of data
     */
    public void updateExamPage(ExamList exams) {
        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"Subject", "Type", "Paper", "Subtitle", "Unit", "Date", "Time"}, 0);
        for (Exam exam : exams.all()) {
            tableModel.addRow(exam.toLongTableRow());
        }
        JTable table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        autoSizeColumns(table);
        tabbedPane.setComponentAt(1, new JScrollPane(table));
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
        JTable table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        autoSizeColumns(table);
        tabbedPane.setComponentAt(2, new JScrollPane(table));
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
        JTable table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        autoSizeColumns(table);
        tabbedPane.setComponentAt(3, new JScrollPane(table));
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
        JTable table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        autoSizeColumns(table);
        tabbedPane.setComponentAt(4, new JScrollPane(table));
    }


    /**
     * new data for this page of the tabbed view
     *
     * @param venues new List of data
     */
    public void updateVenuPage(VenueList venues) {
        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"Venue", "Rooms", "Rows", "Columns", "Desks", "AARA"}, 0);
        for (Venue venue : venues.all()) {
            StringBuilder roomNames = new StringBuilder();
            boolean firstRoom = true;
            for (Room room : venue.getRooms()) {
                if (!firstRoom) {
                    roomNames.append(" ");
                }
                roomNames.append(room.roomId());
                firstRoom = false;
            }
            tableModel.addRow(new Object[]{
                    venue.venueId(),
                    roomNames.toString(),
                    venue.getRows(),
                    venue.getColumns(),
                    venue.deskCount(),
                    venue.isAara() ? "✔" : ""
            });
        }
        JTable table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        autoSizeColumns(table);
        tabbedPane.setComponentAt(5, new JScrollPane(table));
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
                dirty = false;
                frame.setTitle("Exam Block Manager - "
                        + model.getTitle()
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
            case "scheduled":
                dirty = true;
                updateTree(model.getSessions(), model.getVenues());
                updateExamTable(model.getExams());
                break;
            case "finalised":
                dirty = false;
                updateTree(model.getSessions(), model.getVenues());
                break;
            default:
                break;
        }
        updateButtonStates();
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

    /**
     * Add a listener for the Finalise button
     *
     * @param listener listener
     */
    public void addFinaliseButtonListener(ActionListener listener) {
        finaliseButton.addActionListener(listener);
    }

    /**
     * Add a listener for the Add button
     *
     * @param listener listener
     */
    public void addAddButtonListener(ActionListener listener) {
        addButton.addActionListener(listener);
    }

    /**
     * Add a listener for the Clear button
     *
     * @param listener listener
     */
    public void addClearButtonListener(ActionListener listener) {
        clearButton.addActionListener(listener);
    }

    // ==================== Getters ====================

    /**
     * get the top level window
     *
     * @return the frame handle
     */
    public JFrame getFrame() {
        return frame;
    }

    /**
     * get the exam table
     *
     * @return the exam table
     */
    public JTable getExamTable() {
        return examTable;
    }

    /**
     * get the session tree
     *
     * @return the session tree
     */
    public JTree getTree() {
        return tree;
    }

    /**
     * get the notebook like tabbed window
     *
     * @return the window
     */
    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    /**
     * the actual button
     *
     * @return the actual button
     */
    public JButton getFinaliseButton() {
        return finaliseButton;
    }

    /**
     * the actual button
     *
     * @return the actual button
     */
    public JButton getAddButton() {
        return addButton;
    }

    /**
     * the actual button
     *
     * @return the actual button
     */
    public JButton getClearButton() {
        return clearButton;
    }

    /**
     * return the Session root node
     *
     * @return the Session root node
     */
    public DefaultMutableTreeNode getSessionRoot() {
        return sessionRoot;
    }

    /**
     * return the Venue root node
     *
     * @return the Venue root node
     */
    public DefaultMutableTreeNode getVenueRoot() {
        return venueRoot;
    }

    /**
     * return the table model
     *
     * @return the table model
     */
    public DefaultTableModel getExamTableModel() {
        return examTableModel;
    }

    /**
     * set the new title
     *
     * @param title title
     */
    public void setTitle(String title) {
        frame.setTitle("Exam Block Manager - " + title);
    }

    /**
     * set the version to something new
     *
     * @param version new version
     */
    public void setVersion(double version) {
        // Update title bar with new version
    }

    /**
     * return the selected exam rows. Since this is a single-select
     * list ctrl, this is redundant
     *
     * @return the array of one object, or null is failure
     */
    public int[] getSelectedExamRows() {
        int row = examTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return new int[]{row};
    }

    /**
     * get the node from the session tree
     *
     * @return the node
     */
    public DefaultMutableTreeNode getSelectedTreeNode() {
        if (tree.getSelectionPath() == null) {
            return null;
        }
        return (DefaultMutableTreeNode) tree.getSelectionPath()
                .getLastPathComponent();
    }

    /**
     * clear the selection of any control, as well as some cached values
     */
    public void removeAllSelections() {
        examTable.clearSelection();
        tree.clearSelection();
        examMap.clear();
        updateButtonStates();
    }

    /**
     * Checks if there are any un-finalized sessions in the JTree.
     * A session is un-finalized if any Exam node has no Desk children.
     *
     * @return true if un-finalized sessions exist, false otherwise
     */
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

    /**
     * mapping from list to item represented
     *
     * @param index in the list
     * @param exam  what to place there
     */
    public void addExamToExamMap(int index, Exam exam) {
        examMap.put(index, exam);
    }

    /**
     * get Exam stored for index index
     *
     * @param index index
     * @return item
     */
    public Exam getExamFromExamMap(int index) {
        return examMap.get(index);
    }

    /**
     * Mapping from list item to the item represented
     *
     * @param examNode the object being requested
     * @return the item or null
     */
    public Exam getExamFromExamNodeMap(DefaultMutableTreeNode examNode) {
        return examNodeMap.get(examNode);
    }

    /**
     * Add Session object to the SessionNode map
     *
     * @param sessionNode tree node
     * @param session     Session object
     */
    public void addSessionToSessionNodeMap(
            DefaultMutableTreeNode sessionNode, Session session) {
        sessionNodeMap.put(sessionNode, session);
    }

    /**
     * return Session object from the SessionNode map
     *
     * @param sessionNode tree node
     * @return session Session object
     */
    public Session getSessionFromSessionNodeMap(
            DefaultMutableTreeNode sessionNode) {
        return sessionNodeMap.get(sessionNode);
    }

    /**
     * Add Venue object to the VenueNode map
     *
     * @param venueNode tree node
     * @param venue     Venue object
     */
    public void addVenueToVenueNodeMap(
            DefaultMutableTreeNode venueNode, Venue venue) {
        venueNodeMap.put(venueNode, venue);
    }

    /**
     * Get Venue object from the VenueNode map
     *
     * @param venueNode tree node
     * @return venue Venue object
     */
    public Venue getVenueFromVenueNodeMap(
            DefaultMutableTreeNode venueNode) {
        return venueNodeMap.get(venueNode);
    }

    /**
     * Auto-size table columns to fit their content.
     *
     * @param table the table to resize
     */
    private void autoSizeColumns(JTable table) {
        for (int col = 0; col < table.getColumnCount(); col++) {
            int maxWidth = 0;
            // Check header width
            String headerValue = table.getColumnName(col);
            maxWidth = table.getFontMetrics(table.getFont())
                    .stringWidth(headerValue) + 20;
            // Check data width
            for (int row = 0; row < table.getRowCount(); row++) {
                Object value = table.getValueAt(row, col);
                if (value != null) {
                    int width = table.getFontMetrics(table.getFont())
                            .stringWidth(value.toString()) + 20;
                    maxWidth = Math.max(maxWidth, width);
                }
            }
            table.getColumnModel().getColumn(col).setPreferredWidth(maxWidth);
        }
    }
}