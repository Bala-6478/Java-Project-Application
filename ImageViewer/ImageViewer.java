import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.Timer;

public class ImageViewer extends JFrame implements ActionListener {

    JLabel imageLabel;
    JButton openBtn, openFileBtn, nextBtn, prevBtn, zoomInBtn, zoomOutBtn, slideBtn;

    File[] imageFiles;
    int currentIndex = 0;

    double zoom = 1.0;
    Timer slideshowTimer;

    public ImageViewer() {
        setTitle("Modern Image Viewer");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Image Label
        imageLabel = new JLabel("", JLabel.CENTER);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(30, 30, 30));
        add(new JScrollPane(imageLabel), BorderLayout.CENTER);

        // Panel
        JPanel panel = new JPanel();
        panel.setBackground(new Color(40, 40, 40));

        openBtn = createButton("Open Folder");
        openFileBtn = createButton("Open Image");
        prevBtn = createButton("Previous");
        nextBtn = createButton("Next");
        zoomInBtn = createButton("Zoom +");
        zoomOutBtn = createButton("Zoom -");
        slideBtn = createButton("Slideshow");

        panel.add(openBtn);
        panel.add(openFileBtn);
        panel.add(prevBtn);
        panel.add(nextBtn);
        panel.add(zoomInBtn);
        panel.add(zoomOutBtn);
        panel.add(slideBtn);

        add(panel, BorderLayout.SOUTH);

        // Actions
        openBtn.addActionListener(this);
        openFileBtn.addActionListener(this);
        nextBtn.addActionListener(this);
        prevBtn.addActionListener(this);
        zoomInBtn.addActionListener(this);
        zoomOutBtn.addActionListener(this);
        slideBtn.addActionListener(this);

        // Drag & Drop
        imageLabel.setTransferHandler(new TransferHandler() {
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.javaFileListFlavor);
            }

            public boolean importData(TransferSupport support) {
                try {
                    java.util.List<File> files = (java.util.List<File>) support.getTransferable()
                            .getTransferData(java.awt.datatransfer.DataFlavor.javaFileListFlavor);

                    imageFiles = files.toArray(new File[0]);
                    currentIndex = 0;
                    showImage();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        // Slideshow Timer
        slideshowTimer = new Timer(2000, e -> nextImage());

        setVisible(true);
    }

    // Styled Button
    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(70, 70, 70));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    // Load folder
    public void loadImages(File folder) {
        imageFiles = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".jpg") ||
                name.toLowerCase().endsWith(".png") ||
                name.toLowerCase().endsWith(".jpeg"));

        currentIndex = 0;
        zoom = 1.0;
        showImage();
    }

    // Load single image
    public void loadSingleImage(File file) {
        imageFiles = new File[]{file};
        currentIndex = 0;
        zoom = 1.0;
        showImage();
    }

    // Show Image
    public void showImage() {
        if (imageFiles != null && imageFiles.length > 0) {
            ImageIcon icon = new ImageIcon(imageFiles[currentIndex].getAbsolutePath());
            Image img = icon.getImage();

            int width = (int) (img.getWidth(null) * zoom);
            int height = (int) (img.getHeight(null) * zoom);

            Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaled));
        }
    }

    // Next Image
    public void nextImage() {
        if (imageFiles != null && currentIndex < imageFiles.length - 1) {
            currentIndex++;
            showImage();
        }
    }

    // Previous Image
    public void prevImage() {
        if (imageFiles != null && currentIndex > 0) {
            currentIndex--;
            showImage();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == openBtn) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                loadImages(chooser.getSelectedFile());
            }
        }

        if (e.getSource() == openFileBtn) {
            JFileChooser chooser = new JFileChooser();

            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                loadSingleImage(chooser.getSelectedFile());
            }
        }

        if (e.getSource() == nextBtn) nextImage();
        if (e.getSource() == prevBtn) prevImage();

        if (e.getSource() == zoomInBtn) {
            zoom += 0.2;
            showImage();
        }

        if (e.getSource() == zoomOutBtn) {
            if (zoom > 0.2) zoom -= 0.2;
            showImage();
        }

        if (e.getSource() == slideBtn) {
            if (slideshowTimer.isRunning()) {
                slideshowTimer.stop();
                slideBtn.setText("Slideshow");
            } else {
                slideshowTimer.start();
                slideBtn.setText("Stop");
            }
        }
    }

    public static void main(String[] args) {
        new ImageViewer();
    }
}