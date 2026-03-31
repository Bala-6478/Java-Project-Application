// Filename: ResumeBuilder.java
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ResumeBuilder extends JFrame {
    // Personal info
    private JTextField tfName, tfEmail, tfPhone, tfWebsite, tfDob, tfAddress, tfSignature;

    // Resume sections
    private JTextArea taSummary, taEducation, taExperience, taSkills, taProjects, taCertificates;

    // Buttons
    private JButton btnPreview, btnSaveHTML, btnSaveText, btnClear, btnExit;

    public ResumeBuilder() {
        setTitle("Resume Builder");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(950, 750);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(main);

        // Top - Basic Info
        JPanel top = new JPanel(new GridLayout(3, 3, 8, 8));
        tfName = new JTextField();
        tfEmail = new JTextField();
        tfPhone = new JTextField();
        tfWebsite = new JTextField();
        tfDob = new JTextField();
        tfAddress = new JTextField();
        tfSignature = new JTextField();

        top.add(new LabeledPanel("Full Name", tfName));
        top.add(new LabeledPanel("Email", tfEmail));
        top.add(new LabeledPanel("Phone", tfPhone));
        top.add(new LabeledPanel("Website / LinkedIn", tfWebsite));
        top.add(new LabeledPanel("Date of Birth", tfDob));
        top.add(new LabeledPanel("Address", tfAddress));
        top.add(new LabeledPanel("Signature (text)", tfSignature));

        main.add(top, BorderLayout.NORTH);

        // Center - Sections
        JPanel center = new JPanel(new GridLayout(3, 2, 8, 8));
        taSummary = new JTextArea(5, 20);
        taEducation = new JTextArea(5, 20);
        taExperience = new JTextArea(5, 20);
        taSkills = new JTextArea(5, 20);
        taProjects = new JTextArea(5, 20);
        taCertificates = new JTextArea(5, 20);

        center.add(new LabeledPanel("Professional Summary", new JScrollPane(taSummary)));
        center.add(new LabeledPanel("Education", new JScrollPane(taEducation)));
        center.add(new LabeledPanel("Experience", new JScrollPane(taExperience)));
        center.add(new LabeledPanel("Skills", new JScrollPane(taSkills)));
        center.add(new LabeledPanel("Projects", new JScrollPane(taProjects)));
        center.add(new LabeledPanel("Certificates", new JScrollPane(taCertificates)));

        main.add(center, BorderLayout.CENTER);

        // Bottom - Buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnPreview = new JButton("Preview");
        btnSaveHTML = new JButton("Save as HTML");
        btnSaveText = new JButton("Save as TXT");
        btnClear = new JButton("Clear");
        btnExit = new JButton("Exit");

        bottom.add(btnPreview);
        bottom.add(btnSaveHTML);
        bottom.add(btnSaveText);
        bottom.add(btnClear);
        bottom.add(btnExit);

        main.add(bottom, BorderLayout.SOUTH);

        // Actions
        btnPreview.addActionListener(e -> showPreview());
        btnSaveHTML.addActionListener(e -> saveFile("html"));
        btnSaveText.addActionListener(e -> saveFile("txt"));
        btnClear.addActionListener(e -> clearAll());
        btnExit.addActionListener(e -> System.exit(0));
    }

    private void showPreview() {
        String html = generateHTML();
        JEditorPane editor = new JEditorPane("text/html", html);
        editor.setEditable(false);
        JScrollPane sp = new JScrollPane(editor);
        sp.setPreferredSize(new Dimension(800, 600));

        JOptionPane.showMessageDialog(this, sp, "Resume Preview", JOptionPane.PLAIN_MESSAGE);
    }

    private void saveFile(String type) {
        JFileChooser chooser = new JFileChooser();
        if ("html".equals(type)) {
            chooser.setFileFilter(new FileNameExtensionFilter("HTML file", "html"));
        } else {
            chooser.setFileFilter(new FileNameExtensionFilter("Text file", "txt"));
        }
        int r = chooser.showSaveDialog(this);
        if (r != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        if ("html".equals(type) && !f.getName().toLowerCase().endsWith(".html")) f = new File(f.getAbsolutePath() + ".html");
        if ("txt".equals(type) && !f.getName().toLowerCase().endsWith(".txt")) f = new File(f.getAbsolutePath() + ".txt");

        try (Writer w = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
            if ("html".equals(type)) {
                w.write(generateHTML());
            } else {
                w.write(generatePlainText());
            }
            JOptionPane.showMessageDialog(this, "Saved to " + f.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String generateHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!doctype html><html><head><meta charset='utf-8'><title>")
          .append(escape(tfName.getText())).append("</title>")
          .append("<style>")
          .append("body{font-family:Arial,Helvetica,sans-serif;max-width:900px;margin:30px auto;}")
          .append(".header{border-bottom:2px solid #333;margin-bottom:20px;padding-bottom:10px}")
          .append(".name{font-size:28px;font-weight:bold}")
          .append(".section{margin-bottom:15px}")
          .append(".section h3{text-transform:uppercase;font-size:14px;margin-bottom:5px;color:#333}")
          .append("</style></head><body>");

        // Header
        sb.append("<div class='header'><div class='name'>").append(escape(tfName.getText())).append("</div>")
          .append("<div>").append(escape(tfEmail.getText())).append(" | ").append(escape(tfPhone.getText()))
          .append(" | ").append(escape(tfWebsite.getText())).append("</div>")
          .append("<div>DOB: ").append(escape(tfDob.getText())).append("</div>")
          .append("<div>Address: ").append(escape(tfAddress.getText())).append("</div>")
          .append("</div>");

        // Sections
        addSection(sb, "Summary", taSummary.getText());
        addListSection(sb, "Experience", taExperience.getText());
        addListSection(sb, "Education", taEducation.getText());
        addListSection(sb, "Projects", taProjects.getText());
        addListSection(sb, "Certificates", taCertificates.getText());
        addSection(sb, "Skills", taSkills.getText());

        // Signature
        if (!tfSignature.getText().trim().isEmpty()) {
            sb.append("<div class='section'><h3>Signature</h3><p>")
              .append(escape(tfSignature.getText())).append("</p></div>");
        }

        sb.append("</body></html>");
        return sb.toString();
    }

    private String generatePlainText() {
        StringBuilder sb = new StringBuilder();
        sb.append(tfName.getText()).append("\n")
          .append("Email: ").append(tfEmail.getText()).append("\n")
          .append("Phone: ").append(tfPhone.getText()).append("\n")
          .append("Website: ").append(tfWebsite.getText()).append("\n")
          .append("DOB: ").append(tfDob.getText()).append("\n")
          .append("Address: ").append(tfAddress.getText()).append("\n\n");

        sb.append("Summary:\n").append(taSummary.getText()).append("\n\n");
        sb.append("Experience:\n").append(taExperience.getText()).append("\n\n");
        sb.append("Education:\n").append(taEducation.getText()).append("\n\n");
        sb.append("Projects:\n").append(taProjects.getText()).append("\n\n");
        sb.append("Certificates:\n").append(taCertificates.getText()).append("\n\n");
        sb.append("Skills:\n").append(taSkills.getText()).append("\n\n");
        sb.append("Signature: ").append(tfSignature.getText()).append("\n");
        return sb.toString();
    }

    private void addSection(StringBuilder sb, String title, String content) {
        if (content.trim().isEmpty()) return;
        sb.append("<div class='section'><h3>").append(title).append("</h3><p>")
          .append(escape(content).replace("\n","<br/>")).append("</p></div>");
    }

    private void addListSection(StringBuilder sb, String title, String content) {
        if (content.trim().isEmpty()) return;
        sb.append("<div class='section'><h3>").append(title).append("</h3><ul>");
        for (String line : content.split("\\r?\\n")) {
            if (!line.trim().isEmpty()) sb.append("<li>").append(escape(line)).append("</li>");
        }
        sb.append("</ul></div>");
    }

    private void clearAll() {
        tfName.setText(""); tfEmail.setText(""); tfPhone.setText(""); tfWebsite.setText("");
        tfDob.setText(""); tfAddress.setText(""); tfSignature.setText("");
        taSummary.setText(""); taEducation.setText(""); taExperience.setText("");
        taSkills.setText(""); taProjects.setText(""); taCertificates.setText("");
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    private static class LabeledPanel extends JPanel {
        LabeledPanel(String label, Component comp) {
            super(new BorderLayout(4,4));
            add(new JLabel(label), BorderLayout.NORTH);
            add(comp, BorderLayout.CENTER);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new ResumeBuilder().setVisible(true);
        });
    }
}
