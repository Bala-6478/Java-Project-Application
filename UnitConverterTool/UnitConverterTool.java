import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

public class UnitConverterTool extends JFrame implements ActionListener {

    // ── Constants ────────────────────────────────────────────────────────────
    private static final String TITLE         = "Unit Converter";
    private static final int    WINDOW_WIDTH  = 520;
    private static final int    WINDOW_HEIGHT = 420;

    private static final Color BG_COLOR      = new Color(245, 247, 252);
    private static final Color ACCENT_COLOR  = new Color(66, 103, 212);
    private static final Color CARD_COLOR    = Color.WHITE;
    private static final Color TEXT_MUTED    = new Color(120, 130, 150);
    private static final Color RESULT_BG     = new Color(232, 240, 254);

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_INPUT  = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_RESULT = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_FORMULA = new Font("Segoe UI", Font.ITALIC, 12);

    private static final DecimalFormat DF = new DecimalFormat("#.######");

    /**
     * Unit categories → ordered map of display name → base-unit factor.
     * Temperature is handled specially (non-linear), so its factors are unused.
     */
    private static final Map<String, Map<String, Double>> UNITS = new LinkedHashMap<>();

    static {
        // Length — base unit: Meter
        Map<String, Double> length = new LinkedHashMap<>();
        length.put("Meter",      1.0);
        length.put("Kilometer",  1_000.0);
        length.put("Centimeter", 0.01);
        length.put("Millimeter", 0.001);
        length.put("Mile",       1_609.344);
        length.put("Yard",       0.9144);
        length.put("Foot",       0.3048);
        length.put("Inch",       0.0254);
        UNITS.put("Length", length);

        // Weight — base unit: Gram
        Map<String, Double> weight = new LinkedHashMap<>();
        weight.put("Gram",       1.0);
        weight.put("Kilogram",   1_000.0);
        weight.put("Milligram",  0.001);
        weight.put("Pound",      453.592);
        weight.put("Ounce",      28.3495);
        weight.put("Tonne",      1_000_000.0);
        UNITS.put("Weight", weight);

        // Temperature — special-cased in convert()
        Map<String, Double> temp = new LinkedHashMap<>();
        temp.put("Celsius",    0.0);
        temp.put("Fahrenheit", 0.0);
        temp.put("Kelvin",     0.0);
        UNITS.put("Temperature", temp);

        // Speed — base unit: m/s
        Map<String, Double> speed = new LinkedHashMap<>();
        speed.put("m/s",   1.0);
        speed.put("km/h",  1.0 / 3.6);
        speed.put("mph",   0.44704);
        speed.put("knot",  0.514444);
        UNITS.put("Speed", speed);

        // Area — base unit: Square Meter
        Map<String, Double> area = new LinkedHashMap<>();
        area.put("Square Meter",      1.0);
        area.put("Square Kilometer",  1_000_000.0);
        area.put("Square Foot",       0.092903);
        area.put("Square Inch",       0.00064516);
        area.put("Hectare",           10_000.0);
        area.put("Acre",              4_046.86);
        UNITS.put("Area", area);
    }

    // ── UI Components ────────────────────────────────────────────────────────
    private JComboBox<String> categoryBox;
    private JComboBox<String> fromBox;
    private JComboBox<String> toBox;
    private JTextField        inputField;
    private JLabel            resultLabel;
    private JLabel            formulaLabel;
    private JButton           convertButton;
    private JButton           clearButton;
    private JButton           swapButton;

    // ── Constructor ──────────────────────────────────────────────────────────
    public UnitConverterTool() {
        super(TITLE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_COLOR);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        root.add(buildHeader(),     BorderLayout.NORTH);
        root.add(buildFormPanel(),  BorderLayout.CENTER);
        root.add(buildResultPanel(),BorderLayout.SOUTH);

        add(root);
        updateUnits();
        setVisible(true);
    }

    // ── UI Builders ──────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel icon = new JLabel("⇄  ");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        icon.setForeground(ACCENT_COLOR);

        JLabel title = new JLabel(TITLE);
        title.setFont(FONT_TITLE);
        title.setForeground(new Color(30, 40, 70));

        header.add(icon);
        header.add(title);
        return header;
    }

    private JPanel buildFormPanel() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 225, 235), 1, true),
                new EmptyBorder(18, 20, 18, 20)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(6, 0, 6, 10);
        gc.anchor  = GridBagConstraints.WEST;
        gc.fill    = GridBagConstraints.HORIZONTAL;

        // Row 0 – Category
        addRow(card, gc, 0, "Category", buildCategoryBox());

        // Row 1 – From / Swap / To
        gc.gridx = 0; gc.gridy = 1; gc.gridwidth = 1; gc.weightx = 0;
        card.add(makeLabel("From"), gc);

        gc.gridx = 1; gc.weightx = 0.45;
        fromBox = new JComboBox<>();
        styleCombo(fromBox);
        card.add(fromBox, gc);

        gc.gridx = 2; gc.weightx = 0.1; gc.insets = new Insets(6, 4, 6, 4);
        swapButton = makeIconButton("⇄");
        swapButton.addActionListener(this);
        card.add(swapButton, gc);

        gc.gridx = 3; gc.weightx = 0.45; gc.insets = new Insets(6, 0, 6, 10);
        toBox = new JComboBox<>();
        styleCombo(toBox);
        card.add(toBox, gc);

        // Row 2 – Value input
        addRow(card, gc, 2, "Value", buildInputField());

        // Row 3 – Buttons
        gc.gridx = 1; gc.gridy = 3; gc.gridwidth = 3; gc.weightx = 1.0;
        gc.insets = new Insets(12, 0, 0, 0);
        card.add(buildButtonRow(), gc);

        return card;
    }

    private JComboBox<String> buildCategoryBox() {
        categoryBox = new JComboBox<>(UNITS.keySet().toArray(new String[0]));
        styleCombo(categoryBox);
        categoryBox.addActionListener(this);
        return categoryBox;
    }

    private JTextField buildInputField() {
        inputField = new JTextField();
        inputField.setFont(FONT_INPUT);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 210, 230), 1, true),
                new EmptyBorder(4, 8, 4, 8)));
        // Enter key triggers convert
        inputField.addActionListener(this);
        return inputField;
    }

    private JPanel buildButtonRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        row.setOpaque(false);

        clearButton = new JButton("Clear");
        styleButton(clearButton, false);
        clearButton.addActionListener(this);

        convertButton = new JButton("Convert");
        styleButton(convertButton, true);
        convertButton.addActionListener(this);

        row.add(clearButton);
        row.add(convertButton);
        return row;
    }

    private JPanel buildResultPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 0, 0, 0));

        JPanel resultCard = new JPanel(new BorderLayout(0, 4));
        resultCard.setBackground(RESULT_BG);
        resultCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(180, 200, 245), 1, true),
                new EmptyBorder(12, 16, 12, 16)));

        resultLabel = new JLabel("Result will appear here");
        resultLabel.setFont(FONT_RESULT);
        resultLabel.setForeground(ACCENT_COLOR);

        formulaLabel = new JLabel(" ");
        formulaLabel.setFont(FONT_FORMULA);
        formulaLabel.setForeground(TEXT_MUTED);

        resultCard.add(resultLabel, BorderLayout.CENTER);
        resultCard.add(formulaLabel, BorderLayout.SOUTH);
        panel.add(resultCard, BorderLayout.CENTER);
        return panel;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void addRow(JPanel panel, GridBagConstraints gc,
                        int row, String labelText, JComponent field) {
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 1; gc.weightx = 0;
        gc.insets = new Insets(6, 0, 6, 10);
        panel.add(makeLabel(labelText), gc);

        gc.gridx = 1; gc.gridwidth = 3; gc.weightx = 1.0;
        panel.add(field, gc);
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    private void styleCombo(JComboBox<?> box) {
        box.setFont(FONT_INPUT);
        box.setBackground(CARD_COLOR);
        box.setBorder(new LineBorder(new Color(200, 210, 230), 1, true));
    }

    private void styleButton(JButton btn, boolean primary) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (primary) {
            btn.setBackground(ACCENT_COLOR);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(new Color(225, 228, 235));
            btn.setForeground(new Color(60, 70, 90));
        }
        btn.setPreferredSize(new Dimension(100, 34));
    }

    private JButton makeIconButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBackground(new Color(235, 240, 255));
        btn.setForeground(ACCENT_COLOR);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(40, 30));
        return btn;
    }

    // ── Unit / Conversion Logic ───────────────────────────────────────────────

    /** Repopulates fromBox / toBox based on the selected category. */
    private void updateUnits() {
        String category = (String) categoryBox.getSelectedItem();
        Map<String, Double> units = UNITS.get(category);

        fromBox.removeAllItems();
        toBox.removeAllItems();

        for (String unit : units.keySet()) {
            fromBox.addItem(unit);
            toBox.addItem(unit);
        }

        // Default: select a different unit in toBox when possible
        if (toBox.getItemCount() > 1) {
            toBox.setSelectedIndex(1);
        }
    }

    /**
     * Converts {@code value} from {@code from} to {@code to} within {@code category}.
     * For linear categories we normalise to the base unit then scale out.
     * Temperature is handled as a special case.
     *
     * @throws IllegalArgumentException if the category is unknown
     */
    private double convert(double value, String from, String to, String category) {
        if (from.equals(to)) return value;

        if (category.equals("Temperature")) {
            return convertTemperature(value, from, to);
        }

        Map<String, Double> units = UNITS.get(category);
        if (units == null) throw new IllegalArgumentException("Unknown category: " + category);

        double fromFactor = units.get(from);
        double toFactor   = units.get(to);

        // value (in 'from') → base unit → 'to'
        return value * fromFactor / toFactor;
    }

    /** Handles the non-linear temperature conversions. */
    private double convertTemperature(double value, String from, String to) {
        // Normalise to Celsius
        double celsius;
        switch (from) {
            case "Fahrenheit": celsius = (value - 32) * 5.0 / 9.0; break;
            case "Kelvin":     celsius = value - 273.15;            break;
            default:           celsius = value;                      break; // already Celsius
        }
        // Convert from Celsius to target
        switch (to) {
            case "Fahrenheit": return celsius * 9.0 / 5.0 + 32;
            case "Kelvin":     return celsius + 273.15;
            default:           return celsius;
        }
    }

    /** Builds a human-readable formula string shown below the result. */
    private String buildFormula(double input, String from, String to, double result) {
        return String.format("  %s %s  =  %s %s",
                DF.format(input), from, DF.format(result), to);
    }

    // ── ActionListener ───────────────────────────────────────────────────────

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == categoryBox) {
            updateUnits();
            clearResult();

        } else if (src == swapButton) {
            int fromIdx = fromBox.getSelectedIndex();
            int toIdx   = toBox.getSelectedIndex();
            fromBox.setSelectedIndex(toIdx);
            toBox.setSelectedIndex(fromIdx);

        } else if (src == convertButton || src == inputField) {
            performConversion();

        } else if (src == clearButton) {
            inputField.setText("");
            clearResult();
        }
    }

    private void performConversion() {
        String raw = inputField.getText().trim();
        if (raw.isEmpty()) {
            showError("Please enter a value to convert.");
            return;
        }

        double value;
        try {
            value = Double.parseDouble(raw);
        } catch (NumberFormatException ex) {
            showError("\"" + raw + "\" is not a valid number.");
            return;
        }

        String category = (String) categoryBox.getSelectedItem();
        String from     = (String) fromBox.getSelectedItem();
        String to       = (String) toBox.getSelectedItem();

        // Guard: Kelvin cannot be negative
        if (category.equals("Temperature") && from.equals("Kelvin") && value < 0) {
            showError("Kelvin cannot be negative.");
            return;
        }

        double result = convert(value, from, to, category);

        resultLabel.setText(DF.format(result) + "  " + to);
        formulaLabel.setText(buildFormula(value, from, to, result));
    }

    private void clearResult() {
        resultLabel.setText("Result will appear here");
        formulaLabel.setText(" ");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.WARNING_MESSAGE);
    }

    // ── Entry Point ──────────────────────────────────────────────────────────

    public static void main(String[] args) {
        // Run on the Event Dispatch Thread
        SwingUtilities.invokeLater(UnitConverterTool::new);
    }
}
