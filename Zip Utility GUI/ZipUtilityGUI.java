import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.zip.*;

class ZipUtilityGUI extends JFrame {
    private JButton compressButton, decompressButton;

    public ZipUtilityGUI() {
        setTitle("📦 ZIP Utility Tool");
        setSize(400, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center screen
        setLayout(new GridLayout(2, 1, 10, 10));

        compressButton = new JButton("🗜️ Compress File");
        decompressButton = new JButton("📂 Decompress ZIP");

        compressButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        decompressButton.setFont(new Font("SansSerif", Font.BOLD, 16));

        add(compressButton);
        add(decompressButton);

        compressButton.addActionListener(e -> compressFile());
        decompressButton.addActionListener(e -> decompressFile());
    }

    private void compressFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select File to Compress");
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File inputFile = chooser.getSelectedFile();

            JFileChooser saveChooser = new JFileChooser();
            saveChooser.setDialogTitle("Save As ZIP File");
            saveChooser.setSelectedFile(new File(inputFile.getName() + ".zip"));

            int saveResult = saveChooser.showSaveDialog(this);
            if (saveResult == JFileChooser.APPROVE_OPTION) {
                File zipFile = saveChooser.getSelectedFile();
                try (
                        FileOutputStream fos = new FileOutputStream(zipFile);
                        ZipOutputStream zos = new ZipOutputStream(fos);
                        FileInputStream fis = new FileInputStream(inputFile)
                ) {
                    zos.putNextEntry(new ZipEntry(inputFile.getName()));
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                    JOptionPane.showMessageDialog(this, "✅ File Compressed: " + zipFile.getName());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
                }
            }
        }
    }

    private void decompressFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select ZIP File to Decompress");
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File zipFile = chooser.getSelectedFile();

            JFileChooser folderChooser = new JFileChooser();
            folderChooser.setDialogTitle("Select Output Folder");
            folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int folderResult = folderChooser.showOpenDialog(this);

            if (folderResult == JFileChooser.APPROVE_OPTION) {
                File outputDir = folderChooser.getSelectedFile();

                try (
                        FileInputStream fis = new FileInputStream(zipFile);
                        ZipInputStream zis = new ZipInputStream(fis)
                ) {
                    ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null) {
                        File newFile = new File(outputDir, entry.getName());
                        try (FileOutputStream fos = new FileOutputStream(newFile)) {
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                        zis.closeEntry();
                    }
                    JOptionPane.showMessageDialog(this, "✅ ZIP Extracted to: " + outputDir.getAbsolutePath());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ZipUtilityGUI().setVisible(true);
        });
    }
}
