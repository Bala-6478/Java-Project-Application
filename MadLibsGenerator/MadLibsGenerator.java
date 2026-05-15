import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MadLibsGenerator extends JFrame {

    // ── Story templates ───────────────────────────────────────────────────────
    private static final String[] TEMPLATES = {
        "One day, a {adjective} {noun} decided to {verb} all the way to {place}. "
            + "Everyone stopped and stared, completely astonished by the sight!",
        "The legendary {noun} of {place} was known for being incredibly {adjective}. "
            + "Locals would {verb} every morning just to catch a glimpse of it.",
        "A travel guide once wrote: \"If you visit {place}, be sure to {verb} near the famous "
            + "{adjective} {noun}. You will never forget the experience!\"",
        "Breaking news from {place}: a remarkably {adjective} {noun} was spotted trying to "
            + "{verb} downtown. Authorities have described the scene as 'unprecedented'.",
        "Scientists in {place} have discovered a {adjective} {noun} that can {verb} at twice "
            + "the speed of light. The Nobel committee is reportedly taking notes."
    };

    // ── Theme colour palettes [BG, CARD, TEXT, MUTED, FIELD_BG, FIELD_BORDER, DIVIDER, CLEAR_BTN] ─
    private static final Color[] LIGHT = {
        new Color(255, 252, 245), new Color(255, 255, 255), new Color(30,  30,  40),
        new Color(130, 130, 150), new Color(248, 248, 252), new Color(210, 210, 220),
        new Color(230, 230, 235), new Color(242, 242, 248),
    };
    private static final Color[] DARK = {
        new Color(15,  17,  26),  new Color(24,  27,  40),  new Color(220, 222, 235),
        new Color(100, 105, 130), new Color(30,  33,  50),  new Color(55,  60,  88),
        new Color(38,  42,  62),  new Color(38,  42,  62),
    };

    private static final Color ACCENT  = new Color(255, 111,  97); // coral
    private static final Color ACCENT2 = new Color( 72, 199, 142); // mint
    private static final Color ACCENT3 = new Color( 99, 149, 255); // periwinkle

    private static final Font FONT_TITLE = new Font("Georgia",   Font.BOLD,  25);
    private static final Font FONT_LABEL = new Font("SansSerif", Font.BOLD,  13);
    private static final Font FONT_FIELD = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font FONT_STORY = new Font("Georgia",   Font.PLAIN, 15);
    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);
    private static final Font FONT_BTN   = new Font("SansSerif", Font.BOLD,  13);

    // ── State ─────────────────────────────────────────────────────────────────
    private boolean darkMode = false;
    private int templateIndex = 0;
    private Color[] theme = LIGHT;

    // ── Input fields ──────────────────────────────────────────────────────────
    private final JTextField nounField      = new JTextField(22);
    private final JTextField verbField      = new JTextField(22);
    private final JTextField adjectiveField = new JTextField(22);
    private final JTextField placeField     = new JTextField(22);

    // ── Output & status ───────────────────────────────────────────────────────
    private final JTextArea     resultArea     = new JTextArea(4, 30);
    private final JLabel        charCountLabel = new JLabel("0 characters");
    private final JLabel        statusLabel    = new JLabel(" ");

    // ── Buttons ───────────────────────────────────────────────────────────────
    private final JButton       generateButton = new JButton("✨ Generate Story");
    private final JButton       clearButton    = new JButton("🗑  Clear");
    private final JButton       copyButton     = new JButton("📋 Copy");
    private final JButton       downloadButton = new JButton("💾 Download");
    private final JButton       randomButton   = new JButton("🎲 Next Template");
    private final JToggleButton themeToggle    = new JToggleButton("🌙 Dark");

    // ── Panels kept for repainting on theme change ────────────────────────────
    private JPanel rootPanel, inputCard, resultCard;
    private JScrollPane resultScroll;
    private final java.util.List<JLabel> inputLabels = new java.util.ArrayList<>();

    // ── Constructor ───────────────────────────────────────────────────────────
    public MadLibsGenerator() {
        super("Mad Libs Generator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(540, 580));

        initFields();
        buildUI();
        wireEvents();
        applyTheme();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── Field initialisation ──────────────────────────────────────────────────
    private void initFields() {
        for (JTextField f : inputs()) f.setFont(FONT_FIELD);
        nounField.setToolTipText("e.g. dragon, sandwich, professor");
        verbField.setToolTipText("e.g. juggle, whisper, somersault");
        adjectiveField.setToolTipText("e.g. bewildered, sparkly, ancient");
        placeField.setToolTipText("e.g. Tokyo, the moon, a sock drawer");
        resultArea.setFont(FONT_STORY);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setEditable(false);
    }

    // ── UI construction ───────────────────────────────────────────────────────
    private void buildUI() {
        rootPanel = new JPanel(new BorderLayout());
        rootPanel.add(buildHeader(),     BorderLayout.NORTH);
        rootPanel.add(buildInputCard(),  BorderLayout.CENTER);
        rootPanel.add(buildResultCard(), BorderLayout.SOUTH);
        setContentPane(rootPanel);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBackground(ACCENT);
        p.setBorder(new EmptyBorder(16, 26, 16, 26));

        JLabel title = new JLabel("Mad Libs Generator");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Fill in the blanks — stories await!");
        sub.setFont(FONT_SMALL);
        sub.setForeground(new Color(255, 213, 208));
        JPanel txt = new JPanel(new GridLayout(2, 1, 0, 3));
        txt.setOpaque(false);
        txt.add(title); txt.add(sub);
        p.add(txt, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        styleHeaderToggle(themeToggle);
        styleHeaderBtn(randomButton);
        right.add(themeToggle);
        right.add(randomButton);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JPanel buildInputCard() {
        inputCard = new JPanel(new GridBagLayout());
        inputCard.setBorder(new EmptyBorder(20, 26, 16, 26));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(7, 0, 7, 10);
        gc.anchor = GridBagConstraints.WEST;

        String[][] rows = {
            {"🐾  Noun",      nounField.getToolTipText()},
            {"🏃  Verb",      verbField.getToolTipText()},
            {"✨  Adjective", adjectiveField.getToolTipText()},
            {"📍  Place",     placeField.getToolTipText()},
        };
        JTextField[] fields = {nounField, verbField, adjectiveField, placeField};

        for (int i = 0; i < rows.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
            JLabel lbl = new JLabel(rows[i][0]);
            lbl.setFont(FONT_LABEL);
            lbl.setPreferredSize(new Dimension(132, 32));
            inputLabels.add(lbl);
            inputCard.add(lbl, gc);

            gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
            inputCard.add(fields[i], gc);
        }

        gc.gridx = 0; gc.gridy = rows.length; gc.gridwidth = 2;
        gc.fill = GridBagConstraints.NONE; gc.anchor = GridBagConstraints.EAST;
        gc.insets = new Insets(2, 0, 2, 0);
        statusLabel.setFont(FONT_SMALL);
        inputCard.add(statusLabel, gc);

        gc.gridy = rows.length + 1; gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(10, 0, 0, 0);
        JPanel btns = new JPanel(new GridLayout(1, 2, 10, 0));
        btns.setOpaque(false);
        styleButton(generateButton, ACCENT, Color.WHITE);
        // clearButton colour applied in applyTheme
        btns.add(generateButton);
        btns.add(clearButton);
        inputCard.add(btns, gc);

        return inputCard;
    }

    private JPanel buildResultCard() {
        resultCard = new JPanel(new BorderLayout(0, 8));
        resultCard.setBorder(new EmptyBorder(12, 26, 20, 26));

        JLabel heading = new JLabel("Your Story");
        heading.setFont(FONT_LABEL);
        resultCard.add(heading, BorderLayout.NORTH);

        resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(BorderFactory.createEmptyBorder());
        resultScroll.setPreferredSize(new Dimension(500, 108));
        resultCard.add(resultScroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(8, 0));
        footer.setOpaque(false);
        charCountLabel.setFont(FONT_SMALL);
        footer.add(charCountLabel, BorderLayout.WEST);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionRow.setOpaque(false);
        styleButton(copyButton,     ACCENT2, Color.WHITE);
        styleButton(downloadButton, ACCENT3, Color.WHITE);
        copyButton.setEnabled(false);
        downloadButton.setEnabled(false);
        actionRow.add(copyButton);
        actionRow.add(downloadButton);
        footer.add(actionRow, BorderLayout.EAST);
        resultCard.add(footer, BorderLayout.SOUTH);

        return resultCard;
    }

    // ── Theme application ─────────────────────────────────────────────────────
    private void applyTheme() {
        theme = darkMode ? DARK : LIGHT;

        rootPanel.setBackground(theme[0]);
        inputCard.setBackground(theme[1]);
        inputCard.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, theme[6]),
                new EmptyBorder(20, 26, 16, 26)));
        resultCard.setBackground(theme[0]);

        for (JLabel lbl : inputLabels) lbl.setForeground(theme[2]);
        statusLabel.setForeground(theme[3]);
        charCountLabel.setForeground(theme[3]);

        for (JTextField f : inputs()) {
            f.setBackground(theme[4]);
            f.setForeground(theme[2]);
            f.setCaretColor(theme[2]);
            f.setBorder(fieldBorder(theme[5]));
        }

        resultArea.setBackground(theme[4]);
        resultArea.setForeground(theme[2]);
        resultArea.setCaretColor(theme[2]);
        resultArea.setBorder(new CompoundBorder(
                new LineBorder(theme[5], 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        resultScroll.getViewport().setBackground(theme[4]);

        styleButton(clearButton, theme[7], theme[2]);

        themeToggle.setText(darkMode ? "☀️ Light" : "🌙 Dark");
        themeToggle.setSelected(darkMode);

        repaint();
    }

    // ── Event wiring ──────────────────────────────────────────────────────────
    private void wireEvents() {
        generateButton.addActionListener(e -> generateStory());
        clearButton.addActionListener(e -> clearAll());
        copyButton.addActionListener(e -> copyToClipboard());
        downloadButton.addActionListener(e -> showDownloadDialog());
        randomButton.addActionListener(e -> cycleTemplate());
        themeToggle.addActionListener(e -> { darkMode = themeToggle.isSelected(); applyTheme(); });

        KeyAdapter enter = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) generateStory();
            }
        };
        for (JTextField f : inputs()) {
            f.addKeyListener(enter);
            f.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    if (!f.getText().trim().isEmpty()) f.setBorder(fieldBorder(theme[5]));
                }
                public void removeUpdate(javax.swing.event.DocumentEvent e) {}
                public void changedUpdate(javax.swing.event.DocumentEvent e) {}
            });
        }
    }

    // ── Story generation ──────────────────────────────────────────────────────
    private void generateStory() {
        String noun = nounField.getText().trim(), verb = verbField.getText().trim(),
               adj  = adjectiveField.getText().trim(), place = placeField.getText().trim();

        if (noun.isEmpty() || verb.isEmpty() || adj.isEmpty() || place.isEmpty()) {
            highlightEmpty();
            JOptionPane.showMessageDialog(this, "Please fill in all four fields first!",
                    "Missing Fields", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String story = TEMPLATES[templateIndex]
                .replace("{noun}", noun).replace("{verb}", verb)
                .replace("{adjective}", adj).replace("{place}", place);

        resultArea.setText(story);
        int len = story.length();
        charCountLabel.setText(len + " character" + (len == 1 ? "" : "s"));
        copyButton.setEnabled(true);
        downloadButton.setEnabled(true);
        statusLabel.setText("Template " + (templateIndex + 1) + " of " + TEMPLATES.length);
    }

    private void clearAll() {
        for (JTextField f : inputs()) { f.setText(""); f.setBorder(fieldBorder(theme[5])); }
        resultArea.setText("");
        charCountLabel.setText("0 characters");
        statusLabel.setText(" ");
        copyButton.setEnabled(false);
        downloadButton.setEnabled(false);
        nounField.requestFocus();
    }

    private void copyToClipboard() {
        String text = resultArea.getText();
        if (!text.isEmpty()) {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                   .setContents(new StringSelection(text), null);
            copyButton.setText("✅ Copied!");
            new Timer(1600, e -> copyButton.setText("📋 Copy")) {{ setRepeats(false); start(); }};
        }
    }

    private void cycleTemplate() {
        templateIndex = (templateIndex + 1) % TEMPLATES.length;
        statusLabel.setText("Template " + (templateIndex + 1) + " of " + TEMPLATES.length);
        if (!resultArea.getText().isEmpty()) generateStory();
    }

    // ── Download dialog ───────────────────────────────────────────────────────
    private void showDownloadDialog() {
        String story = resultArea.getText();
        if (story.isEmpty()) return;

        Object[] formats = {"📄  Plain Text (.txt)", "🌐  Web Page (.html)", "✍️  Markdown (.md)"};
        Object choice = JOptionPane.showInputDialog(this,
                "Choose a format to save your story:", "Download Story",
                JOptionPane.PLAIN_MESSAGE, null, formats, formats[0]);
        if (choice == null) return;

        String ext = choice.toString().contains(".html") ? "html"
                   : choice.toString().contains(".md")   ? "md" : "txt";

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Story As…");
        fc.setSelectedFile(new File("my_mad_lib." + ext));
        fc.setFileFilter(new FileNameExtensionFilter(ext.toUpperCase() + " Files", ext));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File target = fc.getSelectedFile();
        if (!target.getName().toLowerCase().endsWith("." + ext))
            target = new File(target.getAbsolutePath() + "." + ext);

        try {
            Files.writeString(target.toPath(), buildContent(story, ext));
            final File saved = target;
            downloadButton.setText("✅ Saved!");
            new Timer(2000, e -> {
                downloadButton.setText("💾 Download");
                int open = JOptionPane.showConfirmDialog(this,
                        "Saved to:\n" + saved.getAbsolutePath() + "\n\nOpen containing folder?",
                        "Saved!", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (open == JOptionPane.YES_OPTION) {
                    try { Desktop.getDesktop().open(saved.getParentFile()); }
                    catch (IOException ignored) {}
                }
            }) {{ setRepeats(false); start(); }};
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save:\n" + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String buildContent(String story, String ext) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return switch (ext) {
            case "html" -> "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n  <meta charset=\"UTF-8\">\n"
                + "  <title>My Mad Lib</title>\n  <style>\n"
                + "    body{font-family:Georgia,serif;max-width:640px;margin:60px auto;"
                + "background:#fffcf5;color:#1e1e28;line-height:1.8}\n"
                + "    h1{color:#ff6f61;margin-bottom:4px}\n"
                + "    .story{font-size:1.1em;background:#fff;padding:24px 28px;"
                + "border-left:4px solid #ff6f61;border-radius:6px;margin:20px 0}\n"
                + "    small{color:#828296}\n"
                + "  </style>\n</head>\n<body>\n"
                + "  <h1>My Mad Lib</h1>\n"
                + "  <div class=\"story\">" + story + "</div>\n"
                + "  <small>Generated on " + ts + "</small>\n</body>\n</html>\n";
            case "md"   -> "# My Mad Lib\n\n> " + story + "\n\n---\n*Generated on " + ts + "*\n";
            default     -> "MY MAD LIB\n" + "=".repeat(42) + "\n\n"
                         + story + "\n\n" + "-".repeat(42) + "\nGenerated on " + ts + "\n";
        };
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JTextField[] inputs() {
        return new JTextField[]{nounField, verbField, adjectiveField, placeField};
    }

    private void highlightEmpty() {
        for (JTextField f : inputs()) {
            f.setBorder(f.getText().trim().isEmpty()
                    ? new CompoundBorder(new LineBorder(ACCENT, 2, true), new EmptyBorder(4, 8, 4, 8))
                    : fieldBorder(theme[5]));
        }
    }

    private static Border fieldBorder(Color c) {
        return new CompoundBorder(new LineBorder(c, 1, true), new EmptyBorder(5, 9, 5, 9));
    }

    private static void styleButton(JButton btn, Color bg, Color fg) {
        btn.setFont(FONT_BTN);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        for (MouseListener ml : btn.getMouseListeners())
            if (ml.getClass().isAnonymousClass()) btn.removeMouseListener(ml);
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { if (btn.isEnabled()) btn.setBackground(bg.darker()); }
            @Override public void mouseExited(MouseEvent e)  { if (btn.isEnabled()) btn.setBackground(bg); }
        });
    }

    private static void styleHeaderBtn(JButton btn) {
        btn.setFont(FONT_SMALL);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setForeground(Color.WHITE);
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(255, 255, 255, 130), 1, true),
                new EmptyBorder(6, 12, 6, 12)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static void styleHeaderToggle(JToggleButton btn) {
        btn.setFont(FONT_SMALL);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setForeground(Color.WHITE);
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(255, 255, 255, 130), 1, true),
                new EmptyBorder(6, 12, 6, 12)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MadLibsGenerator::new);
    }
}
