import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;

public class FileExplorerPro extends JFrame {

    private JTree tree;
    private DefaultTreeModel model;
    private JTextField pathField, searchField, typeField;
    private JTextArea detailsArea;
    private boolean darkMode = false;

    public FileExplorerPro() {
        setTitle("File Explorer Pro");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== TOP PANEL =====
        JPanel topPanel = new JPanel(new BorderLayout());

        pathField = new JTextField("C:/");
        JButton goBtn = new JButton("Go");

        JPanel leftTop = new JPanel(new BorderLayout());
        leftTop.add(pathField, BorderLayout.CENTER);
        leftTop.add(goBtn, BorderLayout.EAST);

        // SEARCH PANEL
        searchField = new JTextField();
        typeField = new JTextField();
        typeField.setPreferredSize(new Dimension(80, 25));
        typeField.setToolTipText("Type (.txt, .java)");

        JButton searchBtn = new JButton("Search");

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(typeField, BorderLayout.EAST);

        JPanel rightTop = new JPanel(new BorderLayout());
        rightTop.add(searchPanel, BorderLayout.CENTER);
        rightTop.add(searchBtn, BorderLayout.EAST);

        JButton darkBtn = new JButton("🌙");

        topPanel.add(darkBtn, BorderLayout.WEST);
        topPanel.add(leftTop, BorderLayout.CENTER);
        topPanel.add(rightTop, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // ===== TREE =====
        File rootFile = new File("C:/");
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootFile);
        model = new DefaultTreeModel(root);

        tree = new JTree(model);
        tree.setCellRenderer(new FileRenderer());

        tree.addTreeWillExpandListener(new TreeWillExpandListener() {
            public void treeWillExpand(TreeExpansionEvent event) {
                DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                loadChildren(node);
            }
            public void treeWillCollapse(TreeExpansionEvent event) {}
        });

        add(new JScrollPane(tree), BorderLayout.CENTER);

        // ===== DETAILS =====
        detailsArea = new JTextArea(5, 20);
        detailsArea.setEditable(false);
        add(new JScrollPane(detailsArea), BorderLayout.SOUTH);

        tree.addTreeSelectionListener(e -> showDetails());

        // ===== RIGHT CLICK MENU =====
        JPopupMenu menu = new JPopupMenu();
        JMenuItem open = new JMenuItem("Open");
        JMenuItem delete = new JMenuItem("Delete");
        JMenuItem rename = new JMenuItem("Rename");

        menu.add(open);
        menu.add(delete);
        menu.add(rename);

        tree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    tree.setSelectionPath(path);
                    menu.show(tree, e.getX(), e.getY());
                }
            }
        });

        open.addActionListener(e -> openFile());
        delete.addActionListener(e -> deleteFile());
        rename.addActionListener(e -> renameFile());

        // ===== BUTTON ACTIONS =====
        goBtn.addActionListener(e -> loadPath());

        searchBtn.addActionListener(e -> {
            String name = searchField.getText().toLowerCase();
            String type = typeField.getText().toLowerCase();
            File rootDir = new File(pathField.getText());

            searchRecursive(rootDir, name, type);
        });

        darkBtn.addActionListener(e -> toggleDarkMode());

        tree.setDragEnabled(true);

        setVisible(true);
    }

    // ===== LOAD CHILDREN (LAZY) =====
    private void loadChildren(DefaultMutableTreeNode node) {
        node.removeAllChildren();
        File file = (File) node.getUserObject();
        File[] files = file.listFiles();

        if (files == null) return;

        for (File f : files) {
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(f);
            node.add(child);

            if (f.isDirectory()) {
                child.add(new DefaultMutableTreeNode("Loading..."));
            }
        }
        model.reload(node);
    }

    private File getSelectedFile() {
        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node == null) return null;
        return (File) node.getUserObject();
    }

    // ===== FILE ACTIONS =====
    private void openFile() {
        try {
            File f = getSelectedFile();
            if (f != null && f.isFile()) {
                Desktop.getDesktop().open(f);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Cannot open file");
        }
    }

    private void deleteFile() {
        File f = getSelectedFile();
        if (f != null) {
            int confirm = JOptionPane.showConfirmDialog(this, "Delete?");
            if (confirm == 0) {
                f.delete();
                refresh();
            }
        }
    }

    private void renameFile() {
        File f = getSelectedFile();
        if (f != null) {
            String name = JOptionPane.showInputDialog("New Name:");
            if (name != null) {
                File newFile = new File(f.getParent() + "/" + name);
                f.renameTo(newFile);
                refresh();
            }
        }
    }

    // ===== LOAD PATH =====
    private void loadPath() {
        File file = new File(pathField.getText());
        if (file.exists()) {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(file);
            model.setRoot(root);
            loadChildren(root);
        }
    }

    // ===== SEARCH =====
    private void searchRecursive(File file, String name, String type) {

        boolean nameMatch = file.getName().toLowerCase().contains(name);
        boolean typeMatch = type.isEmpty() || file.getName().toLowerCase().endsWith(type);

        if (nameMatch && typeMatch) {
            JOptionPane.showMessageDialog(this, "Found: " + file.getAbsolutePath());
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    searchRecursive(f, name, type);
                }
            }
        }
    }

    // ===== DETAILS =====
    private void showDetails() {
        File f = getSelectedFile();
        if (f == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        detailsArea.setText(
                "Name: " + f.getName() +
                "\nPath: " + f.getAbsolutePath() +
                "\nSize: " + f.length() + " bytes" +
                "\nModified: " + sdf.format(f.lastModified())
        );
    }

    // ===== DARK MODE =====
    private void toggleDarkMode() {
        darkMode = !darkMode;

        Color bg = darkMode ? Color.DARK_GRAY : Color.WHITE;
        Color fg = darkMode ? Color.WHITE : Color.BLACK;

        tree.setBackground(bg);
        tree.setForeground(fg);
        detailsArea.setBackground(bg);
        detailsArea.setForeground(fg);
    }

    private void refresh() {
        loadPath();
    }

    // ===== ICON RENDERER =====
    class FileRenderer extends DefaultTreeCellRenderer {
        public Component getTreeCellRendererComponent(
                JTree tree, Object value, boolean sel,
                boolean exp, boolean leaf, int row, boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object obj = node.getUserObject();

            if (obj instanceof File) {
                File f = (File) obj;

                if (f.isDirectory())
                    setIcon(UIManager.getIcon("FileView.directoryIcon"));
                else
                    setIcon(UIManager.getIcon("FileView.fileIcon"));

                setText(f.getName());
            }
            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FileExplorerPro::new);
    }
}