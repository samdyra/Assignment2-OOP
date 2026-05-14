package examblock.controller;

import examblock.given.SessionHandler;
import examblock.model.ExamBlockModel;
import examblock.model.Utilities;
import examblock.view.ExamBlockView;
import examblock.view.components.DialogUtils;
import examblock.view.components.FileChooser;

import javax.swing.*;

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
        ExamBlockView view = new ExamBlockView(model);

        // Register the view as an observer of the model
        model.addObserver(view);

        // Set parent for dialogs
        DialogUtils.setParent(view);
        FileChooser.setParent(view);

        // Construct and install menu items
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");

        JMenuItem loadItem = new JMenuItem("Load...");
        loadItem.addActionListener(e -> {
            model.loadFromFile();
        });
        fileMenu.add(loadItem);

        fileMenu.addSeparator();

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> {
            model.saveToFile(model.getRegistry(), model.getFilename(),
                    model.getTitle(), model.getVersion());
        });
        fileMenu.add(saveItem);

        JMenuItem saveAsItem = new JMenuItem("Save As");
        saveAsItem.addActionListener(e -> {
            model.saveToFile(model.getRegistry(), null,
                    model.getTitle(), model.getVersion());
        });
        fileMenu.add(saveAsItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> {
            System.exit(0);
        });
        fileMenu.add(exitItem);

        // View menu
        JMenu viewMenu = new JMenu("View");

        JMenuItem deskAllocationsItem = new JMenuItem("Desk Allocations...");
        deskAllocationsItem.addActionListener(e -> {
            model.getVenues().printAllocations(model.getSessions());
        });
        viewMenu.add(deskAllocationsItem);

        JMenuItem finaliseItem = new JMenuItem("Finalise Reports...");
        finaliseItem.addActionListener(e -> {
            SessionHandler.finaliseExamBlock(model);
        });
        viewMenu.add(finaliseItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        view.setJMenuBar(menuBar);

        // Display the view
        view.setVisible(true);
    }
}