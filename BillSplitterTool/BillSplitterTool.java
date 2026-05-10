import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class BillSplitterTool extends JFrame implements ActionListener {

    // ── Colors ────────────────────────────────────────────────────
    private static final Color BG_DARK       = new Color(13, 17, 23);
    private static final Color BG_CARD       = new Color(22, 27, 34);
    private static final Color BG_INPUT      = new Color(33, 38, 45);
    private static final Color ACCENT_GOLD   = new Color(255, 196, 0);
    private static final Color TEXT_PRIMARY  = new Color(230, 237, 243);
    private static final Color TEXT_MUTED    = new Color(125, 133, 144);
    private static final Color BORDER_COLOR  = new Color(48, 54, 61);
    private static final Color BTN_CLEAR_BG  = new Color(33, 38, 45);
    private static final Color SUCCESS_GREEN = new Color(56, 211, 159);
    private static final Color ERROR_RED     = new Color(255, 100, 100);
    private static final Color TABLE_ROW_ALT = new Color(28, 33, 40);
    private static final Color TABLE_HDR     = new Color(18, 22, 29);

    // ── Fonts ─────────────────────────────────────────────────────
    private static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD, 22);
    private static final Font FONT_SUB     = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONT_LABEL   = new Font("SansSerif", Font.BOLD, 13);
    private static final Font FONT_INPUT   = new Font("SansSerif", Font.PLAIN, 15);
    private static final Font FONT_BTN     = new Font("SansSerif", Font.BOLD, 14);
    private static final Font FONT_RESULT  = new Font("SansSerif", Font.BOLD, 28);
    private static final Font FONT_SMALL   = new Font("SansSerif", Font.PLAIN, 11);
    private static final Font FONT_TABLE   = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONT_TABLE_H = new Font("SansSerif", Font.BOLD, 12);

    // ── Fields ────────────────────────────────────────────────────
    private JTextField       totalField;
    private JTextField       peopleField;
    private JTextField       tipField;
    private JLabel           resultAmountLabel;
    private JLabel           resultDetailsLabel;
    private JLabel           statusLabel;
    private DefaultTableModel historyModel;
    private JTable           historyTable;
    private int              historyCount = 0;

    // ── Constructor ───────────────────────────────────────────────
    public BillSplitterTool() {
        setTitle("Bill Splitter");
        setSize(500, 680);
        setMinimumSize(new Dimension(420, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        // Inner content panel (scrollable)
        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                RadialGradientPaint rg = new RadialGradientPaint(
                    new Point(240, 80), 320,
                    new float[]{0f, 1f},
                    new Color[]{new Color(28, 34, 46), BG_DARK}
                );
                g2.setPaint(rg);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        content.setOpaque(true);
        content.setBackground(BG_DARK);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(30, 28, 30, 28));

        content.add(buildHeader());
        content.add(vgap(24));
        content.add(buildInputCard());
        content.add(vgap(14));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(FONT_SMALL);
        statusLabel.setForeground(ERROR_RED);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(statusLabel);
        content.add(vgap(10));

        content.add(buildButtonRow());
        content.add(vgap(20));
        content.add(buildResultCard());
        content.add(vgap(22));
        content.add(buildHistorySection());
        content.add(vgap(10));

        // Wrap in JScrollPane
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        styleScrollBar(scrollPane.getVerticalScrollBar());
        scrollPane.getViewport().setBackground(BG_DARK);

        add(scrollPane);
        setVisible(true);
    }

    // ── Section Builders ─────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel h = transparent();
        h.setLayout(new BoxLayout(h, BoxLayout.Y_AXIS));
        h.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel icon = new JLabel("₹");
        icon.setFont(new Font("SansSerif", Font.BOLD, 38));
        icon.setForeground(ACCENT_GOLD);
        icon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Bill Splitter");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Split bills fairly, every time.");
        sub.setFont(FONT_SUB);
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        h.add(icon); h.add(vgap(3));
        h.add(title); h.add(vgap(3));
        h.add(sub);
        return h;
    }

    private JPanel buildInputCard() {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(fieldGroup("Total Bill Amount (₹)", "e.g. 1200.00",
                totalField  = makeTextField()));
        card.add(vgap(16));
        card.add(fieldGroup("Number of People", "e.g. 4",
                peopleField = makeTextField()));
        card.add(vgap(16));
        card.add(fieldGroup("Tip Percentage (optional)", "e.g. 10",
                tipField    = makeTextField()));
        return card;
    }

    private JPanel buildButtonRow() {
        JPanel row = transparent();
        row.setLayout(new GridLayout(1, 2, 12, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton splitBtn = makePrimaryButton("Split Bill");
        JButton clearBtn = makeSecondaryButton("Clear");
        splitBtn.addActionListener(this);
        clearBtn.addActionListener(this);

        row.add(splitBtn);
        row.add(clearBtn);
        return row;
    }

    private JPanel buildResultCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(42, 36, 12),
                    getWidth(), getHeight(), new Color(22, 27, 20)
                );
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(new Color(255, 196, 0, 90));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.75f, 0.75f, getWidth()-1.5f, getHeight()-1.5f, 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 24, 20, 24));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel("EACH PERSON PAYS");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(ACCENT_GOLD);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        resultAmountLabel = new JLabel("₹0.00");
        resultAmountLabel.setFont(FONT_RESULT);
        resultAmountLabel.setForeground(TEXT_PRIMARY);
        resultAmountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        resultDetailsLabel = new JLabel("Enter details above and tap Split Bill");
        resultDetailsLabel.setFont(FONT_SMALL);
        resultDetailsLabel.setForeground(TEXT_MUTED);
        resultDetailsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lbl); card.add(vgap(5));
        card.add(resultAmountLabel); card.add(vgap(4));
        card.add(resultDetailsLabel);
        return card;
    }

    private JPanel buildHistorySection() {
        JPanel section = transparent();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Header row with "Clear History" button
        JPanel hdrRow = transparent();
        hdrRow.setLayout(new BorderLayout());
        hdrRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        hdrRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sectionTitle = new JLabel("Split History");
        sectionTitle.setFont(FONT_LABEL);
        sectionTitle.setForeground(TEXT_MUTED);

        JButton clearHist = makeTinyButton("Clear History");
        clearHist.addActionListener(ev -> {
            historyModel.setRowCount(0);
            historyCount = 0;
        });

        hdrRow.add(sectionTitle, BorderLayout.WEST);
        hdrRow.add(clearHist, BorderLayout.EAST);

        section.add(hdrRow);
        section.add(vgap(10));

        // Table model
        String[] cols = {"#", "Bill (₹)", "People", "Tip %", "Each Pays (₹)"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        historyTable = new JTable(historyModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(row % 2 == 0 ? BG_CARD : TABLE_ROW_ALT);
                c.setForeground(col == 4 ? SUCCESS_GREEN : TEXT_PRIMARY);
                c.setFont(FONT_TABLE);
                return c;
            }
        };
        historyTable.setBackground(BG_CARD);
        historyTable.setForeground(TEXT_PRIMARY);
        historyTable.setGridColor(BORDER_COLOR);
        historyTable.setRowHeight(32);
        historyTable.setShowVerticalLines(false);
        historyTable.setShowHorizontalLines(true);
        historyTable.setSelectionBackground(new Color(255, 196, 0, 40));
        historyTable.setSelectionForeground(TEXT_PRIMARY);
        historyTable.setFont(FONT_TABLE);
        historyTable.setFocusable(false);

        int[] widths = {30, 90, 65, 60, 110};
        for (int i = 0; i < widths.length; i++)
            historyTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Styled header
        JTableHeader tableHeader = historyTable.getTableHeader();
        tableHeader.setBackground(TABLE_HDR);
        tableHeader.setForeground(TEXT_MUTED);
        tableHeader.setFont(FONT_TABLE_H);
        tableHeader.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COLOR));
        tableHeader.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) tableHeader.getDefaultRenderer())
            .setHorizontalAlignment(SwingConstants.LEFT);

        // Scrollable table with fixed height
        JScrollPane tableScroll = new JScrollPane(historyTable);
        tableScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        tableScroll.getViewport().setBackground(BG_CARD);
        tableScroll.setPreferredSize(new Dimension(0, 180));
        tableScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        tableScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableScroll.getVerticalScrollBar().setUnitIncrement(10);
        styleScrollBar(tableScroll.getVerticalScrollBar());
        tableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        section.add(tableScroll);
        return section;
    }

    // ── Component Builders ───────────────────────────────────────

    private JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(22, 22, 22, 22));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private JPanel fieldGroup(String labelText, String placeholder, JTextField field) {
        JPanel group = transparent();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(FONT_LABEL);
        label.setForeground(TEXT_MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.putClientProperty("placeholder", placeholder);
        field.setForeground(TEXT_MUTED);
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText(""); field.setForeground(TEXT_PRIMARY);
                }
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder); field.setForeground(TEXT_MUTED);
                }
            }
        });

        group.add(label); group.add(vgap(6)); group.add(field);
        return group;
    }

    private JTextField makeTextField() {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                super.paintComponent(g); g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hasFocus() ? ACCENT_GOLD : BORDER_COLOR);
                g2.setStroke(new BasicStroke(hasFocus() ? 1.5f : 1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 10, 10));
                g2.dispose();
            }
        };
        tf.setOpaque(false);
        tf.setFont(FONT_INPUT);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_GOLD);
        tf.setBorder(new EmptyBorder(10, 14, 10, 14));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tf;
    }

    private JButton makePrimaryButton(String text) {
        JButton btn = new JButton(text) {
            boolean hovered = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? ACCENT_GOLD.brighter() : ACCENT_GOLD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setFont(getFont()); g2.setColor(BG_DARK);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        styleBtn(btn); return btn;
    }

    private JButton makeSecondaryButton(String text) {
        JButton btn = new JButton(text) {
            boolean hovered = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? new Color(48, 54, 61) : BTN_CLEAR_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(BORDER_COLOR); g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 10, 10));
                g2.setFont(getFont()); g2.setColor(TEXT_MUTED);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        styleBtn(btn); return btn;
    }

    private JButton makeTinyButton(String text) {
        JButton btn = new JButton(text) {
            boolean hovered = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? new Color(48, 54, 61) : BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
                g2.setColor(BORDER_COLOR); g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 6, 6));
                g2.setFont(getFont()); g2.setColor(TEXT_MUTED);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setFont(FONT_SMALL);
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(90, 24));
        return btn;
    }

    private void styleBtn(JButton btn) {
        btn.setFont(FONT_BTN);
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 46));
    }

    private void styleScrollBar(JScrollBar bar) {
        bar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(60, 67, 75);
                trackColor = BG_DARK;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
        bar.setBackground(BG_DARK);
    }

    private JPanel transparent() {
        JPanel p = new JPanel(); p.setOpaque(false); return p;
    }

    private Component vgap(int h) {
        return Box.createVerticalStrut(h);
    }

    private String fieldValue(JTextField field) {
        String ph = (String) field.getClientProperty("placeholder");
        String t  = field.getText().trim();
        return t.equals(ph) ? "" : t;
    }

    // ── Action Handler ────────────────────────────────────────────

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = ((JButton) e.getSource()).getText();

        if (cmd.equals("Split Bill")) {
            statusLabel.setText(" ");
            try {
                String totalText  = fieldValue(totalField);
                String peopleText = fieldValue(peopleField);
                String tipText    = fieldValue(tipField);

                if (totalText.isEmpty() || peopleText.isEmpty()) {
                    setStatus("Please fill in Total Bill and Number of People.", false);
                    return;
                }

                double bill   = Double.parseDouble(totalText);
                int    people = Integer.parseInt(peopleText);
                double tip    = tipText.isEmpty() ? 0 : Double.parseDouble(tipText);

                if (people <= 0)          { setStatus("People count must be at least 1.", false); return; }
                if (bill < 0 || tip < 0)  { setStatus("Values cannot be negative.", false); return; }

                double tipAmt    = (bill * tip) / 100;
                double total     = bill + tipAmt;
                double perPerson = total / people;

                resultAmountLabel.setText("₹" + String.format("%.2f", perPerson));
                resultDetailsLabel.setText(String.format(
                    "Total ₹%.2f  ·  Tip ₹%.2f  ·  %d people", total, tipAmt, people));
                resultAmountLabel.setForeground(SUCCESS_GREEN);
                setStatus("✓  Calculation complete", true);

                // Append to scrollable history table
                historyCount++;
                historyModel.addRow(new Object[]{
                    historyCount,
                    String.format("%.2f", bill),
                    people,
                    tip > 0 ? String.format("%.0f%%", tip) : "—",
                    String.format("%.2f", perPerson)
                });
                int last = historyTable.getRowCount() - 1;
                historyTable.scrollRectToVisible(historyTable.getCellRect(last, 0, true));

            } catch (NumberFormatException ex) {
                setStatus("Please enter valid numbers only.", false);
            }
        }

        if (cmd.equals("Clear")) {
            String[]     phs    = {"e.g. 1200.00", "e.g. 4", "e.g. 10"};
            JTextField[] fields = {totalField, peopleField, tipField};
            for (int i = 0; i < fields.length; i++) {
                fields[i].setText(phs[i]);
                fields[i].setForeground(TEXT_MUTED);
            }
            resultAmountLabel.setText("₹0.00");
            resultAmountLabel.setForeground(TEXT_PRIMARY);
            resultDetailsLabel.setText("Enter details above and tap Split Bill");
            statusLabel.setText(" ");
        }
    }

    private void setStatus(String msg, boolean ok) {
        statusLabel.setText(msg);
        statusLabel.setForeground(ok ? SUCCESS_GREEN : ERROR_RED);
    }

    // ── Main ─────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(BillSplitterTool::new);
    }
}
