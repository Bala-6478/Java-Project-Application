import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class InterestCalculator extends JFrame implements ActionListener {
    // Components
    JTextField principalField, rateField, timeField;
    JLabel resultLabel;
    JRadioButton simpleBtn, compoundBtn;
    ButtonGroup interestGroup;

    public InterestCalculator() {
        setTitle("Interest Calculator");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(7, 2, 10, 5));

        // Labels and TextFields
        add(new JLabel("Principal (₹):"));
        principalField = new JTextField();
        add(principalField);

        add(new JLabel("Rate of Interest (%):"));
        rateField = new JTextField();
        add(rateField);

        add(new JLabel("Time (Years):"));
        timeField = new JTextField();
        add(timeField);

        // Interest type radio buttons
        add(new JLabel("Interest Type:"));
        JPanel typePanel = new JPanel();
        simpleBtn = new JRadioButton("Simple", true);
        compoundBtn = new JRadioButton("Compound");
        interestGroup = new ButtonGroup();
        interestGroup.add(simpleBtn);
        interestGroup.add(compoundBtn);
        typePanel.add(simpleBtn);
        typePanel.add(compoundBtn);
        add(typePanel);

        // Button
        JButton calculateBtn = new JButton("Calculate");
        calculateBtn.addActionListener(this);
        add(calculateBtn);

        // Result label
        add(new JLabel("Result:"));
        resultLabel = new JLabel("");
        add(resultLabel);

        setLocationRelativeTo(null); // Center the frame
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            double principal = Double.parseDouble(principalField.getText());
            double rate = Double.parseDouble(rateField.getText());
            double time = Double.parseDouble(timeField.getText());

            double interest = 0, amount = 0;

            if (simpleBtn.isSelected()) {
                interest = (principal * rate * time) / 100;
                amount = principal + interest;
            } else {
                amount = principal * Math.pow((1 + rate / 100), time);
                interest = amount - principal;
            }

            resultLabel.setText(String.format("<html>Interest: ₹%.2f<br>Total Amount: ₹%.2f</html>", interest, amount));
        } catch (NumberFormatException ex) {
            resultLabel.setText("Please enter valid numbers.");
        }
    }

    public static void main(String[] args) {
        new InterestCalculator();
    }
}
