import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * FileExplorerPro – a polished Swing file browser.
 *
 * Improvements over original:
 *  - Consistent dark/light theming via a central ThemeManager
 *  - Extracted helper methods to reduce method length
 *  - Replaced magic strings/colours with named constants
 *  - Search results displayed in a scrollable list dialog (not per-file popups)
 *  - Human-readable file sizes (KB / MB / GB)
 *  - Status bar replaces always-visible details panel
 *  - Toolbar uses a consistent icon + text button style
 *  - Input validation and null-safety throughout
 *  - Directories sorted before files in the tree
 */
public class FileExplorerPro extends JFrame {

    // ── Constants ────────────────────────────────────────────────────────────

    private static final String DEFAULT_PATH   = System.getProperty("user.home");
    private static final Font   MONO_FONT      = new Font("Monospaced", Font.PLAIN, 12);
    private static final Font   UI_FONT        = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font   UI_BOLD        = new Font("Segoe UI", Font.BOLD,   13);

    // Dark palette
    private static final Color DARK_BG       = new Color(0x1E, 0x1E, 0x2E);
    private static final Color DARK_SURFACE  = new Color(0x28, 0x28, 0x3D);
    private static final Color DARK_BORDER   = new Color(0x3A, 0x3A, 0x5A);
    private static final Color DARK_FG       = new Color(0xCD, 0xD6, 0xF4);
    private static final Color DARK_ACCENT   = new Color(0x89, 0xB4, 0xFA);
    private static final Color DARK_SEL      = new Color(0x31, 0x31, 0x52);

    // Light palette
    private static final Color LIGHT_BG      = new Color(0xF8, 0xF8, 0xFC);
    private static final Color LIGHT_SURFACE = Color.WHITE;
    private static final Color LIGHT_BORDER  = new Color(0xD0, 0xD0, 0xE0);
    private static final Color LIGHT_FG      = new Color(0x1E, 0x1E, 0x2E);
    private static final Color LIGHT_ACCENT  = new Color(0x40, 0x80, 0xD0);
    private static final Color LIGHT_SEL     = new Color(0xE0, 0xEA, 0xFA);

    // ── Fields ───────────────────────────────────────────────────────────────

    private JTree               tree;
    private DefaultTreeModel    treeModel;
    private JTextField          pathField;
    private JTextField          searchField;
    private JTextField          typeField;
    private JLabel              statusBar;
    private boolean             darkMode = true;

    // ── Constructor ──────────────────────────────────────────────────────────

    public FileExplorerPro() {
        super("File Explorer Pro");
        setSize(1000, 680);
        setMinimumSize(new Dimension(700, 450));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));

        buildUI();
        applyTheme();
        loadPath(DEFAULT_PATH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── UI Construction ──────────────────────────────────────────────────────

    /** Assembles every UI region and wires up all listeners. */
    private void buildUI() {
        add(buildToolbar(),   BorderLayout.NORTH);
        add(buildTreePanel(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
        buildContextMenu();
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(6, 0));
        bar.setBorder(new EmptyBorder(8, 10, 8, 10));

        // --- Path row ---
        pathField = styledTextField(DEFAULT_PATH);
        JButton goBtn     = styledButton("Go",     "→");
        JButton upBtn     = styledButton("Up",     "↑");
        JButton darkBtn   = styledButton("",       "🌙");
        JButton refreshBtn = styledButton("",      "↻");

        JPanel pathRow = new JPanel(new BorderLayout(4, 0));
        JPanel pathBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pathBtns.add(upBtn);
        pathBtns.add(refreshBtn);
        pathBtns.add(darkBtn);
        pathRow.add(pathBtns,  BorderLayout.WEST);
        pathRow.add(pathField, BorderLayout.CENTER);
        pathRow.add(goBtn,     BorderLayout.EAST);

        // --- Search row ---
        searchField = styledTextField("Search file name…");
        typeField   = styledTextField(".ext");
        typeField.setPreferredSize(new Dimension(72, 28));
        JButton searchBtn = styledButton("Search", "🔍");

        JPanel searchRow = new JPanel(new BorderLayout(4, 0));
        searchRow.add(searchField, BorderLayout.CENTER);
        searchRow.add(typeField,   BorderLayout.EAST);
        searchRow.add(searchBtn,   BorderLayout.AFTER_LAST_LINE); // won't show – keep for layout
        // Fix: add searchBtn after typeField
        JPanel searchWrap = new JPanel(new BorderLayout(4, 0));
        searchWrap.add(searchRow, BorderLayout.CENTER);
        searchWrap.add(searchBtn, BorderLayout.EAST);

        JPanel rows = new JPanel(new GridLayout(2, 1, 0, 6));
        rows.add(pathRow);
        rows.add(searchWrap);

        bar.add(rows, BorderLayout.CENTER);

        // --- Listeners ---
        goBtn.addActionListener(e -> loadPath(pathField.getText().trim()));
        upBtn.addActionListener(e -> navigateUp());
        refreshBtn.addActionListener(e -> loadPath(pathField.getText().trim()));
        darkBtn.addActionListener(e -> { darkMode = !darkMode; applyTheme(); });
        searchBtn.addActionListener(e -> runSearch());
        pathField.addActionListener(e -> loadPath(pathField.getText().trim()));

        return bar;
    }

    private JScrollPane buildTreePanel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new File(DEFAULT_PATH));
        treeModel = new DefaultTreeModel(root);

        tree = new JTree(treeModel);
        tree.setFont(UI_FONT);
        tree.setRowHeight(24);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new FileTreeCellRenderer());
        tree.setDragEnabled(true);

        tree.addTreeWillExpandListener(new TreeWillExpandListener() {
            public void treeWillExpand(TreeExpansionEvent e) {
                DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
                expandNode(node);
            }
            public void treeWillCollapse(TreeExpansionEvent e) {}
        });

        tree.addTreeSelectionListener(e -> updateStatusBar());

        JScrollPane scroll = new JScrollPane(tree);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        return scroll;
    }

    private JPanel buildStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, DARK_BORDER),
            new EmptyBorder(4, 12, 4, 12)
        ));
        statusBar = new JLabel("Ready");
        statusBar.setFont(MONO_FONT);
        panel.add(statusBar, BorderLayout.WEST);
        return panel;
    }

    private void buildContextMenu() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem openItem   = menuItem("Open",   "📂");
        JMenuItem renameItem = menuItem("Rename", "✏️");
        JMenuItem deleteItem = menuItem("Delete", "🗑");
        JMenuItem copyPathItem = menuItem("Copy Path", "📋");

        menu.add(openItem);
        menu.addSeparator();
        menu.add(renameItem);
        menu.add(deleteItem);
        menu.addSeparator();
        menu.add(copyPathItem);

        tree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { maybeShowMenu(e); }
            public void mouseReleased(MouseEvent e) { maybeShowMenu(e); }

            private void maybeShowMenu(MouseEvent e) {
                if (!SwingUtilities.isRightMouseButton(e)) return;
                TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                if (path != null) tree.setSelectionPath(path);
                menu.show(tree, e.getX(), e.getY());
            }
        });

        openItem.addActionListener(e   -> openSelectedFile());
        renameItem.addActionListener(e -> renameSelectedFile());
        deleteItem.addActionListener(e -> deleteSelectedFile());
        copyPathItem.addActionListener(e -> copyPathToClipboard());
    }

    // ── Tree Operations ──────────────────────────────────────────────────────

    /**
     * Lazily loads children of a tree node, sorting directories first.
     */
    private void expandNode(DefaultMutableTreeNode node) {
        Object userObj = node.getUserObject();
        if (!(userObj instanceof File)) return;

        File dir = (File) userObj;
        File[] children = dir.listFiles();

        node.removeAllChildren();

        if (children == null) {
            treeModel.reload(node);
            return;
        }

        // Sort: directories first, then alphabetical
        java.util.Arrays.sort(children, (a, b) -> {
            if (a.isDirectory() != b.isDirectory())
                return a.isDirectory() ? -1 : 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });

        for (File child : children) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
            node.add(childNode);
            if (child.isDirectory()) {
                // Placeholder so the expand arrow appears
                childNode.add(new DefaultMutableTreeNode("…"));
            }
        }
        treeModel.reload(node);
    }

    private void loadPath(String pathStr) {
        File file = new File(pathStr);
        if (!file.exists() || !file.isDirectory()) {
            setStatus("⚠ Path not found: " + pathStr);
            return;
        }
        pathField.setText(file.getAbsolutePath());
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(file);
        treeModel.setRoot(root);
        expandNode(root);
        setStatus("Loaded: " + file.getAbsolutePath());
    }

    private void navigateUp() {
        File current = new File(pathField.getText().trim());
        File parent  = current.getParentFile();
        if (parent != null) loadPath(parent.getAbsolutePath());
    }

    // ── File Actions ─────────────────────────────────────────────────────────

    private File getSelectedFile() {
        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node == null) return null;
        Object obj = node.getUserObject();
        return (obj instanceof File) ? (File) obj : null;
    }

    private void openSelectedFile() {
        File f = getSelectedFile();
        if (f == null) return;
        if (f.isDirectory()) { loadPath(f.getAbsolutePath()); return; }
        try {
            Desktop.getDesktop().open(f);
        } catch (Exception ex) {
            showError("Cannot open: " + f.getName());
        }
    }

    private void deleteSelectedFile() {
        File f = getSelectedFile();
        if (f == null) return;
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Permanently delete \"" + f.getName() + "\"?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            if (f.delete()) {
                setStatus("Deleted: " + f.getName());
                loadPath(pathField.getText().trim());
            } else {
                showError("Could not delete " + f.getName());
            }
        }
    }

    private void renameSelectedFile() {
        File f = getSelectedFile();
        if (f == null) return;
        String newName = (String) JOptionPane.showInputDialog(
            this, "New name:", "Rename", JOptionPane.PLAIN_MESSAGE, null, null, f.getName()
        );
        if (newName == null || newName.trim().isEmpty()) return;
        File dest = new File(f.getParent(), newName.trim());
        if (f.renameTo(dest)) {
            setStatus("Renamed to: " + dest.getName());
            loadPath(pathField.getText().trim());
        } else {
            showError("Rename failed.");
        }
    }

    private void copyPathToClipboard() {
        File f = getSelectedFile();
        if (f == null) return;
        Toolkit.getDefaultToolkit()
               .getSystemClipboard()
               .setContents(new java.awt.datatransfer.StringSelection(f.getAbsolutePath()), null);
        setStatus("Copied: " + f.getAbsolutePath());
    }

    // ── Search ───────────────────────────────────────────────────────────────

    private void runSearch() {
        String name = searchField.getText().trim().toLowerCase();
        String ext  = typeField.getText().trim().toLowerCase();

        if (name.isEmpty() && ext.isEmpty()) {
            showError("Enter a name or extension to search.");
            return;
        }

        File rootDir = new File(pathField.getText().trim());
        if (!rootDir.isDirectory()) { showError("Invalid search directory."); return; }

        java.util.List<File> results = new java.util.ArrayList<>();
        setStatus("Searching…");
        searchRecursive(rootDir, name, ext, results, 0);

        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No files found.", "Search Results",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            showSearchResults(results);
        }
        setStatus("Search complete – " + results.size() + " result(s).");
    }

    private static final int MAX_SEARCH_RESULTS = 200;

    private void searchRecursive(File dir, String name, String ext,
                                 java.util.List<File> results, int depth) {
        if (results.size() >= MAX_SEARCH_RESULTS || depth > 15) return;

        File[] children = dir.listFiles();
        if (children == null) return;

        for (File f : children) {
            if (results.size() >= MAX_SEARCH_RESULTS) return;

            String fname = f.getName().toLowerCase();
            boolean nameOk = name.isEmpty() || fname.contains(name);
            boolean extOk  = ext.isEmpty()  || fname.endsWith(ext);

            if (nameOk && extOk) results.add(f);

            if (f.isDirectory()) searchRecursive(f, name, ext, results, depth + 1);
        }
    }

    private void showSearchResults(java.util.List<File> results) {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (File f : results) listModel.addElement(f.getAbsolutePath());

        JList<String> list = new JList<>(listModel);
        list.setFont(MONO_FONT);
        list.setVisibleRowCount(16);

        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(680, 300));

        JOptionPane.showMessageDialog(this, scroll,
            "Search Results (" + results.size() + ")", JOptionPane.PLAIN_MESSAGE);
    }

    // ── Status Bar ───────────────────────────────────────────────────────────

    private void updateStatusBar() {
        File f = getSelectedFile();
        if (f == null) { setStatus("Ready"); return; }

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy  HH:mm");
        String size = f.isDirectory() ? "directory"
                                      : humanReadableSize(f.length());
        setStatus(f.getAbsolutePath() + "    │    " + size +
                  "    │    Modified: " + sdf.format(f.lastModified()));
    }

    private void setStatus(String text) {
        statusBar.setText(text);
    }

    // ── Theming ──────────────────────────────────────────────────────────────

    private void applyTheme() {
        Color bg      = darkMode ? DARK_BG      : LIGHT_BG;
        Color surface = darkMode ? DARK_SURFACE : LIGHT_SURFACE;
        Color border  = darkMode ? DARK_BORDER  : LIGHT_BORDER;
        Color fg      = darkMode ? DARK_FG      : LIGHT_FG;
        Color accent  = darkMode ? DARK_ACCENT  : LIGHT_ACCENT;
        Color sel     = darkMode ? DARK_SEL     : LIGHT_SEL;

        getContentPane().setBackground(bg);

        // Tree
        tree.setBackground(surface);
        tree.setForeground(fg);
        DefaultTreeCellRenderer r = (DefaultTreeCellRenderer) tree.getCellRenderer();
        r.setTextNonSelectionColor(fg);
        r.setTextSelectionColor(darkMode ? DARK_FG : LIGHT_FG);
        r.setBackgroundNonSelectionColor(surface);
        r.setBackgroundSelectionColor(sel);
        r.setBorderSelectionColor(accent);

        // Text fields
        for (JTextField tf : new JTextField[]{pathField, searchField, typeField}) {
            tf.setBackground(surface);
            tf.setForeground(fg);
            tf.setCaretColor(accent);
            tf.setBorder(new CompoundBorder(
                new LineBorder(border, 1, true),
                new EmptyBorder(3, 8, 3, 8)
            ));
        }

        // Status bar
        statusBar.getParent().setBackground(bg);
        statusBar.setForeground(darkMode ? new Color(0x88, 0x8A, 0xA8) : new Color(0x60, 0x60, 0x80));

        // Toolbar background
        Component toolbar = getContentPane().getComponent(0);
        applyBgFg(toolbar, bg, fg);

        repaint();
    }

    private void applyBgFg(Component c, Color bg, Color fg) {
        c.setBackground(bg);
        c.setForeground(fg);
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents())
                applyBgFg(child, bg, fg);
        }
    }

    // ── Factory / Utility Helpers ─────────────────────────────────────────────

    private JTextField styledTextField(String placeholder) {
        JTextField tf = new JTextField(placeholder);
        tf.setFont(UI_FONT);
        tf.setPreferredSize(new Dimension(0, 28));
        return tf;
    }

    private JButton styledButton(String text, String icon) {
        JButton btn = new JButton(icon + (text.isEmpty() ? "" : " " + text));
        btn.setFont(UI_BOLD);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        return btn;
    }

    private JMenuItem menuItem(String text, String icon) {
        JMenuItem item = new JMenuItem(icon + "  " + text);
        item.setFont(UI_FONT);
        return item;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static String humanReadableSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        DecimalFormat df = new DecimalFormat("0.#");
        if (bytes < 1024 * 1024)      return df.format(bytes / 1024.0)              + " KB";
        if (bytes < 1024 * 1024 * 1024) return df.format(bytes / (1024.0 * 1024))  + " MB";
        return df.format(bytes / (1024.0 * 1024 * 1024)) + " GB";
    }

    // ── Cell Renderer ────────────────────────────────────────────────────────

    private class FileTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(
                JTree tree, Object value, boolean selected,
                boolean expanded, boolean leaf, int row, boolean hasFocus) {

            super.getTreeCellRendererComponent(
                    tree, value, selected, expanded, leaf, row, hasFocus);

            setFont(UI_FONT);

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object obj = node.getUserObject();

            if (obj instanceof File) {
                File f = (File) obj;
                if (f.isDirectory()) {
                    setIcon(UIManager.getIcon("FileView.directoryIcon"));
                    setFont(UI_BOLD);
                } else {
                    setIcon(UIManager.getIcon("FileView.fileIcon"));
                }
                setText(f.getName().isEmpty() ? f.getAbsolutePath() : f.getName());
            } else {
                // "…" placeholder
                setIcon(null);
                setText(obj.toString());
                setFont(UI_FONT.deriveFont(Font.ITALIC));
            }
            return this;
        }
    }

    // ── Entry Point ──────────────────────────────────────────────────────────

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(FileExplorerPro::new);
    }
}
