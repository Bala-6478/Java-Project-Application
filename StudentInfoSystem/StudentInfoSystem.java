// File Name: StudentInfoSystem.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

class Student {
    String name, roll, course, email, phone, gender, address;

    public Student(String name, String roll, String course, String email, String phone, String gender, String address) {
        this.name = name;
        this.roll = roll;
        this.course = course;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.address = address;
    }

    @Override
    public String toString() {
        return "Name: " + name +
               "\nRoll No: " + roll +
               "\nCourse: " + course +
               "\nEmail: " + email +
               "\nPhone: " + phone +
               "\nGender: " + gender +
               "\nAddress: " + address + "\n----------------------\n";
    }
}

public class StudentInfoSystem extends JFrame implements ActionListener {
    // Storage for all students
    static ArrayList<Student> studentList = new ArrayList<>();

    // Student form fields
    JTextField nameField, rollField, courseField, emailField, phoneField;
    JTextArea addressArea;
    JComboBox<String> genderBox;
    JButton submitBtn, clearBtn, adminBtn;

    public StudentInfoSystem() {
        setTitle("Student Information Form");
        setSize(500, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Enter Student Details"));

        nameField = new JTextField();
        rollField = new JTextField();
        courseField = new JTextField();
        emailField = new JTextField();
        phoneField = new JTextField();
        addressArea = new JTextArea(3, 20);
        genderBox = new JComboBox<>(new String[]{"Select", "Male", "Female", "Other"});

        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Roll No:"));
        formPanel.add(rollField);
        formPanel.add(new JLabel("Course:"));
        formPanel.add(courseField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Phone:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Gender:"));
        formPanel.add(genderBox);
        formPanel.add(new JLabel("Address:"));
        formPanel.add(new JScrollPane(addressArea));

        submitBtn = new JButton("Submit");
        clearBtn = new JButton("Clear");
        adminBtn = new JButton("Admin Panel");

        formPanel.add(submitBtn);
        formPanel.add(clearBtn);

        add(formPanel, BorderLayout.CENTER);
        add(adminBtn, BorderLayout.SOUTH);

        submitBtn.addActionListener(this);
        clearBtn.addActionListener(this);
        adminBtn.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitBtn) {
            String name = nameField.getText();
            String roll = rollField.getText();
            String course = courseField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            String gender = (String) genderBox.getSelectedItem();
            String address = addressArea.getText();

            if (name.isEmpty() || roll.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and Roll No are required!");
                return;
            }

            Student s = new Student(name, roll, course, email, phone, gender, address);
            studentList.add(s);

            JOptionPane.showMessageDialog(this, "Student Information Saved!");

            clearFields();
        } 
        else if (e.getSource() == clearBtn) {
            clearFields();
        }
        else if (e.getSource() == adminBtn) {
            new AdminLogin().setVisible(true);
        }
    }

    private void clearFields() {
        nameField.setText("");
        rollField.setText("");
        courseField.setText("");
        emailField.setText("");
        phoneField.setText("");
        addressArea.setText("");
        genderBox.setSelectedIndex(0);
    }

    // ================= ADMIN LOGIN =================
    static class AdminLogin extends JFrame implements ActionListener {
        private JPasswordField passwordField;
        private JButton loginBtn;

        public AdminLogin() {
            setTitle("Admin Login");
            setSize(300, 150);
            setLocationRelativeTo(null);
            setLayout(new GridLayout(3, 1, 10, 10));

            JLabel label = new JLabel("Enter Admin Password:", SwingConstants.CENTER);
            passwordField = new JPasswordField();
            loginBtn = new JButton("Login");

            add(label);
            add(passwordField);
            add(loginBtn);

            loginBtn.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String password = new String(passwordField.getPassword());
            if (password.equals("6478")) {
                new AdminPanel().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Password!");
                passwordField.setText("");
            }
        }
    }

    // ================= ADMIN PANEL =================
    static class AdminPanel extends JFrame {
        JTextArea displayArea;

        public AdminPanel() {
            setTitle("Admin Panel - Student Records");
            setSize(500, 400);
            setLocationRelativeTo(null);

            displayArea = new JTextArea();
            displayArea.setEditable(false);
            displayArea.setBorder(BorderFactory.createTitledBorder("All Student Information"));

            JScrollPane scrollPane = new JScrollPane(displayArea);
            add(scrollPane);

            // Display all students
            StringBuilder sb = new StringBuilder();
            for (Student s : studentList) {
                sb.append(s.toString());
            }
            if (sb.length() == 0) {
                sb.append("No student records found!");
            }
            displayArea.setText(sb.toString());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StudentInfoSystem().setVisible(true);
        });
    }
}
