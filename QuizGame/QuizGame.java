import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class QuizGame extends JFrame implements ActionListener {

    // ─── Constants ───────────────────────────────────────────────
    private static final int WINDOW_W        = 750;
    private static final int WINDOW_H        = 520;
    private static final int SECONDS_PER_Q   = 20;

    private static final Color BG_DARK       = new Color(15,  17,  26);
    private static final Color BG_CARD       = new Color(24,  27,  42);
    private static final Color ACCENT_BLUE   = new Color(99,  179, 237);
    private static final Color ACCENT_GREEN  = new Color(72,  199, 142);
    private static final Color ACCENT_RED    = new Color(252, 100, 100);
    private static final Color ACCENT_YELLOW = new Color(251, 211,  70);
    private static final Color TEXT_PRIMARY  = new Color(235, 238, 255);
    private static final Color TEXT_MUTED    = new Color(130, 140, 170);
    private static final Color OPTION_HOVER  = new Color(35,  40,  62);
    private static final Color OPTION_BORDER = new Color(55,  62,  90);

    private static final Font FONT_TITLE     = new Font("SansSerif", Font.BOLD,  26);
    private static final Font FONT_QUESTION  = new Font("SansSerif", Font.BOLD,  17);
    private static final Font FONT_OPTION    = new Font("SansSerif", Font.PLAIN, 15);
    private static final Font FONT_BUTTON    = new Font("SansSerif", Font.BOLD,  15);
    private static final Font FONT_SMALL     = new Font("SansSerif", Font.BOLD,  13);

    // ─── Questions: {question, opt1, opt2, opt3, opt4, correctAnswer, category} ──
    private static final String[][] QUESTIONS = {
        {"What is the capital of India?",
         "Mumbai", "Delhi", "Chennai", "Kolkata", "Delhi", "Geography"},

        {"Which language is primarily used for Android development?",
         "Python", "Swift", "Kotlin", "Ruby", "Kotlin", "Technology"},

        {"Which planet is called the Red Planet?",
         "Earth", "Mars", "Venus", "Jupiter", "Mars", "Science"},

        {"Who invented the Java programming language?",
         "James Gosling", "Dennis Ritchie", "Bjarne Stroustrup", "Guido van Rossum", "James Gosling", "Technology"},

        {"Which is the largest ocean on Earth?",
         "Indian", "Atlantic", "Pacific", "Arctic", "Pacific", "Geography"},

        {"What does CPU stand for?",
         "Central Processing Unit", "Core Power Unit", "Computer Personal Unit", "Central Program Utility", "Central Processing Unit", "Technology"},

        {"Which country has the largest population in the world?",
         "USA", "India", "China", "Russia", "India", "Geography"},

        {"What is the chemical symbol for Gold?",
         "Go", "Gd", "Au", "Ag", "Au", "Science"},

        {"In what year did World War II end?",
         "1943", "1944", "1945", "1946", "1945", "History"},

        {"Which data structure uses LIFO (Last In First Out)?",
         "Queue", "Stack", "Tree", "Graph", "Stack", "Technology"},

        {"What is the speed of light (approx.) in km/s?",
         "150,000", "300,000", "500,000", "1,000,000", "300,000", "Science"},

        {"Which is the smallest continent by area?",
         "Europe", "Antarctica", "Australia", "South America", "Australia", "Geography"},
    };

    // ─── State ───────────────────────────────────────────────────
    private int   currentQuestion = 0;
    private int   score           = 0;
    private int   streak          = 0;
    private int   maxStreak       = 0;
    private int   timeLeft        = SECONDS_PER_Q;
    private boolean answered      = false;
    private int[]   results;           // 1 = correct, -1 = wrong, 0 = skipped

    private Timer countdownTimer;
    private Timer feedbackTimer;

    // ─── UI Components ───────────────────────────────────────────
    private JLabel     titleLabel, timerLabel, scoreLabel, streakLabel, categoryLabel;
    private JLabel     questionLabel;
    private JPanel     progressBarInner;
    private JPanel[]   optionPanels;
    private JLabel[]   optionLabels;
    private JLabel[]   optionLetters;
    private JButton    nextButton;
    private JPanel     timerBarInner;
    private Timer      timerBarAnim;

    // ─── Constructor ─────────────────────────────────────────────
    public QuizGame() {
        results = new int[QUESTIONS.length];

        setTitle("Quiz Master");
        setSize(WINDOW_W, WINDOW_H);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        getContentPane().setBackground(BG_DARK);

        buildUI();
        loadQuestion();
        startTimer();
        setVisible(true);
    }

    // ─── UI Construction ─────────────────────────────────────────
    private void buildUI() {
        // Header bar
        JPanel header = new JPanel(null);
        header.setBounds(0, 0, WINDOW_W, 64);
        header.setBackground(BG_CARD);
        add(header);

        titleLabel = makeLabel("QUIZ MASTER", FONT_TITLE, ACCENT_BLUE);
        titleLabel.setBounds(24, 14, 260, 36);
        header.add(titleLabel);

        categoryLabel = makeLabel("", FONT_SMALL, TEXT_MUTED);
        categoryLabel.setBounds(24, 44, 300, 18);
        header.add(categoryLabel);

        streakLabel = makeLabel("🔥 0", FONT_SMALL, ACCENT_YELLOW);
        streakLabel.setBounds(440, 18, 80, 28);
        header.add(streakLabel);

        scoreLabel = makeLabel("Score: 0 / " + QUESTIONS.length, FONT_SMALL, TEXT_PRIMARY);
        scoreLabel.setBounds(540, 18, 180, 28);
        header.add(scoreLabel);

        // Progress bar (question progress)
        JPanel progressBg = new JPanel(null);
        progressBg.setBounds(0, 62, WINDOW_W, 4);
        progressBg.setBackground(OPTION_BORDER);
        add(progressBg);

        progressBarInner = new JPanel();
        progressBarInner.setBounds(0, 0, 0, 4);
        progressBarInner.setBackground(ACCENT_BLUE);
        progressBg.add(progressBarInner);

        // Timer bar
        JPanel timerBarBg = new JPanel(null);
        timerBarBg.setBounds(0, 66, WINDOW_W, 4);
        timerBarBg.setBackground(new Color(40, 45, 65));
        add(timerBarBg);

        timerBarInner = new JPanel();
        timerBarInner.setBounds(0, 0, WINDOW_W, 4);
        timerBarInner.setBackground(ACCENT_GREEN);
        timerBarBg.add(timerBarInner);

        // Timer label
        timerLabel = makeLabel("0:20", FONT_SMALL, ACCENT_GREEN);
        timerLabel.setBounds(WINDOW_W - 70, 18, 60, 28);
        header.add(timerLabel);

        // Question label
        questionLabel = new JLabel("", SwingConstants.LEFT);
        questionLabel.setBounds(30, 88, WINDOW_W - 60, 60);
        questionLabel.setFont(FONT_QUESTION);
        questionLabel.setForeground(TEXT_PRIMARY);
        questionLabel.setVerticalAlignment(SwingConstants.CENTER);
        add(questionLabel);

        // Option panels (4)
        optionPanels  = new JPanel[4];
        optionLabels  = new JLabel[4];
        optionLetters = new JLabel[4];
        String[] letters = {"A", "B", "C", "D"};
        int startY = 156;

        for (int i = 0; i < 4; i++) {
            final int idx = i;

            JPanel panel = new JPanel(null);
            panel.setBounds(30, startY + i * 62, WINDOW_W - 60, 52);
            panel.setBackground(BG_CARD);
            panel.setBorder(BorderFactory.createLineBorder(OPTION_BORDER, 1, true));
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel letter = new JLabel(letters[i]);
            letter.setBounds(12, 12, 28, 28);
            letter.setFont(FONT_SMALL);
            letter.setForeground(ACCENT_BLUE);
            letter.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(letter);

            JLabel text = new JLabel();
            text.setBounds(50, 10, WINDOW_W - 130, 32);
            text.setFont(FONT_OPTION);
            text.setForeground(TEXT_PRIMARY);
            panel.add(text);

            // Hover effect
            panel.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    if (!answered) panel.setBackground(OPTION_HOVER);
                }
                @Override public void mouseExited(MouseEvent e) {
                    if (!answered) panel.setBackground(BG_CARD);
                }
                @Override public void mouseClicked(MouseEvent e) {
                    if (!answered) selectOption(idx);
                }
            });
            text.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (!answered) selectOption(idx);
                }
            });

            optionPanels[idx]  = panel;
            optionLabels[idx]  = text;
            optionLetters[idx] = letter;
            add(panel);
        }

        // Next button
        nextButton = new JButton("Skip →");
        nextButton.setBounds(WINDOW_W - 170, WINDOW_H - 66, 140, 42);
        nextButton.setFont(FONT_BUTTON);
        nextButton.setBackground(ACCENT_BLUE);
        nextButton.setForeground(Color.WHITE);
        nextButton.setFocusPainted(false);
        nextButton.setBorderPainted(false);
        nextButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nextButton.addActionListener(this);
        add(nextButton);

        // Question counter label (bottom left)
        JLabel qCounter = new JLabel("Question " + (currentQuestion + 1) + " of " + QUESTIONS.length) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        qCounter.setName("qCounter");
        qCounter.setBounds(30, WINDOW_H - 60, 220, 30);
        qCounter.setFont(FONT_SMALL);
        qCounter.setForeground(TEXT_MUTED);
        add(qCounter);
    }

    // ─── Load Question ────────────────────────────────────────────
    private void loadQuestion() {
        String[] q = QUESTIONS[currentQuestion];

        questionLabel.setText("<html><body style='width:640px'>" +
                (currentQuestion + 1) + ". " + q[0] + "</body></html>");
        categoryLabel.setText("📂 " + q[6]);

        for (int i = 0; i < 4; i++) {
            optionLabels[i].setText(q[i + 1]);
            optionLabels[i].setForeground(TEXT_PRIMARY);
            optionPanels[i].setBackground(BG_CARD);
            optionPanels[i].setBorder(BorderFactory.createLineBorder(OPTION_BORDER, 1, true));
            optionLetters[i].setForeground(ACCENT_BLUE);
            optionLetters[i].setFont(FONT_SMALL);
            optionLetters[i].setText(new String[]{"A","B","C","D"}[i]);
        }

        answered = false;
        timeLeft  = SECONDS_PER_Q;
        nextButton.setText("Skip →");
        nextButton.setBackground(ACCENT_BLUE);

        // Update progress bar
        int pct = (int) ((double) currentQuestion / QUESTIONS.length * WINDOW_W);
        progressBarInner.setBounds(0, 0, pct, 4);

        // Update score label
        scoreLabel.setText("Score: " + score + " / " + QUESTIONS.length);

        // Update question counter
        updateQCounter();

        // Reset timer bar
        timerBarInner.setBackground(ACCENT_GREEN);
        timerBarInner.setBounds(0, 0, WINDOW_W, 4);
        timerLabel.setText("0:" + String.format("%02d", timeLeft));
        timerLabel.setForeground(ACCENT_GREEN);
    }

    private void updateQCounter() {
        for (Component c : getContentPane().getComponents()) {
            if ("qCounter".equals(c.getName())) {
                ((JLabel) c).setText("Question " + (currentQuestion + 1) + " of " + QUESTIONS.length);
            }
        }
    }

    // ─── Option Selection ─────────────────────────────────────────
    private void selectOption(int idx) {
        if (answered) return;
        answered = true;
        countdownTimer.stop();

        String selected = optionLabels[idx].getText();
        String correct  = QUESTIONS[currentQuestion][5];
        boolean isRight = selected.equals(correct);

        if (isRight) {
            score++;
            streak++;
            maxStreak = Math.max(maxStreak, streak);
            results[currentQuestion] = 1;

            // Highlight chosen correct option with pulse flash
            flashOption(idx, ACCENT_GREEN, true);
            // Dim all other options
            dimOtherOptions(idx, -1);
            // Show toast
            showToast("✓  Correct!", ACCENT_GREEN);

        } else {
            streak = 0;
            results[currentQuestion] = -1;

            // Shake + highlight the wrong option red
            flashOption(idx, ACCENT_RED, false);
            shakePanel(optionPanels[idx]);

            // Find and highlight the correct option green
            for (int i = 0; i < 4; i++) {
                if (optionLabels[i].getText().equals(correct)) {
                    flashOption(i, ACCENT_GREEN, true);
                }
            }

            // Dim unchosen options
            dimOtherOptions(idx, getCorrectIndex());
            showToast("✗  Wrong! Correct: " + correct, ACCENT_RED);
        }

        streakLabel.setText("🔥 " + streak);
        scoreLabel.setText("Score: " + score + " / " + QUESTIONS.length);
        nextButton.setText("Next →");
        nextButton.setBackground(isRight ? ACCENT_GREEN : ACCENT_BLUE);

        // Auto-advance after 2s
        feedbackTimer = new Timer(2000, e -> {
            feedbackTimer.stop();
            advance();
        });
        feedbackTimer.setRepeats(false);
        feedbackTimer.start();
    }

    /** Returns the index of the correct option for the current question */
    private int getCorrectIndex() {
        String correct = QUESTIONS[currentQuestion][5];
        for (int i = 0; i < 4; i++) {
            if (optionLabels[i].getText().equals(correct)) return i;
        }
        return -1;
    }

    /**
     * Highlights an option with a glowing border + tinted background + icon badge.
     * @param isCorrect true = green checkmark, false = red X
     */
    private void flashOption(int idx, Color color, boolean isCorrect) {
        // Tinted background (semi-transparent simulation via blended color)
        Color tint = blend(BG_CARD, color, 0.18f);
        optionPanels[idx].setBackground(tint);

        // Thick colored border
        optionPanels[idx].setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2, true),
            BorderFactory.createEmptyBorder(1, 1, 1, 1)
        ));

        // Change letter badge to icon + matching color
        optionLetters[idx].setText(isCorrect ? "✓" : "✗");
        optionLetters[idx].setForeground(color);
        optionLetters[idx].setFont(new Font("SansSerif", Font.BOLD, 17));

        // Change option text color to match
        optionLabels[idx].setForeground(color);
    }

    /** Dims all options that are neither the selected nor the correct one */
    private void dimOtherOptions(int selectedIdx, int correctIdx) {
        for (int i = 0; i < 4; i++) {
            if (i != selectedIdx && i != correctIdx) {
                optionPanels[i].setBackground(blend(BG_CARD, BG_DARK, 0.5f));
                optionPanels[i].setBorder(BorderFactory.createLineBorder(
                    new Color(40, 45, 65), 1, true));
                optionLabels[i].setForeground(TEXT_MUTED);
                optionLetters[i].setForeground(new Color(60, 70, 100));
            }
        }
    }

    /** Shakes a panel left-right to signal a wrong answer */
    private void shakePanel(JPanel panel) {
        int origX = panel.getX();
        int[] offsets = {-8, 8, -6, 6, -4, 4, -2, 2, 0};
        Timer shake = new Timer(30, null);
        int[] step = {0};
        shake.addActionListener(e -> {
            if (step[0] < offsets.length) {
                panel.setLocation(origX + offsets[step[0]], panel.getY());
                step[0]++;
            } else {
                panel.setLocation(origX, panel.getY());
                shake.stop();
            }
        });
        shake.start();
    }

    /**
     * Shows a floating toast message at the bottom of the question area.
     * Fades out after ~1.4s.
     */
    private void showToast(String message, Color color) {
        JLabel toast = new JLabel(message, SwingConstants.CENTER);
        toast.setFont(new Font("SansSerif", Font.BOLD, 15));
        toast.setForeground(Color.WHITE);
        toast.setBackground(color.darker());
        toast.setOpaque(true);
        toast.setBounds(WINDOW_W / 2 - 180, 440, 360, 38);
        toast.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 1, true),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        getContentPane().add(toast);
        getContentPane().setComponentZOrder(toast, 0);
        repaint();

        // Fade-out after 1.3s
        Timer fadeTimer = new Timer(1300, e -> {
            getContentPane().remove(toast);
            repaint();
        });
        fadeTimer.setRepeats(false);
        fadeTimer.start();
    }

    /** Blends two colors: ratio=0 → c1, ratio=1 → c2 */
    private static Color blend(Color c1, Color c2, float ratio) {
        float r = Math.min(1f, Math.max(0f, ratio));
        return new Color(
            (int)(c1.getRed()   * (1 - r) + c2.getRed()   * r),
            (int)(c1.getGreen() * (1 - r) + c2.getGreen() * r),
            (int)(c1.getBlue()  * (1 - r) + c2.getBlue()  * r)
        );
    }

    private void highlightOption(int idx, Color color) {
        // Used only for timeout reveal — reuse flashOption logic
        flashOption(idx, color, color.equals(ACCENT_GREEN));
    }

    // ─── Timer ───────────────────────────────────────────────────
    private void startTimer() {
        countdownTimer = new Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText("0:" + String.format("%02d", timeLeft));

            // Animate timer bar
            int w = (int) ((double) timeLeft / SECONDS_PER_Q * WINDOW_W);
            timerBarInner.setBounds(0, 0, w, 4);

            if (timeLeft <= 5) {
                timerLabel.setForeground(ACCENT_RED);
                timerBarInner.setBackground(ACCENT_RED);
            } else if (timeLeft <= 10) {
                timerLabel.setForeground(ACCENT_YELLOW);
                timerBarInner.setBackground(ACCENT_YELLOW);
            }

            if (timeLeft <= 0) {
                countdownTimer.stop();
                results[currentQuestion] = 0; // skipped/timeout
                answered = true;
                // Reveal correct answer in yellow, dim the rest
                int correctIdx = getCorrectIndex();
                flashOption(correctIdx, ACCENT_YELLOW, true);
                dimOtherOptions(correctIdx, correctIdx);
                showToast("⏱  Time's up! Answer: " + QUESTIONS[currentQuestion][5], ACCENT_YELLOW);
                streak = 0;
                streakLabel.setText("🔥 " + streak);
                nextButton.setText("Next →");

                feedbackTimer = new Timer(1200, ev -> {
                    feedbackTimer.stop();
                    advance();
                });
                feedbackTimer.setRepeats(false);
                feedbackTimer.start();
            }
        });
        countdownTimer.start();
    }

    // ─── Advance ─────────────────────────────────────────────────
    private void advance() {
        currentQuestion++;
        if (currentQuestion < QUESTIONS.length) {
            loadQuestion();
            if (countdownTimer != null) countdownTimer.stop();
            timeLeft = SECONDS_PER_Q;
            startTimer();
        } else {
            if (countdownTimer != null) countdownTimer.stop();
            showResult();
        }
    }

    // ─── Next Button ─────────────────────────────────────────────
    @Override
    public void actionPerformed(ActionEvent e) {
        if (feedbackTimer != null && feedbackTimer.isRunning()) {
            feedbackTimer.stop();
        }
        if (!answered) {
            // Skip: count as wrong/skipped
            countdownTimer.stop();
            results[currentQuestion] = 0;
            answered = true;
            streak = 0;
            streakLabel.setText("🔥 " + streak);
        }
        advance();
    }

    // ─── Result Screen ────────────────────────────────────────────
    private void showResult() {
        // Clear current content
        getContentPane().removeAll();
        getContentPane().setBackground(BG_DARK);

        int correct = 0, wrong = 0, skipped = 0;
        for (int r : results) {
            if (r == 1) correct++;
            else if (r == -1) wrong++;
            else skipped++;
        }

        double pct = (double) correct / QUESTIONS.length * 100;
        String grade = pct >= 90 ? "🏆 Excellent!" :
                       pct >= 70 ? "🎯 Great job!" :
                       pct >= 50 ? "👍 Good effort!" : "📚 Keep practising!";
        Color gradeColor = pct >= 70 ? ACCENT_GREEN : pct >= 50 ? ACCENT_YELLOW : ACCENT_RED;

        // Title
        JLabel header = makeLabel("Quiz Complete!", FONT_TITLE, ACCENT_BLUE);
        header.setBounds(0, 30, WINDOW_W, 40);
        header.setHorizontalAlignment(SwingConstants.CENTER);
        getContentPane().add(header);

        // Grade
        JLabel gradeLabel = makeLabel(grade, new Font("SansSerif", Font.BOLD, 22), gradeColor);
        gradeLabel.setBounds(0, 78, WINDOW_W, 36);
        gradeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        getContentPane().add(gradeLabel);

        // Score big
        JLabel bigScore = makeLabel(correct + " / " + QUESTIONS.length,
                new Font("SansSerif", Font.BOLD, 52), TEXT_PRIMARY);
        bigScore.setBounds(0, 118, WINDOW_W, 64);
        bigScore.setHorizontalAlignment(SwingConstants.CENTER);
        getContentPane().add(bigScore);

        // Stats row
        addStatPanel(70,  220, "✅ Correct",  String.valueOf(correct), ACCENT_GREEN);
        addStatPanel(270, 220, "❌ Wrong",     String.valueOf(wrong),   ACCENT_RED);
        addStatPanel(470, 220, "⏭ Skipped",  String.valueOf(skipped), TEXT_MUTED);

        // Max streak
        JLabel streakInfo = makeLabel("🔥 Best streak: " + maxStreak, FONT_SMALL, ACCENT_YELLOW);
        streakInfo.setBounds(0, 300, WINDOW_W, 28);
        streakInfo.setHorizontalAlignment(SwingConstants.CENTER);
        getContentPane().add(streakInfo);

        // Accuracy
        JLabel acc = makeLabel(String.format("Accuracy: %.0f%%", pct), FONT_SMALL, TEXT_MUTED);
        acc.setBounds(0, 326, WINDOW_W, 24);
        acc.setHorizontalAlignment(SwingConstants.CENTER);
        getContentPane().add(acc);

        // Play Again
        JButton playAgain = new JButton("▶  Play Again");
        playAgain.setBounds(WINDOW_W / 2 - 140, 380, 130, 44);
        playAgain.setFont(FONT_BUTTON);
        playAgain.setBackground(ACCENT_GREEN);
        playAgain.setForeground(Color.WHITE);
        playAgain.setFocusPainted(false);
        playAgain.setBorderPainted(false);
        playAgain.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        playAgain.addActionListener(e -> {
            dispose();
            new QuizGame();
        });
        getContentPane().add(playAgain);

        // Quit
        JButton quit = new JButton("✕  Quit");
        quit.setBounds(WINDOW_W / 2 + 10, 380, 130, 44);
        quit.setFont(FONT_BUTTON);
        quit.setBackground(new Color(60, 40, 50));
        quit.setForeground(ACCENT_RED);
        quit.setFocusPainted(false);
        quit.setBorderPainted(false);
        quit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        quit.addActionListener(e -> dispose());
        getContentPane().add(quit);

        revalidate();
        repaint();
    }

    private void addStatPanel(int x, int y, String label, String value, Color color) {
        JPanel panel = new JPanel(null);
        panel.setBounds(x, y, 180, 70);
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createLineBorder(OPTION_BORDER, 1, true));

        JLabel lbl = makeLabel(label, FONT_SMALL, TEXT_MUTED);
        lbl.setBounds(0, 8, 180, 22);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lbl);

        JLabel val = makeLabel(value, new Font("SansSerif", Font.BOLD, 28), color);
        val.setBounds(0, 30, 180, 34);
        val.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(val);

        getContentPane().add(panel);
    }

    // ─── Helper ───────────────────────────────────────────────────
    private static JLabel makeLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    // ─── Entry Point ─────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(QuizGame::new);
    }
}
