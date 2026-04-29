import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class TypingSpeedTesterAdvanced extends JFrame {

    // ── UI Components ──────────────────────────────────────────────────────────
    private JTextPane textDisplay;
    private JTextArea userInput;
    private JButton startBtn, resetBtn;
    private JComboBox<String> timeSelector, difficultySelector;
    private JLabel timerLabel, wpmLabel, accuracyLabel, levelLabel, streakLabel;
    private JProgressBar timerBar;
    private JPanel historyPanel;
    private JTabbedPane tabbedPane;

    // ── Color Palette ──────────────────────────────────────────────────────────
    private static final Color BG_DARK       = new Color(15, 15, 23);
    private static final Color BG_CARD       = new Color(24, 24, 36);
    private static final Color BG_INPUT      = new Color(30, 30, 46);
    private static final Color ACCENT        = new Color(99, 102, 241);   // indigo
    private static final Color ACCENT_GREEN  = new Color(52, 211, 153);
    private static final Color ACCENT_RED    = new Color(248, 113, 113);
    private static final Color ACCENT_YELLOW = new Color(251, 191, 36);
    private static final Color TEXT_PRIMARY  = new Color(241, 241, 255);
    private static final Color TEXT_MUTED    = new Color(148, 148, 180);
    private static final Color BORDER_COLOR  = new Color(45, 45, 65);

    // ── Sentences by Difficulty ────────────────────────────────────────────────
    private static final String[][] SENTENCES = {
        // Easy
        {
            "The cat sat on the mat near the door",
            "She sells sea shells by the sea shore",
            "A big black bear sat on a big black rug",
            "The quick brown fox jumps over the lazy dog",
            "Pack my box with five dozen liquor jugs",
            "How vexingly quick daft zebras jump",
            "The five boxing wizards jump quickly"
        },
        // Medium
        {
            "Java is a powerful object-oriented programming language used worldwide",
            "Practice typing daily to significantly improve both speed and accuracy",
            "Swing provides a rich set of GUI components for desktop applications",
            "Always prioritize writing clean, readable, and well-documented code",
            "Algorithms and data structures are the foundation of computer science",
            "Design patterns help developers write maintainable and scalable software",
            "The best way to learn programming is through consistent daily practice"
        },
        // Hard
        {
            "Polymorphism, encapsulation, and inheritance are the pillars of OOP design",
            "Asynchronous programming with callbacks, promises, and async/await patterns",
            "Implementing efficient algorithms requires understanding time and space complexity",
            "The microservices architecture decomposes applications into loosely coupled services",
            "Functional programming emphasizes immutability, pure functions, and declarative style",
            "Concurrency bugs like deadlocks and race conditions are notoriously hard to debug",
            "Cryptographic hash functions provide integrity verification for sensitive data payloads"
        }
    };

    // ── State ──────────────────────────────────────────────────────────────────
    private String currentSentence = "";
    private int timeLeft, totalTime;
    private javax.swing.Timer countdownTimer;
    private boolean testRunning = false;
    private int currentStreak = 0;
    private int bestStreak    = 0;
    private List<HistoryEntry> history = new ArrayList<>();

    // ── Fonts ──────────────────────────────────────────────────────────────────
    private static final Font FONT_MONO    = new Font("JetBrains Mono", Font.PLAIN, 16);
    private static final Font FONT_MONO_B  = new Font("JetBrains Mono", Font.BOLD,  16);
    private static final Font FONT_LABEL   = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_STAT    = new Font("Segoe UI", Font.BOLD,  26);
    private static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  22);

    // ══════════════════════════════════════════════════════════════════════════
    public TypingSpeedTesterAdvanced() {
        setTitle("Typing Speed Tester Pro");
        setSize(820, 680);
        setMinimumSize(new Dimension(700, 580));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_DARK);

        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(BG_DARK);
        tabbedPane.setForeground(TEXT_PRIMARY);
        tabbedPane.setFont(FONT_LABEL);
        tabbedPane.addTab("⌨  Test", buildTestPanel());
        tabbedPane.addTab("📊 History", buildHistoryPanel());
        add(tabbedPane);

        setVisible(true);
    }

    // ── Test Panel ─────────────────────────────────────────────────────────────
    private JPanel buildTestPanel() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(BG_DARK);
        root.setBorder(new EmptyBorder(16, 20, 16, 20));

        root.add(buildTopBar(),    BorderLayout.NORTH);
        root.add(buildCenter(),    BorderLayout.CENTER);
        root.add(buildStatsBar(),  BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setBackground(BG_DARK);

        JLabel title = new JLabel("Typing Speed Tester Pro");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRIMARY);

        bar.add(title);
        bar.add(Box.createHorizontalStrut(20));
        bar.add(makeLabel("Difficulty:"));
        difficultySelector = styledCombo(new String[]{"Easy", "Medium", "Hard"});
        difficultySelector.setSelectedIndex(1);
        bar.add(difficultySelector);

        bar.add(makeLabel("Time:"));
        timeSelector = styledCombo(new String[]{"15 sec", "30 sec", "60 sec", "120 sec"});
        timeSelector.setSelectedIndex(2);
        bar.add(timeSelector);

        startBtn = styledButton("▶ Start", ACCENT);
        resetBtn = styledButton("↺ Reset", BG_CARD);
        bar.add(startBtn);
        bar.add(resetBtn);

        startBtn.addActionListener(e -> startTest());
        resetBtn.addActionListener(e -> resetTest());

        return bar;
    }

    private JPanel buildCenter() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_DARK);

        // Timer bar
        timerBar = new JProgressBar(0, 100);
        timerBar.setValue(100);
        timerBar.setStringPainted(false);
        timerBar.setForeground(ACCENT);
        timerBar.setBackground(BG_CARD);
        timerBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        timerBar.setBorderPainted(false);
        panel.add(timerBar);
        panel.add(Box.createVerticalStrut(12));

        // Timer + level row
        JPanel infoRow = new JPanel(new BorderLayout());
        infoRow.setBackground(BG_DARK);
        timerLabel  = statLabel("60", ACCENT_YELLOW, " sec");
        levelLabel  = makeLabel("Medium  •  60 sec");
        streakLabel = makeLabel("Streak: 0  |  Best: 0");
        infoRow.add(timerLabel,  BorderLayout.WEST);
        infoRow.add(levelLabel,  BorderLayout.CENTER);
        infoRow.add(streakLabel, BorderLayout.EAST);
        panel.add(infoRow);
        panel.add(Box.createVerticalStrut(12));

        // Text display card
        textDisplay = new JTextPane();
        textDisplay.setEditable(false);
        textDisplay.setFont(FONT_MONO_B);
        textDisplay.setBackground(BG_CARD);
        textDisplay.setForeground(TEXT_MUTED);
        textDisplay.setText("Press ▶ Start to begin the test…");
        textDisplay.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(14, 16, 14, 16)));
        JScrollPane displayScroll = new JScrollPane(textDisplay);
        displayScroll.setBorder(null);
        displayScroll.setBackground(BG_CARD);
        displayScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        displayScroll.setPreferredSize(new Dimension(0, 90));
        panel.add(displayScroll);
        panel.add(Box.createVerticalStrut(10));

        // User input card
        JLabel inputLabel = makeLabel("Your Input:");
        inputLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(inputLabel);
        panel.add(Box.createVerticalStrut(6));

        userInput = new JTextArea();
        userInput.setLineWrap(true);
        userInput.setWrapStyleWord(true);
        userInput.setFont(FONT_MONO);
        userInput.setBackground(BG_INPUT);
        userInput.setForeground(TEXT_PRIMARY);
        userInput.setCaretColor(ACCENT);
        userInput.setEditable(false);
        userInput.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(14, 16, 14, 16)));

        JScrollPane inputScroll = new JScrollPane(userInput);
        inputScroll.setBorder(null);
        inputScroll.setBackground(BG_INPUT);
        inputScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        inputScroll.setPreferredSize(new Dimension(0, 100));
        panel.add(inputScroll);

        userInput.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { SwingUtilities.invokeLater(() -> updateLiveStats()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { SwingUtilities.invokeLater(() -> updateLiveStats()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        return panel;
    }

    private JPanel buildStatsBar() {
        JPanel bar = new JPanel(new GridLayout(1, 2, 20, 0));
        bar.setBackground(BG_DARK);

        wpmLabel      = bigStatLabel("WPM", "0", ACCENT);
        accuracyLabel = bigStatLabel("Accuracy", "—", ACCENT_GREEN);

        bar.add(wpmLabel);
        bar.add(accuracyLabel);
        return bar;
    }

    // ── History Panel ──────────────────────────────────────────────────────────
    private JPanel buildHistoryPanel() {
        historyPanel = new JPanel();
        historyPanel.setLayout(new BoxLayout(historyPanel, BoxLayout.Y_AXIS));
        historyPanel.setBackground(BG_DARK);

        JScrollPane scroll = new JScrollPane(historyPanel);
        scroll.setBackground(BG_DARK);
        scroll.setBorder(null);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_DARK);
        wrapper.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel h = new JLabel("Session History");
        h.setFont(FONT_TITLE);
        h.setForeground(TEXT_PRIMARY);
        wrapper.add(h, BorderLayout.NORTH);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Test Logic ─────────────────────────────────────────────────────────────
    private void startTest() {
        if (testRunning) return;

        int diffIdx = difficultySelector.getSelectedIndex();
        String[] pool = SENTENCES[diffIdx];
        currentSentence = pool[new Random().nextInt(pool.length)];

        // Highlight display text
        textDisplay.setText("");
        StyledDocument doc = textDisplay.getStyledDocument();
        Style base = textDisplay.addStyle("base", null);
        StyleConstants.setForeground(base, TEXT_PRIMARY);
        StyleConstants.setFontFamily(base, FONT_MONO_B.getFamily());
        StyleConstants.setFontSize(base, 16);
        StyleConstants.setBold(base, true);
        try { doc.insertString(0, currentSentence, base); } catch (BadLocationException ignored) {}

        // Parse time
        String sel = (String) timeSelector.getSelectedItem();
        totalTime = Integer.parseInt(sel.split(" ")[0]);
        timeLeft  = totalTime;

        timerBar.setMaximum(totalTime);
        timerBar.setValue(totalTime);
        timerLabel.setText(timeLeft + "");
        levelLabel.setText(difficultySelector.getSelectedItem() + "  •  " + totalTime + " sec");

        userInput.setText("");
        userInput.setEditable(true);
        userInput.requestFocus();
        testRunning = true;
        startBtn.setEnabled(false);

        wpmLabel.setText("<html><div style='text-align:center'>"
            + "<span style='font-size:11px;color:#9494b4'>WPM</span><br>"
            + "<span style='font-size:26px;font-weight:bold;color:#6366f1'>0</span></div></html>");
        accuracyLabel.setText("<html><div style='text-align:center'>"
            + "<span style='font-size:11px;color:#9494b4'>Accuracy</span><br>"
            + "<span style='font-size:26px;font-weight:bold;color:#34d399'>—</span></div></html>");

        countdownTimer = new javax.swing.Timer(1000, e -> {
            timeLeft--;
            timerBar.setValue(timeLeft);
            timerLabel.setText(timeLeft + "");

            // Color urgency
            if (timeLeft <= 10) timerLabel.setForeground(ACCENT_RED);
            else if (timeLeft <= 20) timerLabel.setForeground(ACCENT_YELLOW);

            if (timeLeft <= 0) finishTest();
        });
        countdownTimer.start();
    }

    private void finishTest() {
        countdownTimer.stop();
        testRunning = false;
        userInput.setEditable(false);
        startBtn.setEnabled(true);

        String typed = userInput.getText().trim();
        int wpm = calcWPM(typed);
        double acc = calcAccuracy(typed);

        // Streak logic
        if (acc >= 90.0) {
            currentStreak++;
            bestStreak = Math.max(bestStreak, currentStreak);
        } else {
            currentStreak = 0;
        }
        streakLabel.setText("Streak: " + currentStreak + "  |  Best: " + bestStreak);

        // Record history
        String diff   = (String) difficultySelector.getSelectedItem();
        String time   = new SimpleDateFormat("HH:mm").format(new Date());
        history.add(0, new HistoryEntry(time, diff, totalTime + "s", wpm, acc));
        refreshHistory();

        // Show result popup
        showResultDialog(wpm, acc);
    }

    private void resetTest() {
        if (countdownTimer != null) countdownTimer.stop();
        testRunning = false;
        startBtn.setEnabled(true);
        timeLeft = totalTime;

        timerBar.setValue(timerBar.getMaximum());
        timerLabel.setForeground(ACCENT_YELLOW);
        timerLabel.setText("—");
        levelLabel.setText("—");
        textDisplay.setText("Press ▶ Start to begin the test…");

        StyledDocument doc = textDisplay.getStyledDocument();
        Style s = textDisplay.addStyle("muted", null);
        StyleConstants.setForeground(s, TEXT_MUTED);
        try { doc.setCharacterAttributes(0, doc.getLength(), s, false); } catch (Exception ignored) {}

        userInput.setText("");
        userInput.setEditable(false);
        wpmLabel.setText("<html><div style='text-align:center'>"
            + "<span style='font-size:11px;color:#9494b4'>WPM</span><br>"
            + "<span style='font-size:26px;font-weight:bold;color:#6366f1'>0</span></div></html>");
        accuracyLabel.setText("<html><div style='text-align:center'>"
            + "<span style='font-size:11px;color:#9494b4'>Accuracy</span><br>"
            + "<span style='font-size:26px;font-weight:bold;color:#34d399'>—</span></div></html>");
    }

    // ── Live Stats ─────────────────────────────────────────────────────────────
    private void updateLiveStats() {
        if (!testRunning) return;
        String typed = userInput.getText();

        // Live character highlight
        highlightInput(typed);

        int wpm = calcWPM(typed);
        double acc = calcAccuracy(typed);

        String wpmColor = wpm >= 60 ? "#34d399" : wpm >= 35 ? "#fbbf24" : "#f87171";
        String accColor = acc >= 90 ? "#34d399" : acc >= 70 ? "#fbbf24" : "#f87171";

        wpmLabel.setText("<html><div style='text-align:center'>"
            + "<span style='font-size:11px;color:#9494b4'>WPM</span><br>"
            + "<span style='font-size:26px;font-weight:bold;color:" + wpmColor + "'>" + wpm + "</span></div></html>");
        accuracyLabel.setText("<html><div style='text-align:center'>"
            + "<span style='font-size:11px;color:#9494b4'>Accuracy</span><br>"
            + "<span style='font-size:26px;font-weight:bold;color:" + accColor + "'>"
            + String.format("%.1f", acc) + "%</span></div></html>");

        // Auto-finish when sentence completed correctly
        if (typed.length() >= currentSentence.length() && acc >= 99.0) {
            finishTest();
        }
    }

    private void highlightInput(String typed) {
        StyledDocument doc = textDisplay.getStyledDocument();
        Style correct = textDisplay.addStyle("correct", null);
        StyleConstants.setForeground(correct, ACCENT_GREEN);
        Style wrong = textDisplay.addStyle("wrong", null);
        StyleConstants.setForeground(wrong, ACCENT_RED);
        Style normal = textDisplay.addStyle("normal", null);
        StyleConstants.setForeground(normal, TEXT_PRIMARY);

        for (int i = 0; i < currentSentence.length(); i++) {
            if (i < typed.length()) {
                doc.setCharacterAttributes(i, 1,
                    typed.charAt(i) == currentSentence.charAt(i) ? correct : wrong, false);
            } else {
                doc.setCharacterAttributes(i, 1, normal, false);
            }
        }
    }

    // ── Calculations ───────────────────────────────────────────────────────────
    private int calcWPM(String typed) {
        if (typed.trim().isEmpty()) return 0;
        int words   = typed.trim().split("\\s+").length;
        int elapsed = Math.max(1, totalTime - timeLeft);
        return (words * 60) / elapsed;
    }

    private double calcAccuracy(String typed) {
        if (currentSentence.isEmpty()) return 0;
        int correct = 0;
        int len = Math.min(typed.length(), currentSentence.length());
        for (int i = 0; i < len; i++) {
            if (typed.charAt(i) == currentSentence.charAt(i)) correct++;
        }
        // Penalize extra characters
        int total = Math.max(typed.length(), currentSentence.length());
        return ((double) correct / total) * 100;
    }

    // ── History ────────────────────────────────────────────────────────────────
    private void refreshHistory() {
        historyPanel.removeAll();
        historyPanel.add(Box.createVerticalStrut(12));

        if (history.isEmpty()) {
            JLabel empty = makeLabel("No sessions yet. Complete a test to see results here.");
            historyPanel.add(empty);
        }

        for (HistoryEntry e : history) {
            historyPanel.add(buildHistoryCard(e));
            historyPanel.add(Box.createVerticalStrut(8));
        }
        historyPanel.revalidate();
        historyPanel.repaint();
    }

    private JPanel buildHistoryCard(HistoryEntry e) {
        JPanel card = new JPanel(new GridLayout(1, 5, 10, 0));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(12, 16, 12, 16)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        card.add(centeredLabel(e.time,   TEXT_MUTED,   FONT_SMALL));
        card.add(centeredLabel(e.diff,   TEXT_PRIMARY, FONT_LABEL));
        card.add(centeredLabel(e.dur,    TEXT_MUTED,   FONT_SMALL));
        card.add(centeredLabel(e.wpm + " WPM",  ACCENT, FONT_LABEL));
        Color accColor = e.accuracy >= 90 ? ACCENT_GREEN : e.accuracy >= 70 ? ACCENT_YELLOW : ACCENT_RED;
        card.add(centeredLabel(String.format("%.1f%%", e.accuracy), accColor, FONT_LABEL));
        return card;
    }

    // ── Result Dialog ──────────────────────────────────────────────────────────
    private void showResultDialog(int wpm, double acc) {
        JDialog dlg = new JDialog(this, "Result", true);
        dlg.setSize(360, 240);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(BG_CARD);

        JPanel p = new JPanel();
        p.setBackground(BG_CARD);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(24, 32, 24, 32));

        JLabel title = new JLabel("Test Complete! 🎉");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel wpmRes  = new JLabel(wpm + " WPM");
        wpmRes.setFont(FONT_STAT);
        wpmRes.setForeground(ACCENT);
        wpmRes.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel accRes  = new JLabel(String.format("%.1f%% Accuracy", acc));
        accRes.setFont(FONT_LABEL);
        accRes.setForeground(acc >= 90 ? ACCENT_GREEN : acc >= 70 ? ACCENT_YELLOW : ACCENT_RED);
        accRes.setAlignmentX(Component.CENTER_ALIGNMENT);

        String badge = wpm >= 80 ? "⚡ Speed Demon" : wpm >= 50 ? "🚀 Fast Fingers" : "📝 Keep Practicing";
        JLabel badgeLbl = new JLabel(badge);
        badgeLbl.setFont(FONT_SMALL);
        badgeLbl.setForeground(TEXT_MUTED);
        badgeLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton close = styledButton("Try Again", ACCENT);
        close.setAlignmentX(Component.CENTER_ALIGNMENT);
        close.addActionListener(e -> { dlg.dispose(); resetTest(); });

        p.add(title); p.add(Box.createVerticalStrut(12));
        p.add(wpmRes); p.add(Box.createVerticalStrut(4));
        p.add(accRes); p.add(Box.createVerticalStrut(8));
        p.add(badgeLbl); p.add(Box.createVerticalStrut(16));
        p.add(close);

        dlg.add(p);
        dlg.setVisible(true);
    }

    // ── UI Helpers ─────────────────────────────────────────────────────────────
    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    private JLabel centeredLabel(String text, Color color, Font font) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    private JLabel statLabel(String val, Color color, String suffix) {
        JLabel l = new JLabel("<html><b style='font-size:18px'>" + val + "</b>"
            + "<span style='font-size:11px;color:#9494b4'>" + suffix + "</span></html>");
        l.setForeground(color);
        l.setFont(FONT_STAT);
        return l;
    }

    private JLabel bigStatLabel(String title, String val, Color color) {
        JLabel l = new JLabel("<html><div style='text-align:center'>"
            + "<span style='font-size:11px;color:#9494b4'>" + title + "</span><br>"
            + "<span style='font-size:26px;font-weight:bold;color:#" + toHex(color) + "'>" + val + "</span>"
            + "</div></html>", SwingConstants.CENTER);
        l.setFont(FONT_STAT);
        l.setBackground(BG_CARD);
        l.setOpaque(true);
        l.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(12, 0, 12, 0)));
        return l;
    }

    private JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_LABEL);
        btn.setBackground(bg);
        btn.setForeground(TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
            new LineBorder(bg.brighter(), 1, true),
            new EmptyBorder(6, 16, 6, 16)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(FONT_SMALL);
        combo.setBackground(BG_CARD);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        return combo;
    }

    private String toHex(Color c) {
        return String.format("%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    // ── Data Model ─────────────────────────────────────────────────────────────
    private static class HistoryEntry {
        String time, diff, dur;
        int wpm;
        double accuracy;
        HistoryEntry(String time, String diff, String dur, int wpm, double accuracy) {
            this.time = time; this.diff = diff; this.dur = dur;
            this.wpm = wpm; this.accuracy = accuracy;
        }
    }

    // ── Entry Point ────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(TypingSpeedTesterAdvanced::new);
    }
}
