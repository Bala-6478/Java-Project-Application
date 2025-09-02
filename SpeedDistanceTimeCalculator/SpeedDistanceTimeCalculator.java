import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SpeedDistanceTimeCalculator extends JFrame implements ActionListener {
    private JComboBox<String> calculationChoice, distanceUnit, speedUnit, timeUnit;
    private JTextField distanceField, speedField, timeField, resultField;
    private JButton calculateButton, clearButton;

    public SpeedDistanceTimeCalculator() {
        // Frame setup
        setTitle("Speed Distance Time Calculator");
        setSize(450, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title label
        JLabel titleLabel = new JLabel("Speed Distance Time Calculator", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(150, 0, 0));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridwidth = 3;
        gbc.gridx = 0; gbc.gridy = 0;
        add(titleLabel, gbc);

        // Dropdown for calculation choice
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Choose a Calculation:"), gbc);

        calculationChoice = new JComboBox<>(new String[]{"Solve for Speed", "Solve for Distance", "Solve for Time"});
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        add(calculationChoice, gbc);

        // Distance input
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Distance ="), gbc);

        distanceField = new JTextField();
        gbc.gridx = 1; gbc.gridy = 2;
        add(distanceField, gbc);

        distanceUnit = new JComboBox<>(new String[]{"km", "mi", "m"});
        gbc.gridx = 2; gbc.gridy = 2;
        add(distanceUnit, gbc);

        // Speed input
        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Speed ="), gbc);

        speedField = new JTextField();
        gbc.gridx = 1; gbc.gridy = 3;
        add(speedField, gbc);

        speedUnit = new JComboBox<>(new String[]{"km/h", "mi/h", "m/s"});
        gbc.gridx = 2; gbc.gridy = 3;
        add(speedUnit, gbc);

        // Time input
        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("Time ="), gbc);

        timeField = new JTextField();
        gbc.gridx = 1; gbc.gridy = 4;
        add(timeField, gbc);

        timeUnit = new JComboBox<>(new String[]{"hr", "min", "sec"});
        gbc.gridx = 2; gbc.gridy = 4;
        add(timeUnit, gbc);

        // Buttons
        calculateButton = new JButton("Calculate");
        clearButton = new JButton("Clear");

        gbc.gridx = 0; gbc.gridy = 5;
        add(clearButton, gbc);
        gbc.gridx = 1; gbc.gridy = 5;
        add(calculateButton, gbc);

        // Result field
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 3;
        add(new JLabel("Answer:"), gbc);

        resultField = new JTextField();
        resultField.setEditable(false);
        gbc.gridy = 7;
        add(resultField, gbc);

        // Add listeners
        calculateButton.addActionListener(this);
        clearButton.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == calculateButton) {
            try {
                String choice = (String) calculationChoice.getSelectedItem();
                double distance = distanceField.getText().isEmpty() ? 0 : Double.parseDouble(distanceField.getText());
                double speed = speedField.getText().isEmpty() ? 0 : Double.parseDouble(speedField.getText());
                double time = timeField.getText().isEmpty() ? 0 : Double.parseDouble(timeField.getText());

                double result = 0;

                switch (choice) {
                    case "Solve for Speed":
                        result = distance / time;
                        resultField.setText(String.format("%.2f %s", result, speedUnit.getSelectedItem()));
                        break;
                    case "Solve for Distance":
                        result = speed * time;
                        resultField.setText(String.format("%.2f %s", result, distanceUnit.getSelectedItem()));
                        break;
                    case "Solve for Time":
                        result = distance / speed;
                        resultField.setText(String.format("%.2f %s", result, timeUnit.getSelectedItem()));
                        break;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Enter valid numeric values.");
            }
        } else if (e.getSource() == clearButton) {
            distanceField.setText("");
            speedField.setText("");
            timeField.setText("");
            resultField.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SpeedDistanceTimeCalculator().setVisible(true);
        });
    }
}
