import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;

public class NumberGuessingGame extends JFrame implements ActionListener {

    // ── Dark Palette ──────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(13,  17,  23);
    private static final Color BG_PANEL     = new Color(22,  27,  34);
    private static final Color BG_CARD      = new Color(30,  37,  46);
    private static final Color ACCENT_CYAN  = new Color(56, 189, 248);
    private static final Color ACCENT_PINK  = new Color(240, 80, 120);
    private static final Color ACCENT_GREEN = new Color(52, 211, 153);
    private static final Color TEXT_PRIMARY = new Color(230, 237, 243);
    private static final Color TEXT_MUTED   = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(48,  54,  61);

    // ── Fonts ─────────────────────────────────────────────────────
    private static final Font FONT_TITLE   = new Font("Consolas", Font.BOLD,  20);
    private static final Font FONT_BODY    = new Font("Consolas", Font.PLAIN, 14);
    private static final Font FONT_SMALL   = new Font("Consolas", Font.PLAIN, 12);
    private static final Font FONT_BIG     = new Font("Consolas", Font.BOLD,  36);
    private static final Font FONT_INPUT   = new Font("Consolas", Font.BOLD,  18);

    // ── UI Components ─────────────────────────────────────────────
    private JTextField  inputField;
    private JLabel      messageLabel, attemptsLabel, timerLabel, scoreLabel, highScoreLabel;
    private JButton     guessButton, resetButton;
    private JProgressBar timerBar;

    // ── Game State ────────────────────────────────────────────────
    private int  randomNumber, attempts, score, highScore;
    private boolean gameOver = false;

    // ── Timer ─────────────────────────────────────────────────────
    private static final int MAX_SECONDS = 30;
    private int  secondsLeft;
    private javax.swing.Timer countdownTimer;

    // ─────────────────────────────────────────────────────────────
    public NumberGuessingGame() {
        setTitle("🎯 Number Guessing Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        score     = 0;
        highScore = 0;

        buildUI();
        startNewRound();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── UI Construction ───────────────────────────────────────────
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_DARK);
        root.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        setContentPane(root);

        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildCenter(),  BorderLayout.CENTER);
        root.add(buildFooter(),  BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_PANEL);
        header.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(14, 20, 14, 20)
        ));

        JLabel title = new JLabel("< NumberGuesser />");
        title.setFont(FONT_TITLE);
        title.setForeground(ACCENT_CYAN);

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
        stats.setOpaque(false);

        scoreLabel     = styledLabel("Score: 0",     ACCENT_GREEN, FONT_BODY);
        highScoreLabel = styledLabel("Best: 0",      ACCENT_PINK,  FONT_BODY);
        attemptsLabel  = styledLabel("Attempts: 0",  TEXT_MUTED,   FONT_BODY);

        stats.add(attemptsLabel);
        stats.add(highScoreLabel);
        stats.add(scoreLabel);

        header.add(title, BorderLayout.WEST);
        header.add(stats, BorderLayout.EAST);
        return header;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(BG_DARK);
        center.setBorder(new EmptyBorder(28, 32, 20, 32));

        // Timer row
        JPanel timerRow = new JPanel(new BorderLayout(10, 0));
        timerRow.setOpaque(false);
        timerRow.setMaximumSize(new Dimension(400, 30));

        timerLabel = styledLabel("⏳ 30s", ACCENT_CYAN, FONT_SMALL);
        timerRow.add(timerLabel, BorderLayout.WEST);

        timerBar = new JProgressBar(0, MAX_SECONDS);
        timerBar.setValue(MAX_SECONDS);
        timerBar.setForeground(ACCENT_CYAN);
        timerBar.setBackground(BG_CARD);
        timerBar.setBorderPainted(false);
        timerBar.setPreferredSize(new Dimension(300, 8));
        timerRow.add(timerBar, BorderLayout.CENTER);

        center.add(timerRow);
        center.add(Box.createVerticalStrut(24));

        // Message card
        JPanel card = new GlowCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(20, 24, 20, 24)
        ));
        card.setMaximumSize(new Dimension(400, 100));

        messageLabel = new JLabel("Guess a number between 1 and 100");
        messageLabel.setFont(FONT_BODY);
        messageLabel.setForeground(TEXT_PRIMARY);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(messageLabel);
        center.add(card);
        center.add(Box.createVerticalStrut(24));

        // Input row
        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        inputRow.setOpaque(false);
        inputRow.setMaximumSize(new Dimension(400, 50));

        inputField = new JTextField(8);
        inputField.setFont(FONT_INPUT);
        inputField.setForeground(TEXT_PRIMARY);
        inputField.setBackground(BG_CARD);
        inputField.setCaretColor(ACCENT_CYAN);
        inputField.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        inputField.setHorizontalAlignment(JTextField.CENTER);
        inputField.addActionListener(this);   // Enter key triggers guess

        guessButton = buildButton("Guess →", ACCENT_CYAN,   BG_DARK);
        resetButton = buildButton("↺ Restart", ACCENT_PINK, BG_DARK);

        inputRow.add(inputField);
        inputRow.add(guessButton);
        inputRow.add(resetButton);

        center.add(inputRow);
        return center;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(BG_PANEL);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JLabel hint = styledLabel("© NumberGuesser  |  Score = (11 - attempts) × time bonus", TEXT_MUTED, FONT_SMALL);
        footer.add(hint);
        return footer;
    }

    // ── Helpers ───────────────────────────────────────────────────
    private JLabel styledLabel(String text, Color fg, Font font) {
        JLabel l = new JLabel(text);
        l.setForeground(fg);
        l.setFont(font);
        return l;
    }

    private JButton buildButton(String text, Color fg, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(fg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(fg.darker().darker());
                } else {
                    g2.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 30));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(fg);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BODY);
        btn.setForeground(fg);
        btn.setBackground(new Color(0,0,0,0));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(this);
        return btn;
    }

    // ── Game Logic ────────────────────────────────────────────────
    private void startNewRound() {
        randomNumber = new Random().nextInt(100) + 1;
        attempts     = 0;
        gameOver     = false;
        secondsLeft  = MAX_SECONDS;

        inputField.setEnabled(true);
        inputField.setText("");
        inputField.requestFocus();
        guessButton.setEnabled(true);

        updateAttempts();
        timerBar.setForeground(ACCENT_CYAN);
        timerBar.setValue(MAX_SECONDS);
        timerLabel.setText("⏳ " + MAX_SECONDS + "s");
        timerLabel.setForeground(ACCENT_CYAN);
        messageLabel.setText("Guess a number between 1 and 100");
        messageLabel.setForeground(TEXT_PRIMARY);

        if (countdownTimer != null) countdownTimer.stop();
        countdownTimer = new javax.swing.Timer(1000, e -> tick());
        countdownTimer.start();
    }

    private void tick() {
        secondsLeft--;
        timerBar.setValue(secondsLeft);
        timerLabel.setText("⏳ " + secondsLeft + "s");

        float ratio = (float) secondsLeft / MAX_SECONDS;
        if (ratio > 0.5f) {
            timerBar.setForeground(ACCENT_CYAN);
            timerLabel.setForeground(ACCENT_CYAN);
        } else if (ratio > 0.25f) {
            timerBar.setForeground(new Color(250, 190, 70));
            timerLabel.setForeground(new Color(250, 190, 70));
        } else {
            timerBar.setForeground(ACCENT_PINK);
            timerLabel.setForeground(ACCENT_PINK);
        }

        if (secondsLeft <= 0) {
            countdownTimer.stop();
            endGame(false, "⏰ Time's up! The number was " + randomNumber);
        }
    }

    private void handleGuess() {
        if (gameOver) return;
        try {
            int guess = Integer.parseInt(inputField.getText().trim());
            attempts++;
            updateAttempts();
            inputField.setText("");
            inputField.requestFocus();

            if (guess < 1 || guess > 100) {
                flash("⚠ Enter a number between 1 and 100!", ACCENT_PINK);
                return;
            }
            if (guess < randomNumber) {
                flash("📉 Too Low! Try higher.", new Color(250, 190, 70));
            } else if (guess > randomNumber) {
                flash("📈 Too High! Try lower.", new Color(250, 190, 70));
            } else {
                countdownTimer.stop();
                int gained = calcScore();
                score += gained;
                if (score > highScore) highScore = score;
                updateScores();
                endGame(true, "🎉 Correct in " + attempts + " tries! +" + gained + " pts");
            }
        } catch (NumberFormatException ex) {
            flash("✖ Enter a valid number!", ACCENT_PINK);
        }
    }

    private int calcScore() {
        // Base: (11 - attempts) capped at 1, multiplied by time bonus
        int base  = Math.max(1, 11 - attempts);
        int bonus = Math.max(1, secondsLeft);
        return base * bonus;
    }

    private void endGame(boolean won, String msg) {
        gameOver = true;
        inputField.setEnabled(false);
        guessButton.setEnabled(false);
        messageLabel.setText(msg);
        messageLabel.setForeground(won ? ACCENT_GREEN : ACCENT_PINK);
    }

    private void flash(String msg, Color color) {
        messageLabel.setText(msg);
        messageLabel.setForeground(color);
    }

    private void updateAttempts() {
        attemptsLabel.setText("Attempts: " + attempts);
    }

    private void updateScores() {
        scoreLabel.setText("Score: " + score);
        highScoreLabel.setText("Best: " + highScore);
    }

    // ── ActionListener ────────────────────────────────────────────
    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == guessButton || src == inputField) handleGuess();
        else if (src == resetButton) startNewRound();
    }

    // ── Glow Card ─────────────────────────────────────────────────
    static class GlowCard extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Subtle glow
            for (int i = 6; i >= 1; i--) {
                int alpha = 12 - i * 2;
                g2.setColor(new Color(56, 189, 248, Math.max(0, alpha)));
                g2.fillRoundRect(-i, -i, getWidth()+i*2, getHeight()+i*2, 10+i, 10+i);
            }
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
        }
    }

    // ── Entry Point ───────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(NumberGuessingGame::new);
    }
}
