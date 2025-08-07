import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main1 {

    public static void main(String[] args) {
        // Create frame
        JFrame frame = new JFrame("Password Checker");
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        // Components
        JLabel label = new JLabel("Enter Password:");
        JPasswordField passwordField = new JPasswordField(20);
        JButton checkButton = new JButton("Check Password");
        JLabel resultLabel = new JLabel("");

        // Action listener for button
        checkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String password = new String(passwordField.getPassword());
                String result = checkPassword(password);
                resultLabel.setText(result);
            }
        });

        // Add components to frame
        frame.add(label);
        frame.add(passwordField);
        frame.add(checkButton);
        frame.add(resultLabel);

        frame.setVisible(true);
    }

    // Password validation logic
    public static String checkPassword(String password) {
        if (password.length() < 8) {
            return "Password too short (min 8 characters)";
        }
        if (!password.matches(".*\\d.*")) {
            return "Password must contain at least one digit";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter";
        }
        if (!password.matches(".*[!@#$%^&*()].*")) {
            return "Password must contain at least one special character (!@#$%^&*())";
        }
        return "Password is strong!";
    }
}