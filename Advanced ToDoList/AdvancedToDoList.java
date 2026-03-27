import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdvancedToDoList {

    private JFrame frame;
    private JTextField taskField, searchField;
    private JComboBox<String> priorityBox;
    private DefaultListModel<String> model;
    private JList<String> list;

    private final String FILE_NAME = "tasks.txt";

    public AdvancedToDoList() {
        frame = new JFrame("Advanced To-Do List");
        frame.setSize(500, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Top Panel
        JPanel top = new JPanel(new GridLayout(2, 1));

        JPanel inputPanel = new JPanel(new BorderLayout());
        taskField = new JTextField();
        priorityBox = new JComboBox<>(new String[]{"Low", "Medium", "High"});
        JButton addBtn = new JButton("Add Task");

        inputPanel.add(taskField, BorderLayout.CENTER);
        inputPanel.add(priorityBox, BorderLayout.WEST);
        inputPanel.add(addBtn, BorderLayout.EAST);

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        JButton searchBtn = new JButton("Search");

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);

        top.add(inputPanel);
        top.add(searchPanel);

        // Center List
        model = new DefaultListModel<>();
        list = new JList<>(model);
        list.setFont(new Font("Arial", Font.PLAIN, 16));
        JScrollPane scroll = new JScrollPane(list);

        // Bottom Panel
        JPanel bottom = new JPanel();

        JButton deleteBtn = new JButton("Delete");
        JButton doneBtn = new JButton("Mark Done");
        JButton clearBtn = new JButton("Clear All");

        bottom.add(doneBtn);
        bottom.add(deleteBtn);
        bottom.add(clearBtn);

        // Add to Frame
        frame.add(top, BorderLayout.NORTH);
        frame.add(scroll, BorderLayout.CENTER);
        frame.add(bottom, BorderLayout.SOUTH);

        // Load tasks
        loadTasks();

        // Add Task
        addBtn.addActionListener(e -> addTask());

        // Delete Task
        deleteBtn.addActionListener(e -> deleteTask());

        // Mark Done
        doneBtn.addActionListener(e -> markDone());

        // Clear All
        clearBtn.addActionListener(e -> clearAll());

        // Search
        searchBtn.addActionListener(e -> searchTask());

        frame.setVisible(true);
    }

    private void addTask() {
        String task = taskField.getText().trim();
        String priority = priorityBox.getSelectedItem().toString();

        if (!task.isEmpty()) {
            String time = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd-MM HH:mm"));

            model.addElement("[" + priority + "] " + task + " (" + time + ")");
            taskField.setText("");
            saveTasks();
        } else {
            JOptionPane.showMessageDialog(frame, "Enter a task!");
        }
    }

    private void deleteTask() {
        int i = list.getSelectedIndex();
        if (i != -1) {
            model.remove(i);
            saveTasks();
        }
    }

    private void markDone() {
        int i = list.getSelectedIndex();
        if (i != -1) {
            String task = model.get(i);
            model.set(i, "✔ " + task);
            saveTasks();
        }
    }

    private void clearAll() {
        int confirm = JOptionPane.showConfirmDialog(frame, "Clear all tasks?");
        if (confirm == 0) {
            model.clear();
            saveTasks();
        }
    }

    private void searchTask() {
        String keyword = searchField.getText().toLowerCase();

        if (keyword.isEmpty()) {
            loadTasks();
            return;
        }

        DefaultListModel<String> filtered = new DefaultListModel<>();

        for (int i = 0; i < model.size(); i++) {
            String task = model.get(i).toLowerCase();
            if (task.contains(keyword)) {
                filtered.addElement(model.get(i));
            }
        }

        list.setModel(filtered);
    }

    private void saveTasks() {
        try (PrintWriter writer = new PrintWriter(FILE_NAME)) {
            for (int i = 0; i < model.size(); i++) {
                writer.println(model.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTasks() {
        model.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                model.addElement(line);
            }
        } catch (Exception e) {
            // File may not exist first time
        }
        list.setModel(model);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdvancedToDoList::new);
    }
}