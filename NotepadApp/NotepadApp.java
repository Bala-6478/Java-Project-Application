import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.io.*;
import java.awt.Desktop;

public class NotepadApp extends JFrame {

    private JTextArea textArea;
    private JLabel statusBar;
    private JFileChooser fileChooser;
    private boolean darkMode = false;
    private File currentFile = null;

    public NotepadApp() {
        setTitle("Advanced Notepad IDE");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // TEXT AREA
        textArea = new JTextArea();
        textArea.setFont(new Font("Consolas", Font.PLAIN, 16));
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        // STATUS BAR
        statusBar = new JLabel("Words: 0 | Characters: 0");
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusBar, BorderLayout.SOUTH);

        // FILE CHOOSER
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Supported Files (*.txt, *.java, *.html, *.log)",
                "txt", "java", "html", "log"));

        // MENU
        setJMenuBar(createMenuBar());

        // MODERN TEXT LISTENER (NO WARNING)
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateStatusBar(); }
            public void removeUpdate(DocumentEvent e) { updateStatusBar(); }
            public void changedUpdate(DocumentEvent e) { updateStatusBar(); }
        });

        setVisible(true);
    }

    // MENU BAR
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // FILE MENU
        JMenu file = new JMenu("File");

        JMenuItem newFile = new JMenuItem("New");
        JMenuItem open = new JMenuItem("Open");
        JMenuItem save = new JMenuItem("Save");
        JMenuItem saveAs = new JMenuItem("Save As");
        JMenuItem exit = new JMenuItem("Exit");

        newFile.setAccelerator(KeyStroke.getKeyStroke("control N"));
        open.setAccelerator(KeyStroke.getKeyStroke("control O"));
        save.setAccelerator(KeyStroke.getKeyStroke("control S"));

        newFile.addActionListener(e -> newFile());
        open.addActionListener(e -> openFile());
        save.addActionListener(e -> saveFile());
        saveAs.addActionListener(e -> saveFileAs());
        exit.addActionListener(e -> System.exit(0));

        file.add(newFile);
        file.add(open);
        file.add(save);
        file.add(saveAs);
        file.addSeparator();
        file.add(exit);

        // EDIT MENU
        JMenu edit = new JMenu("Edit");

        JMenuItem cut = new JMenuItem("Cut");
        JMenuItem copy = new JMenuItem("Copy");
        JMenuItem paste = new JMenuItem("Paste");
        JMenuItem selectAll = new JMenuItem("Select All");

        cut.setAccelerator(KeyStroke.getKeyStroke("control X"));
        copy.setAccelerator(KeyStroke.getKeyStroke("control C"));
        paste.setAccelerator(KeyStroke.getKeyStroke("control V"));
        selectAll.setAccelerator(KeyStroke.getKeyStroke("control A"));

        cut.addActionListener(e -> textArea.cut());
        copy.addActionListener(e -> textArea.copy());
        paste.addActionListener(e -> textArea.paste());
        selectAll.addActionListener(e -> textArea.selectAll());

        edit.add(cut);
        edit.add(copy);
        edit.add(paste);
        edit.addSeparator();
        edit.add(selectAll);

        // VIEW MENU
        JMenu view = new JMenu("View");

        JMenuItem fontSize = new JMenuItem("Font Size");
        JMenuItem fontFamily = new JMenuItem("Font Family");
        JMenuItem dark = new JMenuItem("Toggle Dark Mode");

        fontSize.addActionListener(e -> changeFontSize());
        fontFamily.addActionListener(e -> changeFontFamily());
        dark.addActionListener(e -> toggleDarkMode());

        view.add(fontSize);
        view.add(fontFamily);
        view.add(dark);

        // RUN MENU
        JMenu runMenu = new JMenu("Run");

        JMenuItem runFile = new JMenuItem("Run File");
        runFile.setAccelerator(KeyStroke.getKeyStroke("control R"));

        runFile.addActionListener(e -> runCurrentFile());

        runMenu.add(runFile);

        // ADD MENUS
        menuBar.add(file);
        menuBar.add(edit);
        menuBar.add(view);
        menuBar.add(runMenu);

        return menuBar;
    }

    // FILE METHODS
    private void newFile() {
        textArea.setText("");
        currentFile = null;
        setTitle("New File");
    }

    private void openFile() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(currentFile))) {
                textArea.read(br, null);
                setTitle(currentFile.getName());
            } catch (Exception e) {
                showError("Error opening file");
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
            setTitle(file.getName());
        } catch (Exception e) {
            showError("Error saving file");
        }
    }

    // RUN FEATURE (MODERN FIX)
    private void runCurrentFile() {
        if (currentFile == null) {
            showError("Please save file first!");
            return;
        }

        try {
            String fileName = currentFile.getName();

            if (fileName.endsWith(".html")) {
                Desktop.getDesktop().browse(currentFile.toURI());
            }

            else if (fileName.endsWith(".java")) {
                String path = currentFile.getParent();
                String name = fileName.replace(".java", "");

                ProcessBuilder compile = new ProcessBuilder("javac", fileName);
                compile.directory(new File(path));
                compile.start().waitFor();

                ProcessBuilder run = new ProcessBuilder("java", name);
                run.directory(new File(path));

                Process process = run.start();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));

                String line, output = "";
                while ((line = reader.readLine()) != null) {
                    output += line + "\n";
                }

                JOptionPane.showMessageDialog(this,
                        output.isEmpty() ? "Program executed!" : output);
            }

            else {
                Desktop.getDesktop().open(currentFile);
            }

        } catch (Exception e) {
            showError("Error running file");
        }
    }

    // UI METHODS
    private void updateStatusBar() {
        String text = textArea.getText();
        int chars = text.length();
        int words = text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;

        statusBar.setText("Words: " + words + " | Characters: " + chars);
    }

    private void changeFontSize() {
        String input = JOptionPane.showInputDialog(this, "Enter Font Size:");
        try {
            int size = Integer.parseInt(input);
            textArea.setFont(new Font(textArea.getFont().getFamily(), Font.PLAIN, size));
        } catch (Exception e) {
            showError("Invalid size");
        }
    }

    private void changeFontFamily() {
        String font = JOptionPane.showInputDialog(this, "Enter Font Name:");
        if (font != null) {
            textArea.setFont(new Font(font, Font.PLAIN, textArea.getFont().getSize()));
        }
    }

    private void toggleDarkMode() {
        if (!darkMode) {
            textArea.setBackground(Color.BLACK);
            textArea.setForeground(Color.WHITE);
            statusBar.setBackground(Color.DARK_GRAY);
            statusBar.setForeground(Color.WHITE);
        } else {
            textArea.setBackground(Color.WHITE);
            textArea.setForeground(Color.BLACK);
            statusBar.setBackground(null);
            statusBar.setForeground(Color.BLACK);
        }
        darkMode = !darkMode;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NotepadApp::new);
    }
}