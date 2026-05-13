package examblock.view.components;

import examblock.model.Utilities;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * A save dialog that prompts for overwrite, as well as ensuring
 * newVersion is greater than oldVersion.
 */
public class FileChooser {
    private static Component parent;
    private final String title;
    private final double oldVersion;
    private double newVersion;
    private JTextField titleField;
    private JTextField versionField;

    /**
     * Create a new FileChooser
     *
     * @param title            the name of the Exam Block, not the filename.
     * @param oldVersion       the existing version of the Exam Block being saved.
     * @param suggestedVersion this value is placed into the textbox as a default.
     */
    public FileChooser(String title, double oldVersion, double suggestedVersion) {
        this.title = title;
        this.oldVersion = oldVersion;
        this.newVersion = suggestedVersion;
    }

    /**
     * Create a new FileChooser
     *
     * @param title      the name of the Exam Block, not the filename.
     * @param oldVersion the existing version of the Exam Block being saved.
     */
    public FileChooser(String title, double oldVersion) {
        this(title, oldVersion, oldVersion + 0.1);
    }

    /**
     * Create a new FileChooser with empty title and version, used when
     * those values aren't needed and the accessory panel is not needed.
     */
    public FileChooser() {
        this("", 0);
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
     * getter
     *
     * @return new Title
     */
    public String title() {
        if (titleField != null && !titleField.getText().isEmpty()) {
            return titleField.getText();
        }
        return title;
    }

    /**
     * getter
     *
     * @return new Version
     */
    public double version() {
        if (versionField != null && !versionField.getText().isEmpty()) {
            try {
                return Double.parseDouble(versionField.getText());
            } catch (NumberFormatException e) {
                return newVersion;
            }
        }
        return newVersion;
    }

    /**
     * If you want to set an acceptable version number that the user can
     * just accept, call this with the version you want.
     *
     * @param suggestedVersion the version the user can accept as-is.
     */
    public void suggestedVersion(double suggestedVersion) {
        this.newVersion = suggestedVersion;
    }

    /**
     * On linux (at least) the save file dialog does not protect you from
     * overwriting existing files. This method will prompt the user to
     * confirm the overwrite.
     *
     * @param hint     initial suggested filename, or null (or empty) for none
     * @param fileType Select this file filter by default.
     * @return the selected new filename, or an empty string if none chosen.
     */
    public String save(String hint, Utilities.FileType fileType) {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setFileFilter(new FileNameExtensionFilter(
                fileType.getExtension().toUpperCase() + " Files",
                fileType.getExtension()));

        if (hint != null && !hint.isEmpty()) {
            chooser.setSelectedFile(new File(hint));
        }

        // add accessory panel for title and ver
        if (!title.isEmpty()) {
            JPanel accessory = new JPanel();
            accessory.setLayout(new BoxLayout(accessory, BoxLayout.Y_AXIS));
            accessory.add(new JLabel("Title:"));
            titleField = new JTextField(title);
            accessory.add(titleField);
            accessory.add(new JLabel("Version (must be > " + oldVersion + "):"));
            versionField = new JTextField(String.valueOf(newVersion));
            accessory.add(versionField);
            chooser.setAccessory(accessory);
        }

        int result = chooser.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();

            // check vers is > than old
            if (!title.isEmpty()) {
                double enteredVersion = version();
                if (enteredVersion <= oldVersion
                        && !Utilities.isBadVersion(oldVersion)) {
                    JOptionPane.showMessageDialog(parent,
                            "Version must be greater than " + oldVersion);
                    return "";
                }
            }

            if (selectedFile.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(parent,
                        selectedFile.getName() + " already exists. Overwrite?",
                        "Confirm Overwrite",
                        JOptionPane.YES_NO_OPTION);
                if (overwrite != JOptionPane.YES_OPTION) {
                    return "";
                }
            }

            return selectedFile.getAbsolutePath();
        }
        return "";
    }

    /**
     * Display the load/open dialog, and return the selected filename.
     *
     * @param hint     initial suggested filename, or null (or empty) for none
     * @param fileType Select this file filter by default.
     * @return the selected existing file as a File, or null if none chosen.
     */
    public File open(String hint, Utilities.FileType fileType) {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setFileFilter(new FileNameExtensionFilter(
                fileType.getExtension().toUpperCase() + " Files",
                fileType.getExtension()));

        if (hint != null && !hint.isEmpty()) {
            chooser.setSelectedFile(new File(hint));
        }

        int result = chooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }
}