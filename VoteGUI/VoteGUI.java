import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class VoteGUI {
    public static void main(String[] args) {
        // Create the main window
        JFrame frame = new JFrame("Vote Eligibility Checker");
        frame.setSize(350, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        // Components
        JLabel label = new JLabel("Enter your age:");
        JTextField ageField = new JTextField(10);
        JButton checkButton = new JButton("Check");
        JLabel resultLabel = new JLabel("");

        // Style result label
        resultLabel.setFont(new Font("Arial", Font.BOLD, 14));
        resultLabel.setForeground(Color.BLUE);

        // Add components
        frame.add(label);
        frame.add(ageField);
        frame.add(checkButton);
        frame.add(resultLabel);

        // Button action
        checkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int age = Integer.parseInt(ageField.getText());
                    if (age >= 18) {
                        resultLabel.setText("You are eligible to vote.");
                    } else {
                        resultLabel.setText("You are not eligible to vote.");
                    }
                } catch (NumberFormatException ex) {
                    resultLabel.setText("Please enter a valid age.");
                    resultLabel.setForeground(Color.RED);
                }
            }
        });

        // Show the frame
        frame.setVisible(true);
    }
}