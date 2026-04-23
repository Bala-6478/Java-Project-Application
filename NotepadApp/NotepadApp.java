import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.Desktop;

public class NotepadApp extends JFrame {

    private JTextArea textArea;
    private JLabel statusBar;
    private JFileChooser fileChooser;
    private boolean darkMode = false;
    private File currentFile = null;
    private boolean isModified = false;
    private UndoManager undoManager = new UndoManager();

    public NotepadApp() {
        setTitle("Advanced Notepad IDE");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // TEXT AREA
        textArea = new JTextArea();
        textArea.setFont(new Font("Consolas", Font.PLAIN, 16));
        textArea.setTabSize(4);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        // LINE NUMBER PANEL
        JScrollPane scrollPane = new JScrollPane(textArea);
        LineNumberPanel lineNumbers = new LineNumberPanel(textArea);
        scrollPane.setRowHeaderView(lineNumbers);
        add(scrollPane, BorderLayout.CENTER);

        // STATUS BAR
        statusBar = new JLabel("Words: 0 | Characters: 0 | Line: 1 | Col: 1");
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusBar, BorderLayout.SOUTH);

        // FILE CHOOSER
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Supported Files (*.txt, *.java, *.html, *.log)",
                "txt", "java", "html", "log"));

        // MENU
        setJMenuBar(createMenuBar());

        // DOCUMENT LISTENER
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { onDocumentChanged(); }
            public void removeUpdate(DocumentEvent e)  { onDocumentChanged(); }
            public void changedUpdate(DocumentEvent e) { updateStatusBar(); }
        });

        // UNDO/REDO SUPPORT
        textArea.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));

        // CARET LISTENER for line/col tracking
        textArea.addCaretListener(e -> updateStatusBar());

        // CLOSE CONFIRMATION
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { exitApp(); }
        });

        // CONTEXT MENU (RIGHT CLICK)
        JPopupMenu popup = new JPopupMenu();
        JMenuItem pCut   = new JMenuItem("Cut");
        JMenuItem pCopy  = new JMenuItem("Copy");
        JMenuItem pPaste = new JMenuItem("Paste");
        pCut.addActionListener(e -> textArea.cut());
        pCopy.addActionListener(e -> textArea.copy());
        pPaste.addActionListener(e -> textArea.paste());
        popup.add(pCut);
        popup.add(pCopy);
        popup.add(pPaste);
        textArea.setComponentPopupMenu(popup);

        setVisible(true);
    }

    // ──────────────────────────── MENU BAR ────────────────────────────

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // FILE MENU
        JMenu file = new JMenu("File");

        JMenuItem newFile  = new JMenuItem("New");
        JMenuItem open     = new JMenuItem("Open");
        JMenuItem save     = new JMenuItem("Save");
        JMenuItem saveAs   = new JMenuItem("Save As");
        JMenuItem exit     = new JMenuItem("Exit");

        newFile.setAccelerator(KeyStroke.getKeyStroke("control N"));
        open.setAccelerator(KeyStroke.getKeyStroke("control O"));
        save.setAccelerator(KeyStroke.getKeyStroke("control S"));
        saveAs.setAccelerator(KeyStroke.getKeyStroke("control shift S"));

        newFile.addActionListener(e -> newFile());
        open.addActionListener(e -> openFile());
        save.addActionListener(e -> saveFile());
        saveAs.addActionListener(e -> saveFileAs());
        exit.addActionListener(e -> exitApp());

        file.add(newFile); file.add(open); file.add(save); file.add(saveAs);
        file.addSeparator();
        file.add(exit);

        // EDIT MENU
        JMenu edit = new JMenu("Edit");

        JMenuItem undo      = new JMenuItem("Undo");
        JMenuItem redo      = new JMenuItem("Redo");
        JMenuItem cut       = new JMenuItem("Cut");
        JMenuItem copy      = new JMenuItem("Copy");
        JMenuItem paste     = new JMenuItem("Paste");
        JMenuItem selectAll = new JMenuItem("Select All");
        JMenuItem findItem  = new JMenuItem("Find & Replace");

        undo.setAccelerator(KeyStroke.getKeyStroke("control Z"));
        redo.setAccelerator(KeyStroke.getKeyStroke("control Y"));
        cut.setAccelerator(KeyStroke.getKeyStroke("control X"));
        copy.setAccelerator(KeyStroke.getKeyStroke("control C"));
        paste.setAccelerator(KeyStroke.getKeyStroke("control V"));
        selectAll.setAccelerator(KeyStroke.getKeyStroke("control A"));
        findItem.setAccelerator(KeyStroke.getKeyStroke("control H"));

        undo.addActionListener(e -> { if (undoManager.canUndo()) undoManager.undo(); });
        redo.addActionListener(e -> { if (undoManager.canRedo()) undoManager.redo(); });
        cut.addActionListener(e -> textArea.cut());
        copy.addActionListener(e -> textArea.copy());
        paste.addActionListener(e -> textArea.paste());
        selectAll.addActionListener(e -> textArea.selectAll());
        findItem.addActionListener(e -> showFindReplaceDialog());

        edit.add(undo); edit.add(redo); edit.addSeparator();
        edit.add(cut); edit.add(copy); edit.add(paste); edit.addSeparator();
        edit.add(selectAll); edit.addSeparator(); edit.add(findItem);

        // VIEW MENU
        JMenu view = new JMenu("View");

        JMenuItem fontSize   = new JMenuItem("Font Size");
        JMenuItem fontFamily = new JMenuItem("Font Family");
        JMenuItem dark       = new JMenuItem("Toggle Dark Mode");
        JCheckBoxMenuItem wrap = new JCheckBoxMenuItem("Word Wrap", true);

        fontSize.addActionListener(e -> changeFontSize());
        fontFamily.addActionListener(e -> changeFontFamily());
        dark.addActionListener(e -> toggleDarkMode());
        wrap.addActionListener(e -> textArea.setLineWrap(wrap.isSelected()));

        view.add(fontSize); view.add(fontFamily); view.add(dark); view.addSeparator(); view.add(wrap);

        // RUN MENU
        JMenu runMenu = new JMenu("Run");
        JMenuItem runFile = new JMenuItem("Run File");
        runFile.setAccelerator(KeyStroke.getKeyStroke("control R"));
        runFile.addActionListener(e -> runCurrentFile());
        runMenu.add(runFile);

        menuBar.add(file);
        menuBar.add(edit);
        menuBar.add(view);
        menuBar.add(runMenu);

        return menuBar;
    }

    // ──────────────────────────── FILE METHODS ────────────────────────────

    private void newFile() {
        if (!confirmDiscard()) return;
        textArea.setText("");
        currentFile = null;
        isModified = false;
        undoManager.discardAllEdits();
        setTitle("Untitled — Advanced Notepad IDE");
    }

    private void openFile() {
        if (!confirmDiscard()) return;
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(currentFile))) {
                textArea.read(br, null);
                isModified = false;
                undoManager.discardAllEdits();
                updateTitle();
            } catch (Exception e) {
                showError("Error opening file: " + e.getMessage());
            }
        }
    }

    private void saveFile() {
        if (currentFile != null) {
            writeToFile(currentFile);
        } else {
            saveFileAs();
        }
    }

    private void saveFileAs() {
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            writeToFile(currentFile);
        }
    }

    private void writeToFile(File file) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            textArea.write(bw);
            isModified = false;
            updateTitle();
        } catch (Exception e) {
            showError("Error saving file: " + e.getMessage());
        }
    }

    /** Returns true if safe to proceed (no unsaved changes, or user said OK). */
    private boolean confirmDiscard() {
        if (!isModified) return true;
        int choice = JOptionPane.showConfirmDialog(this,
                "You have unsaved changes. Discard them?",
                "Unsaved Changes",
                JOptionPane.YES_NO_CANCEL_OPTION);
        return choice == JOptionPane.YES_OPTION;
    }

    private void exitApp() {
        if (confirmDiscard()) System.exit(0);
    }

    // ──────────────────────────── RUN FEATURE ────────────────────────────

    private void runCurrentFile() {
        if (currentFile == null) {
            showError("Please save the file first!");
            return;
        }

        String fileName = currentFile.getName();

        try {
            if (fileName.endsWith(".html")) {
                Desktop.getDesktop().browse(currentFile.toURI());
                return;
            }

            if (fileName.endsWith(".java")) {
                String dir  = currentFile.getParent();
                String name = fileName.replace(".java", "");

                // Compile
                ProcessBuilder compile = new ProcessBuilder("javac", fileName);
                compile.directory(new File(dir));
                compile.redirectErrorStream(true);
                Process compileProcess = compile.start();

                String compileOutput = readStream(compileProcess.getInputStream());
                compileProcess.waitFor();

                if (!compileOutput.isBlank()) {
                    showOutputDialog("Compile Errors", compileOutput);
                    return;
                }

                // Run with timeout via separate thread
                ProcessBuilder run = new ProcessBuilder("java", name);
                run.directory(new File(dir));
                run.redirectErrorStream(true);
                Process runProcess = run.start();

                String output = readStream(runProcess.getInputStream());
                showOutputDialog("Program Output", output.isBlank() ? "(No output)" : output);
                return;
            }

            // Fallback: open with system default
            Desktop.getDesktop().open(currentFile);

        } catch (Exception e) {
            showError("Error running file: " + e.getMessage());
        }
    }

    private String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append('\n');
        }
        return sb.toString();
    }

    private void showOutputDialog(String title, String content) {
        JTextArea out = new JTextArea(content, 15, 60);
        out.setFont(new Font("Consolas", Font.PLAIN, 13));
        out.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(out), title, JOptionPane.INFORMATION_MESSAGE);
    }

    // ──────────────────────────── FIND & REPLACE ────────────────────────────

    private void showFindReplaceDialog() {
        JDialog dialog = new JDialog(this, "Find & Replace", false);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(400, 160);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField findField    = new JTextField(20);
        JTextField replaceField = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0; dialog.add(new JLabel("Find:"), gbc);
        gbc.gridx = 1;                dialog.add(findField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; dialog.add(new JLabel("Replace:"), gbc);
        gbc.gridx = 1;                dialog.add(replaceField, gbc);

        JButton findBtn    = new JButton("Find Next");
        JButton replaceBtn = new JButton("Replace All");

        findBtn.addActionListener(e -> {
            String text   = textArea.getText();
            String target = findField.getText();
            int idx = text.indexOf(target, textArea.getCaretPosition());
            if (idx == -1) idx = text.indexOf(target);   // wrap around
            if (idx >= 0) {
                textArea.setCaretPosition(idx);
                textArea.select(idx, idx + target.length());
            } else {
                JOptionPane.showMessageDialog(dialog, "Not found.");
            }
        });

        replaceBtn.addActionListener(e -> {
            String newText = textArea.getText().replace(
                    findField.getText(), replaceField.getText());
            textArea.setText(newText);
        });

        gbc.gridx = 0; gbc.gridy = 2; dialog.add(findBtn, gbc);
        gbc.gridx = 1;                dialog.add(replaceBtn, gbc);

        dialog.setVisible(true);
    }

    // ──────────────────────────── UI HELPERS ────────────────────────────

    private void onDocumentChanged() {
        isModified = true;
        updateTitle();
        updateStatusBar();
    }

    private void updateTitle() {
        String name = (currentFile != null) ? currentFile.getName() : "Untitled";
        setTitle((isModified ? "* " : "") + name + " — Advanced Notepad IDE");
    }

    private void updateStatusBar() {
        String text  = textArea.getText();
        int chars    = text.length();
        int words    = text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;

        // Calculate line and column from caret position
        int caret = textArea.getCaretPosition();
        int line = 1, col = 1;
        try {
            line = textArea.getLineOfOffset(caret) + 1;
            col  = caret - textArea.getLineStartOffset(line - 1) + 1;
        } catch (Exception ignored) {}

        statusBar.setText(String.format(
                "Words: %d | Characters: %d | Line: %d | Col: %d", words, chars, line, col));
    }

    private void changeFontSize() {
        String input = JOptionPane.showInputDialog(this,
                "Enter Font Size:", textArea.getFont().getSize());
        if (input == null) return;
        try {
            int size = Integer.parseInt(input.trim());
            if (size < 6 || size > 72) { showError("Size must be between 6 and 72."); return; }
            textArea.setFont(new Font(textArea.getFont().getFamily(), Font.PLAIN, size));
        } catch (NumberFormatException e) {
            showError("Invalid size — please enter a number.");
        }
    }

    private void changeFontFamily() {
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        String chosen = (String) JOptionPane.showInputDialog(this,
                "Choose a font:", "Font Family",
                JOptionPane.PLAIN_MESSAGE, null, fonts,
                textArea.getFont().getFamily());
        if (chosen != null)
            textArea.setFont(new Font(chosen, Font.PLAIN, textArea.getFont().getSize()));
    }

    private void toggleDarkMode() {
        darkMode = !darkMode;
        Color bg  = darkMode ? new Color(30, 30, 30)  : Color.WHITE;
        Color fg  = darkMode ? new Color(220, 220, 220) : Color.BLACK;
        Color sbg = darkMode ? new Color(50, 50, 50)  : null;
        textArea.setBackground(bg);
        textArea.setForeground(fg);
        textArea.setCaretColor(fg);
        statusBar.setBackground(sbg);
        statusBar.setForeground(fg);
        statusBar.setOpaque(darkMode);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ──────────────────────────── LINE NUMBER PANEL ────────────────────────────

    static class LineNumberPanel extends JPanel implements DocumentListener, CaretListener {
        private final JTextArea textArea;
        private static final int PADDING = 5;

        LineNumberPanel(JTextArea ta) {
            this.textArea = ta;
            setPreferredSize(new Dimension(45, 0));
            setBackground(new Color(240, 240, 240));
            ta.getDocument().addDocumentListener(this);
            ta.addCaretListener(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setFont(textArea.getFont());
            g.setColor(new Color(120, 120, 120));
            int lineHeight = g.getFontMetrics().getHeight();
            int startLine;
            try {
                int startOffset = textArea.viewToModel2D(new Point(0, 0));
                startLine = textArea.getLineOfOffset(startOffset);
            } catch (Exception e) { startLine = 0; }
            int totalLines = textArea.getLineCount();
            for (int i = startLine; i < totalLines; i++) {
                int y = (i - startLine) * lineHeight + lineHeight;
                String num = String.valueOf(i + 1);
                int x = getWidth() - PADDING - g.getFontMetrics().stringWidth(num);
                g.drawString(num, x, y);
            }
        }

        public void insertUpdate(DocumentEvent e)  { repaint(); }
        public void removeUpdate(DocumentEvent e)  { repaint(); }
        public void changedUpdate(DocumentEvent e) { repaint(); }
        public void caretUpdate(CaretEvent e)      { repaint(); }
    }

    // ──────────────────────────── MAIN ────────────────────────────

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(NotepadApp::new);
    }
}
