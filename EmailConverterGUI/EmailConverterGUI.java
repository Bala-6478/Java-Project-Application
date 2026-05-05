import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Pattern;

public class EmailConverterGUI extends JFrame {

    // Encapsulated UI components
    private final JTextField emailField;
    private final JTextArea resultArea;

    // Pre-compiled Regex Pattern for better performance
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    // Modern Color Palette
    private final Color bgColor = new Color(245, 247, 250);
    private final Color primaryColor = new Color(52, 152, 219);
    private final Color primaryHover = new Color(41, 128, 185);
    private final Color dangerColor = new Color(231, 76, 60);
    private final Color dangerHover = new Color(192, 57, 43);
    private final Color textColor = new Color(44, 62, 80);

    public EmailConverterGUI() {
        // Attempt to set System Look and Feel for a more native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Failed to set system look and feel.");
        }

        setTitle("Email Converter Tool");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centers the window
        getContentPane().setBackground(bgColor);

        // Main layout with padding
        setLayout(new BorderLayout(15, 15));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // --- HEADER PANEL ---
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        headerPanel.setBackground(bgColor);
        
        JLabel titleLabel = new JLabel("Email Converter & Validator");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(textColor);
        
        JLabel subtitleLabel = new JLabel("Enter an email address below to process or validate it.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(127, 140, 141));

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);

        // --- INPUT PANEL ---
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(bgColor);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));

        emailField = new JTextField();
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        // Soft border with inner padding for the text field
        Border line = BorderFactory.createLineBorder(new Color(189, 195, 199), 1);
        Border padding = BorderFactory.createEmptyBorder(8, 10, 8, 10);
        emailField.setBorder(BorderFactory.createCompoundBorder(line, padding));

        inputPanel.add(emailField, BorderLayout.CENTER);

        // Combine Header and Input
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(bgColor);
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(inputPanel, BorderLayout.SOUTH);

        // --- CENTER PANEL (Results) ---
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 15));
        resultArea.setForeground(textColor);
        resultArea.setMargin(new Insets(15, 15, 15, 15));
        resultArea.setBorder(BorderFactory.createEmptyBorder()); // Removed default 3D border
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(223, 228, 234), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // --- BOTTOM PANEL (Buttons) ---
        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        buttonPanel.setBackground(bgColor);

        JButton lowerBtn = new JButton("To Lowercase");
        JButton upperBtn = new JButton("To Uppercase");
        JButton userBtn = new JButton("Get Username");
        JButton domainBtn = new JButton("Get Domain");
        JButton validateBtn = new JButton("Validate Email");
        JButton clearBtn = new JButton("Clear");

        // Apply custom styling to buttons
        styleButton(lowerBtn, primaryColor, primaryHover, Color.WHITE);
        styleButton(upperBtn, primaryColor, primaryHover, Color.WHITE);
        styleButton(userBtn, primaryColor, primaryHover, Color.WHITE);
        styleButton(domainBtn, primaryColor, primaryHover, Color.WHITE);
        styleButton(validateBtn, new Color(46, 204, 113), new Color(39, 174, 96), Color.WHITE); // Green for validate
        styleButton(clearBtn, dangerColor, dangerHover, Color.WHITE); // Red for clear

        buttonPanel.add(lowerBtn);
        buttonPanel.add(upperBtn);
        buttonPanel.add(userBtn);
        buttonPanel.add(domainBtn);
        buttonPanel.add(validateBtn);
        buttonPanel.add(clearBtn);

        // Add panels to the main frame
        add(topContainer, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- ACTION LISTENERS ---
        lowerBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            resultArea.setText(email.toLowerCase());
        });

        upperBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            resultArea.setText(email.toUpperCase());
        });

        userBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            int atIndex = email.indexOf("@");
            if (atIndex > 0) {
                resultArea.setText("Username:\n" + email.substring(0, atIndex));
            } else {
                showError("Invalid Email Format: Missing or improperly placed '@'");
            }
        });

        domainBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            int atIndex = email.lastIndexOf("@");
            if (atIndex > 0 && atIndex < email.length() - 1) {
                resultArea.setText("Domain:\n" + email.substring(atIndex + 1));
            } else {
                showError("Invalid Email Format: Missing domain portion");
            }
        });

        validateBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                showError("Please enter an email address first.");
                return;
            }
            if (EMAIL_PATTERN.matcher(email).matches()) {
                resultArea.setText("STATUS: Valid Email ✅\n\n" + email);
                resultArea.setForeground(new Color(39, 174, 96)); // Bold green
            } else {
                showError("STATUS: Invalid Email ❌\n\nPlease check the formatting.");
            }
        });

        clearBtn.addActionListener(e -> {
            emailField.setText("");
            resultArea.setText("");
            resultArea.setForeground(textColor);
            emailField.requestFocus();
        });

        // Reset text color on any standard button press
        java.awt.event.ActionListener resetColorListener = e -> {
            if (e.getSource() != validateBtn && e.getSource() != clearBtn) {
                resultArea.setForeground(textColor);
            }
        };
        lowerBtn.addActionListener(resetColorListener);
        upperBtn.addActionListener(resetColorListener);
        userBtn.addActionListener(resetColorListener);
        domainBtn.addActionListener(resetColorListener);

        setVisible(true);
    }

    /**
     * Helper method to show an error in red text.
     */
    private void showError(String message) {
        resultArea.setText(message);
        resultArea.setForeground(dangerColor);
    }

    /**
     * Helper method to style buttons with flat design and hover effects.
     */
    private void styleButton(JButton btn, Color bg, Color hoverBg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Add Hover Effect
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EmailConverterGUI());
    }
}