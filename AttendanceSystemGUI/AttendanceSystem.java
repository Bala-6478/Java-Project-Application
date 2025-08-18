import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

public class AttendanceSystem extends JFrame {
    private JTextField nameField, rollField;
    private JButton addButton, markPresentButton, markAbsentButton;
    private JTable table;
    private DefaultTableModel tableModel;

    public AttendanceSystem() {
        setTitle("Student Attendance Management System");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Top Panel for input
        JPanel inputPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add Student"));

        nameField = new JTextField();
        rollField = new JTextField();
        addButton = new JButton("Add Student");

        inputPanel.add(new JLabel("Student Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel());
        inputPanel.add(new JLabel("Roll No:"));
        inputPanel.add(rollField);
        inputPanel.add(addButton);

        // Table
        tableModel = new DefaultTableModel(new String[]{"Roll No", "Name", "Attendance"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // Buttons
        JPanel buttonPanel = new JPanel();
        markPresentButton = new JButton("Mark Present");
        markAbsentButton = new JButton("Mark Absent");
        buttonPanel.add(markPresentButton);
        buttonPanel.add(markAbsentButton);

        // Add components to frame
        setLayout(new BorderLayout(10, 10));
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Event: Add Student
        addButton.addActionListener(e -> {
            String name = nameField.getText();
            String roll = rollField.getText();

            if (name.isEmpty() || roll.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both name and roll no.");
            } else {
                tableModel.addRow(new Object[]{roll, name, "Not Marked"});
                nameField.setText("");
                rollField.setText("");
            }
        });

        // Event: Mark Present
        markPresentButton.addActionListener(e -> markAttendance("Present"));

        // Event: Mark Absent
        markAbsentButton.addActionListener(e -> markAttendance("Absent"));
    }

    private void markAttendance(String status) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student.");
        } else {
            tableModel.setValueAt(status, selectedRow, 2);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AttendanceSystem().setVisible(true);
        });
    }
}
