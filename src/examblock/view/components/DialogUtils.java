package examblock.view.components;

import examblock.model.Utilities;

import javax.swing.*;
import java.awt.*;

/**
 * Dialogs for easy use
 */
public class DialogUtils {

    /**
     * How to handle long lines of text in the TextViewer
     */
    public enum ViewerOptions {
        /** horizontal scroll */
        SCROLL,
        /** wrap long lines mode */
        WRAP
    }

    /** The parent frame for dialogs */
    private static Component parent;

    /**
     * Default constructor
     */
    public DialogUtils() {
    }

    /**
     * set the frame for dialogs
     *
     * @param frame where we should be centered
     */
    public static void setParent(Component frame) {
        parent = frame;
    }

    /**
     * put up a dialog box with a JTextBox in it
     *
     * @param message      what to say
     * @param title        title for the popup window
     * @param initialValue if any
     * @return the entered text
     */
    public static String getUserInput(String message, String title,
                                      String initialValue) {
        return (String) JOptionPane.showInputDialog(parent, message, title,
                JOptionPane.QUESTION_MESSAGE, null, null, initialValue);
    }

    /**
     * Displays a text viewer dialog with a wrap text option. Shows the
     * provided text in a scrollable text area, with a View menu to toggle
     * line wrapping. The option parameter sets the initial wrap state.
     *
     * @param text     the text to display
     * @param title    the dialog title
     * @param option   how to handle long lines of text in the TextViewer
     * @param fileType the file type to save the text as
     */
    public static void showTextViewer(String text, String title,
                                      ViewerOptions option,
                                      Utilities.FileType fileType) {
        JDialog dialog = new JDialog((Frame) parent, title, true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(parent);

        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        // wrap mode
        boolean wrapText = (option == ViewerOptions.WRAP);
        textArea.setLineWrap(wrapText);
        textArea.setWrapStyleWord(wrapText);

        final JScrollPane scrollPane = new JScrollPane(textArea);

        // wrap toggle
        JMenuBar menuBar = new JMenuBar();
        JMenu viewMenu = new JMenu("View");
        JCheckBoxMenuItem wrapItem = new JCheckBoxMenuItem("Wrap Text", wrapText);
        wrapItem.addActionListener(e -> {
            textArea.setLineWrap(wrapItem.isSelected());
            textArea.setWrapStyleWord(wrapItem.isSelected());
        });
        viewMenu.add(wrapItem);
        menuBar.add(viewMenu);

        dialog.setJMenuBar(menuBar);
        dialog.add(scrollPane);
        dialog.setVisible(true);
    }

    /**
     * simple informational box
     *
     * @param message what to say
     */
    public static void showMessage(String message) {
        JOptionPane.showMessageDialog(parent, message);
    }

    /**
     * prompt for a Yes/No/Cancel response from the user
     *
     * @param message text to display
     * @return return code
     */
    public static int askQuestion(String message) {
        return JOptionPane.showConfirmDialog(parent, message,
                "Confirm", JOptionPane.YES_NO_CANCEL_OPTION);
    }
}