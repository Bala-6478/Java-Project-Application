import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Calculatorpro extends JFrame implements ActionListener {
    JTextField textField;
    double num1 = 0, num2 = 0, result = 0;
    char operator;

    Calculatorpro() {
        setTitle("Calculator");
        setSize(400, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // White background
        getContentPane().setBackground(Color.WHITE);

        textField = new JTextField();
        textField.setBounds(30, 40, 320, 50);
        textField.setEditable(false);
        textField.setFont(new Font("Arial", Font.BOLD, 20));
        add(textField);

        String[] buttonLabels = {
            "7", "8", "9", "/", 
            "4", "5", "6", "*", 
            "1", "2", "3", "-", 
            ".", "0", "=", "+",
            "C", "%", "√", "x²", "1/x"
        };

        JButton[] buttons = new JButton[buttonLabels.length];

        int x = 30, y = 100;
        for (int i = 0; i < buttonLabels.length; i++) {
            buttons[i] = new JButton(buttonLabels[i]);
            buttons[i].setBounds(x, y, 70, 40);
            buttons[i].addActionListener(this);
            buttons[i].setFont(new Font("Arial", Font.PLAIN, 18));

            // Button colors
            if (buttonLabels[i].matches("[0-9.]")) {
                buttons[i].setBackground(new Color(230, 230, 230)); // light gray for numbers
            } else if (buttonLabels[i].equals("C")) {
                buttons[i].setBackground(new Color(255, 120, 120)); // red for clear
                buttons[i].setForeground(Color.WHITE);
            } else {
                buttons[i].setBackground(new Color(150, 200, 255)); // blue for operators/functions
            }

            add(buttons[i]);

            x += 80;
            if ((i + 1) % 4 == 0) {
                x = 30;
                y += 60;
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        try {
            if ((command.charAt(0) >= '0' && command.charAt(0) <= '9') || command.equals(".")) {
                textField.setText(textField.getText() + command);
            } else if (command.equals("C")) {
                textField.setText("");
                num1 = num2 = result = 0;
            } else if (command.equals("=")) {
                num2 = Double.parseDouble(textField.getText());
                switch (operator) {
                    case '+': result = num1 + num2; break;
                    case '-': result = num1 - num2; break;
                    case '*': result = num1 * num2; break;
                    case '/': result = num1 / num2; break;
                    case '%': result = num1 % num2; break;
                }
                textField.setText(String.valueOf(result));
                num1 = result;
            } else if (command.equals("√")) {
                num1 = Double.parseDouble(textField.getText());
                result = Math.sqrt(num1);
                textField.setText(String.valueOf(result));
            } else if (command.equals("x²")) {
                num1 = Double.parseDouble(textField.getText());
                result = Math.pow(num1, 2);
                textField.setText(String.valueOf(result));
            } else if (command.equals("1/x")) {
                num1 = Double.parseDouble(textField.getText());
                result = 1 / num1;
                textField.setText(String.valueOf(result));
            } else {
                num1 = Double.parseDouble(textField.getText());
                operator = command.charAt(0);
                textField.setText("");
            }
        } catch (Exception ex) {
            textField.setText("Error");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Calculatorpro().setVisible(true);
        });
    }
}
