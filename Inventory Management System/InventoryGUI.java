import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

class Item {
    String id, name;
    int quantity;
    double price;

    Item(String id, String name, int quantity, double price) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }
}

class InventoryGUI extends JFrame {
    private ArrayList<Item> inventory = new ArrayList<>();
    private final String fileName = "inventory.txt";
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField idField, nameField, qtyField, priceField, searchField;

    public InventoryGUI() {
        setTitle("Inventory Management System");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table
        String[] columnNames = {"ID", "Name", "Quantity", "Price"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Input Fields
        JPanel inputPanel = new JPanel(new GridLayout(2, 5, 10, 5));
        idField = new JTextField();
        nameField = new JTextField();
        qtyField = new JTextField();
        priceField = new JTextField();
        JButton addButton = new JButton("Add");

        inputPanel.add(new JLabel("ID:"));
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(new JLabel("Quantity:"));
        inputPanel.add(new JLabel("Price:"));
        inputPanel.add(new JLabel("")); // empty

        inputPanel.add(idField);
        inputPanel.add(nameField);
        inputPanel.add(qtyField);
        inputPanel.add(priceField);
        inputPanel.add(addButton);
        add(inputPanel, BorderLayout.NORTH);

        // Control Buttons
        JPanel controlPanel = new JPanel();
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        JButton searchButton = new JButton("Search");
        JButton saveButton = new JButton("Save & Exit");
        searchField = new JTextField(10);

        controlPanel.add(new JLabel("Search:"));
        controlPanel.add(searchField);
        controlPanel.add(searchButton);
        controlPanel.add(editButton);
        controlPanel.add(deleteButton);
        controlPanel.add(saveButton);
        add(controlPanel, BorderLayout.SOUTH);

        // Action Listeners
        addButton.addActionListener(e -> addItem());
        editButton.addActionListener(e -> editItem());
        deleteButton.addActionListener(e -> deleteItem());
        searchButton.addActionListener(e -> searchItem());
        saveButton.addActionListener(e -> {
            saveToFile();
            System.exit(0);
        });

        loadFromFile();
        refreshTable();
        setVisible(true);
    }

    private void addItem() {
        try {
            String id = idField.getText();
            String name = nameField.getText();
            int qty = Integer.parseInt(qtyField.getText());
            double price = Double.parseDouble(priceField.getText());

            inventory.add(new Item(id, name, qty, price));
            refreshTable();
            clearFields();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input.");
        }
    }

    private void editItem() {
        int selected = table.getSelectedRow();
        if (selected >= 0) {
            try {
                String id = (String) tableModel.getValueAt(selected, 0);
                for (Item item : inventory) {
                    if (item.id.equals(id)) {
                        item.name = JOptionPane.showInputDialog("Enter new name:", item.name);
                        item.quantity = Integer.parseInt(JOptionPane.showInputDialog("Enter new quantity:", item.quantity));
                        item.price = Double.parseDouble(JOptionPane.showInputDialog("Enter new price:", item.price));
                        refreshTable();
                        return;
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error editing item.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select an item to edit.");
        }
    }

    private void deleteItem() {
        int selected = table.getSelectedRow();
        if (selected >= 0) {
            String id = (String) tableModel.getValueAt(selected, 0);
            inventory.removeIf(item -> item.id.equals(id));
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Select an item to delete.");
        }
    }

    private void searchItem() {
        String keyword = searchField.getText().toLowerCase();
        DefaultTableModel searchModel = new DefaultTableModel(new String[]{"ID", "Name", "Quantity", "Price"}, 0);
        for (Item item : inventory) {
            if (item.id.toLowerCase().contains(keyword) || item.name.toLowerCase().contains(keyword)) {
                searchModel.addRow(new Object[]{item.id, item.name, item.quantity, item.price});
            }
        }
        table.setModel(searchModel);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Item item : inventory) {
            tableModel.addRow(new Object[]{item.id, item.name, item.quantity, item.price});
        }
        table.setModel(tableModel);
    }

    private void clearFields() {
        idField.setText("");
        nameField.setText("");
        qtyField.setText("");
        priceField.setText("");
    }

    private void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
            for (Item item : inventory) {
                pw.println(item.id + "," + item.name + "," + item.quantity + "," + item.price);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving to file.");
        }
    }

    private void loadFromFile() {
        File file = new File(fileName);
        if (!file.exists()) return;
        try (Scanner fileScanner = new Scanner(file)) {
            while (fileScanner.hasNextLine()) {
                String[] parts = fileScanner.nextLine().split(",");
                if (parts.length == 4) {
                    inventory.add(new Item(parts[0], parts[1],
                            Integer.parseInt(parts[2]),
                            Double.parseDouble(parts[3])));
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading file.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(InventoryGUI::new);
    }
}