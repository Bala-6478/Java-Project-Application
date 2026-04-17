import java.awt.*;
import javax.swing.*;

public class MultiToolApp {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Utility App");
        frame.setSize(450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: Divisibility Checker
        JPanel divPanel = new JPanel(new FlowLayout());
        JTextField divInput = new JTextField(10);
        JTextArea divResult = new JTextArea(4, 30);
        JButton divButton = new JButton("Check");
        divResult.setEditable(false);
        divPanel.add(new JLabel("Enter a number:"));
        divPanel.add(divInput);
        divPanel.add(divButton);
        divPanel.add(new JScrollPane(divResult));

        divButton.addActionListener(e -> {
            try {
                int num = Integer.parseInt(divInput.getText());
                StringBuilder sb = new StringBuilder();
                if (num % 3 == 0 && num % 5 == 0) {
                    sb.append("Divisible by 3 and 5\nMaybe an Odd Number");
                } else if (num % 2 == 0) {
                    sb.append("Divisible by 2\n" + num + " is Maybe an Even Number");
                } else {
                    sb.append("Not divisible by 2, 3, or 5");
                }
                divResult.setForeground(Color.BLUE);
                divResult.setText(sb.toString());
            } catch (NumberFormatException ex) {
                divResult.setForeground(Color.RED);
                divResult.setText("Enter a valid integer.");
            }
        });

        // Tab 2: Voting Eligibility Checker
        JPanel votePanel = new JPanel(new FlowLayout());
        JTextField ageInput = new JTextField(10);
        JLabel voteResult = new JLabel("");
        JButton voteButton = new JButton("Check");
        votePanel.add(new JLabel("Enter your age:"));
        votePanel.add(ageInput);
        votePanel.add(voteButton);
        votePanel.add(voteResult);

        voteButton.addActionListener(e -> {
            try {
                int age = Integer.parseInt(ageInput.getText());
                if (age >= 18) {
                    voteResult.setForeground(Color.BLUE);
                    voteResult.setText("You are eligible to vote.");
                } else {
                    voteResult.setForeground(Color.BLUE);
                    voteResult.setText("You are not eligible to vote.");
                }
            } catch (NumberFormatException ex) {
                voteResult.setForeground(Color.RED);
                voteResult.setText("Enter a valid age.");
            }
        });

        // Tab 3: Palindrome Checker
        JPanel palinPanel = new JPanel(new FlowLayout());
        JTextField palinInput = new JTextField(10);
        JTextArea palinResult = new JTextArea(4, 30);
        JButton palinButton = new JButton("Check");
        palinResult.setEditable(false);
        palinPanel.add(new JLabel("Enter a number:"));
        palinPanel.add(palinInput);
        palinPanel.add(palinButton);
        palinPanel.add(new JScrollPane(palinResult));

        palinButton.addActionListener(e -> {
            try {
                int num = Integer.parseInt(palinInput.getText());
                int original = num, reversed = 0;
                while (num != 0) {
                    reversed = reversed * 10 + num % 10;
                    num /= 10;
                }
                StringBuilder sb = new StringBuilder("Reversed Number: " + reversed + "\n");
                if (reversed == original) {
                    sb.append("The given number is a Palindrome.");
                } else {
                    sb.append("The given number is NOT a Palindrome.");
                }
                palinResult.setForeground(Color.BLUE);
                palinResult.setText(sb.toString());
            } catch (NumberFormatException ex) {
                palinResult.setForeground(Color.RED);
                palinResult.setText("Enter a valid integer.");
            }
        });

        // Add all tabs
        tabbedPane.addTab("Divisibility", divPanel);
        tabbedPane.addTab("Vote Check", votePanel);
        tabbedPane.addTab("Palindrome", palinPanel);

        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }
}
