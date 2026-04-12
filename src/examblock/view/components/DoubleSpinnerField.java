package examblock.view.components;

import javax.swing.*;
import java.awt.*;

/**
 * A custom text field for entering and adjusting double values with spinner buttons.
 * Extends {@link DoubleTextField} to inherit double input validation and adds up/down
 * buttons to increment/decrement the value. Values are rounded to one decimal place
 * for display and arithmetic.
 */
public class DoubleSpinnerField extends DoubleTextField {

    /** Increment/decrement step (matches 1 decimal place) */
    private static final double INCREMENT = 0.1;

    /** Minimum allowed value */
    private double minimumValue = -Double.MAX_VALUE;

    /** Maximum allowed value */
    private double maximumValue = Double.MAX_VALUE;

    /** Button for incrementing the value */
    private JButton upButton;

    /** Button for decrementing the value */
    private JButton downButton;

    /**
     * Constructs a new {@code DoubleSpinnerField} with default settings.
     * Initializes the text field for double input and adds spinner buttons for
     * incrementing/decrementing the value by 0.1. The initial value is 0.0,
     * displayed with one decimal place.
     */
    public DoubleSpinnerField() {
        super(); // Initialize DoubleTextField
        initSpinner();
    }

    /**
     * Initializes the spinner UI, adding up and down buttons to the right of the text field.
     * Configures the buttons with a hand cursor and action listeners for increment/decrement.
     */
    private void initSpinner() {
        // Use BorderLayout for the spinner
        setLayout(new BorderLayout(2, 0));

        // Create up/down buttons
        upButton = new JButton("↑");
        downButton = new JButton("↓");
        upButton.setMargin(new Insets(2, 2, 2, 2));
        downButton.setMargin(new Insets(2, 2, 2, 2));
        upButton.setFont(new Font("Arial", Font.PLAIN, 10));
        downButton.setFont(new Font("Arial", Font.PLAIN, 10));
        upButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        downButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Panel for buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        buttonPanel.add(upButton);
        buttonPanel.add(downButton);

        // Add buttons to the EAST
        add(buttonPanel, BorderLayout.EAST);

        // Action listeners for buttons
        upButton.addActionListener(e -> incrementValue(INCREMENT));
        downButton.addActionListener(e -> incrementValue(-INCREMENT));
    }

    /**
     * Increments or decrements the current value by the specified delta.
     * The new value is rounded to one decimal place and constrained within
     * {@code minimumValue} and {@code maximumValue}. Updates the text field
     * and notifies listeners of the change.
     *
     * @param delta the amount to add (positive) or subtract (negative)
     */
    private void incrementValue(double delta) {
        try {
            double currentValue = getDouble();
            double newValue = currentValue + delta;

            // Enforce min/max bounds
            if (newValue < minimumValue) {
                newValue = minimumValue;
            } else if (newValue > maximumValue) {
                newValue = maximumValue;
            }

            // Round to 1 decimal place
            newValue = Math.round(newValue * 10.0) / 10.0;
            setDouble(newValue);
        } catch (NumberFormatException e) {
            setDouble(minimumValue);
        }

        // Notify listeners (e.g., DocumentListener) of the change
        firePropertyChange("value", null, getDouble());
    }

    /**
     * Sets the value of the text field to the specified double, formatted to one decimal place.
     *
     * @param value the double value to set
     */
    @Override
    public void setDouble(double value) {
        // Format to 1 decimal place
        String formatted = String.format("%.1f", value);
        setText(formatted);
    }

    /**
     * Sets the minimum allowed value for the spinner.
     * Values below this will be adjusted to the minimum.
     *
     * @param min the minimum value
     */
    public void setMinimum(double min) {
        this.minimumValue = min;
        // Adjust current value if necessary
        if (getDouble() < min) {
            setDouble(min);
        }
    }

    /**
     * Sets the maximum allowed value for the spinner.
     * Values above this will be adjusted to the maximum.
     *
     * @param max the maximum value
     */
    public void setMaximum(double max) {
        this.maximumValue = max;
        // Adjust current value if necessary
        if (getDouble() > max) {
            setDouble(max);
        }
    }

    /**
     * Returns the preferred size of the component, accounting for the text field and buttons.
     *
     * @return the preferred dimension
     */
    @Override
    public Dimension getPreferredSize() {
        Dimension textSize = super.getPreferredSize();
        Dimension buttonSize = upButton.getPreferredSize();
        return new Dimension(textSize.width + buttonSize.width + 2, textSize.height);
    }
}