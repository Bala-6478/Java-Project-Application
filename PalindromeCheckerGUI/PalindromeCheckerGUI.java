import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class PalindromeCheckerGUI extends JFrame implements ActionListener {

    private JTextField textField;
    private JButton checkButton;
    private JButton clearButton;
    private JLabel resultLabel;
    private JLabel cleanedLabel;
    private JLabel reversedLabel;
    private JPanel resultPanel;

    private static final Color BG_COLOR       = new Color(245, 245, 250);
    private static final Color ACCENT_COLOR   = new Color(70, 130, 180);
    private static final Color SUCCESS_COLOR  = new Color(34, 139, 34);
    private static final Color FAILURE_COLOR  = new Color(178, 34, 34);
    private static final Color PANEL_COLOR    = Color.WHITE;

    PalindromeCheckerGUI() {
        setTitle("Palindrome Checker");
        setSize(480, 340);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout(10, 10));

        add(buildTitlePanel(), BorderLayout.NORTH);
        add(buildInputPanel(), BorderLayout.CENTER);
        add(buildResultPanel(), BorderLayout.SOUTH);

        setVisible(true);
    }

    // ── UI Builders ──────────────────────────────────────────────────────────

    private JPanel buildTitlePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(ACCENT_COLOR);
        panel.setBorder(new EmptyBorder(12, 10, 12, 10));

        JLabel title = new JLabel("🔤 Palindrome Checker");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        panel.add(title);
        return panel;
    }

    private JPanel buildInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(10, 20, 5, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Label
        JLabel inputLabel = new JLabel("Enter a word or phrase:");
        inputLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(inputLabel, gbc);

        // Text field
        textField = new JTextField(22);
        textField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ACCENT_COLOR, 2, true),
            new EmptyBorder(4, 8, 4, 8)
        ));
        textField.addActionListener(this); // Enter key triggers check
        gbc.gridy = 1; gbc.gridwidth = 2;
        panel.add(textField, gbc);

        // Buttons
        checkButton = styledButton("Check", ACCENT_COLOR);
        checkButton.addActionListener(this);
        gbc.gridy = 2; gbc.gridwidth = 1; gbc.gridx = 0;
        panel.add(checkButton, gbc);

        clearButton = styledButton("Clear", new Color(130, 130, 130));
        clearButton.addActionListener(this);
        gbc.gridx = 1;
        panel.add(clearButton, gbc);

        return panel;
    }

    private JPanel buildResultPanel() {
        resultPanel = new JPanel(new GridLayout(3, 1, 4, 4));
        resultPanel.setBackground(PANEL_COLOR);
        resultPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(0, 20, 15, 20),
            BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 230), 1, true),
                new EmptyBorder(10, 14, 10, 14)
            )
        ));

        cleanedLabel  = infoLabel("Cleaned:  —");
        reversedLabel = infoLabel("Reversed: —");
        resultLabel   = new JLabel("Result will appear here", SwingConstants.CENTER);
        resultLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        resultLabel.setForeground(Color.GRAY);

        resultPanel.add(cleanedLabel);
        resultPanel.add(reversedLabel);
        resultPanel.add(resultLabel);
        return resultPanel;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel infoLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 13));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    // ── Logic ─────────────────────────────────────────────────────────────────

    /**
     * Strips non-alphanumeric characters and lowercases the input,
     * so phrases like "A man a plan a canal Panama" are handled correctly.
     */
    private String sanitize(String str) {
        return str.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private boolean isPalindrome(String cleaned) {
        String reversed = new StringBuilder(cleaned).reverse().toString();
        return cleaned.equals(reversed);
    }

    // ── Events ────────────────────────────────────────────────────────────────

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == clearButton) {
            textField.setText("");
            cleanedLabel.setText("Cleaned:  —");
            reversedLabel.setText("Reversed: —");
            resultLabel.setText("Result will appear here");
            resultLabel.setForeground(Color.GRAY);
            resultPanel.setBackground(PANEL_COLOR);
            textField.requestFocus();
            return;
        }

        String input = textField.getText().trim();

        if (input.isEmpty()) {
            resultLabel.setText("⚠️  Please enter a word or phrase!");
            resultLabel.setForeground(new Color(180, 120, 0));
            cleanedLabel.setText("Cleaned:  —");
            reversedLabel.setText("Reversed: —");
            return;
        }

        String cleaned  = sanitize(input);
        String reversed = new StringBuilder(cleaned).reverse().toString();
        boolean result  = cleaned.equals(reversed);

        cleanedLabel.setText("Cleaned:  " + cleaned);
        reversedLabel.setText("Reversed: " + reversed);

        if (result) {
            resultLabel.setText("✅  \"" + input + "\" is a Palindrome!");
            resultLabel.setForeground(SUCCESS_COLOR);
            resultPanel.setBackground(new Color(240, 255, 240));
        } else {
            resultLabel.setText("❌  \"" + input + "\" is NOT a Palindrome.");
            resultLabel.setForeground(FAILURE_COLOR);
            resultPanel.setBackground(new Color(255, 242, 242));
        }
    }

    // ── Entry Point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PalindromeCheckerGUI::new);
    }
}
