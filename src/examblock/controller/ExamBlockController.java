package examblock.controller;

import examblock.given.SessionHandler;
import examblock.model.*;
import examblock.view.ExamBlockView;
import examblock.view.components.DialogUtils;
import examblock.view.components.FileChooser;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Main controller to coordinate between model and view
 */
public class ExamBlockController {

    /**
     * The C in MVC. The controller holds all the pieces, and controls the app.
     */
    public ExamBlockController() {
        // Construct the model
        ExamBlockModel model = new ExamBlockModel();

        // Construct the view
        ExamBlockView view = new ExamBlockView(model.getRegistry());
        view.setModel(model);

        // Register the view as an observer
        model.addObserver(view);

        // Set parent for dialogs
        DialogUtils.setParent(view.getFrame());
        FileChooser.setParent(view.getFrame());

        // Add listeners
        view.addAddButtonListener(e -> handleAdd(model, view));
        view.addClearButtonListener(e -> handleClear(view));
        view.addFinaliseButtonListener(e -> handleFinalise(model));

        // Construct and install menu items
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem loadItem = new JMenuItem("Load...");
        loadItem.addActionListener(e -> model.loadFromFile());
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> model.saveToFile(model.getRegistry(),
                model.getFilename(), model.getTitle(), model.getVersion()));
        fileMenu.add(saveItem);
        JMenuItem saveAsItem = new JMenuItem("Save As");
        saveAsItem.addActionListener(e -> model.saveToFile(model.getRegistry(),
                null, model.getTitle(), model.getVersion()));
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // View menu
        JMenu viewMenu = new JMenu("View");
        JMenuItem deskItem = new JMenuItem("Desk Allocations...");
        deskItem.addActionListener(e -> {
            StringBuilder sb = new StringBuilder();
            model.getVenues().writeAllocations(sb, model.getSessions());
            DialogUtils.showTextViewer(sb.toString(),
                    "Desk Allocations",
                    DialogUtils.ViewerOptions.SCROLL,
                    Utilities.FileType.TXT);
        });
        viewMenu.add(deskItem);
        JMenuItem finaliseItem = new JMenuItem("Finalise Reports...");
        finaliseItem.addActionListener(e -> {
            StringBuilder sb = new StringBuilder();
            model.getVenues().writeAllocations(sb, model.getSessions());
            DialogUtils.showTextViewer(sb.toString(),
                    "Finalise Reports",
                    DialogUtils.ViewerOptions.SCROLL,
                    Utilities.FileType.TXT);
        });
        viewMenu.add(finaliseItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        view.getFrame().setJMenuBar(menuBar);

        view.display();

        // prompt to load a file on startup
        model.loadFromFile();
    }

    private void handleAdd(ExamBlockModel model, ExamBlockView view) {
        int[] selectedRows = view.getSelectedExamRows();
        DefaultMutableTreeNode selectedNode = view.getSelectedTreeNode();
        if (selectedRows == null || selectedNode == null) {
            return;
        }

        Exam exam = view.getExamFromExamMap(selectedRows[0]);
        Venue venue = view.getVenueFromVenueNodeMap(selectedNode);
        if (exam == null || venue == null) {
            return;
        }

        boolean aara = venue.isAara();
        boolean scheduled = SessionHandler.scheduleExam(model, exam, venue, aara);

        if (scheduled) {
            view.updateTree(model.getSessions(), model.getVenues());
            view.updateExamTable(model.getExams());
        }
    }

    private void handleClear(ExamBlockView view) {
        view.removeAllSelections();
    }

    private void handleFinalise(ExamBlockModel model) {
        SessionHandler.finaliseExamBlock(model);
    }
}