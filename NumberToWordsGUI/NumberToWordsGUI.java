import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class NumberToWordsGUI extends JFrame implements ActionListener {

    private JTextField inputField;
    private JTextArea outputArea;
    private JButton convertBtn;

    private static final String[] ONES = {
        "", "One", "Two", "Three", "Four", "Five", "Six",
        "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve",
        "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] TENS = {
        "", "", "Twenty", "Thirty", "Forty",
        "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    private static final long[] MAGNITUDES     = { 1_000_000_000L, 1_000_000L, 1_000L };
    private static final String[] MAG_NAMES    = { "Billion", "Million", "Thousand" };

    public NumberToWordsGUI() {
        setTitle("Number to Words Converter");
        setSize(450, 320);
        setMinimumSize(new Dimension(400, 280));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        setVisible(true);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- Top input panel ---
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        inputPanel.add(new JLabel("Enter Number:"));

        inputField = new JTextField(18);
        inputField.addActionListener(this); // Enter key triggers conversion
        inputPanel.add(inputField);

        convertBtn = new JButton("Convert");
        convertBtn.addActionListener(this);
        inputPanel.add(convertBtn);

        // --- Center output area ---
        outputArea = new JTextArea(5, 30);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        outputArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));

        JLabel outputLabel = new JLabel("Result:");
        JPanel centerPanel = new JPanel(new BorderLayout(0, 5));
        centerPanel.add(outputLabel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String text = inputField.getText().trim();
        if (text.isEmpty()) {
            outputArea.setText("Please enter a number.");
            return;
        }
        try {
            long num = Long.parseLong(text);
            outputArea.setText(numberToWords(num));
        } catch (NumberFormatException ex) {
            outputArea.setText("Invalid input! Please enter a whole number.");
        }
        inputField.selectAll();
        inputField.requestFocus();
    }

    private String numberToWords(long num) {
        if (num == 0) return "Zero";

        if (num < 0) {
            return "Negative " + numberToWords(-num);
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < MAGNITUDES.length; i++) {
            if (num >= MAGNITUDES[i]) {
                result.append(convertBelow1000((int) (num / MAGNITUDES[i])))
                      .append(" ").append(MAG_NAMES[i]).append(" ");
                num %= MAGNITUDES[i];
            }
        }

        if (num > 0) {
            result.append(convertBelow1000((int) num));
        }

        return result.toString().trim().replaceAll("\\s+", " ");
    }

    private String convertBelow1000(int num) {
        if (num == 0) return "";

        if (num < 20) return ONES[num];

        if (num < 100) {
            String word = TENS[num / 10];
            if (num % 10 != 0) word += " " + ONES[num % 10];
            return word;
        }

        // 100–999
        String word = ONES[num / 100] + " Hundred";
        int remainder = num % 100;
        if (remainder != 0) word += " " + convertBelow1000(remainder);
        return word;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NumberToWordsGUI::new);
    }
}
