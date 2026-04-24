import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.Timer;

public class ImageViewer extends JFrame implements ActionListener {

    // --- UI Components ---
    private JLabel imageLabel;
    private JButton openBtn, openFileBtn, nextBtn, prevBtn, zoomInBtn, zoomOutBtn,
            slideBtn, fitBtn;
    private JLabel statusLabel;
    private JSlider speedSlider;

    // --- State ---
    private File[] imageFiles;
    private int currentIndex = 0;
    private double zoom = 1.0;
    private static final double ZOOM_MIN = 0.1;
    private static final double ZOOM_MAX = 5.0;
    private static final double ZOOM_STEP = 0.2;

    private Timer slideshowTimer;
    private boolean isFitToWindow = false;

    // --- Supported formats ---
    private static final String[] SUPPORTED_FORMATS = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};

    public ImageViewer() {
        setTitle("Image Viewer");
        setSize(960, 660);
        setMinimumSize(new Dimension(600, 400));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        buildImagePanel();
        buildControlPanel();
        buildStatusBar();
        setupKeyboardShortcuts();
        setupDragAndDrop();

        slideshowTimer = new Timer(2000, e -> nextImage());

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // -------------------------------------------------------------------------
    // UI Construction
    // -------------------------------------------------------------------------

    private void buildImagePanel() {
        imageLabel = new JLabel("Drag & Drop images here, or use 'Open' buttons", JLabel.CENTER);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(28, 28, 30));
        imageLabel.setForeground(new Color(140, 140, 145));
        imageLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JScrollPane scroll = new JScrollPane(imageLabel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(28, 28, 30));
        add(scroll, BorderLayout.CENTER);
    }

    private void buildControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 8));
        panel.setBackground(new Color(44, 44, 46));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(60, 60, 65)));

        openBtn     = createButton("📁 Open Folder", "Open a folder of images  [Ctrl+O]");
        openFileBtn = createButton("🖼 Open Image",  "Open a single image file [Ctrl+Shift+O]");
        prevBtn     = createButton("◀ Prev",         "Previous image           [←]");
        nextBtn     = createButton("Next ▶",         "Next image               [→]");
        zoomInBtn   = createButton("＋ Zoom",         "Zoom in                  [+]");
        zoomOutBtn  = createButton("－ Zoom",         "Zoom out                 [−]");
        fitBtn      = createButton("⊡ Fit",          "Fit image to window      [F]");
        slideBtn    = createButton("▶ Slideshow",    "Start/stop slideshow     [Space]");

        // Speed label + slider
        JLabel speedLabel = new JLabel("Speed:");
        speedLabel.setForeground(new Color(180, 180, 185));
        speedLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        speedSlider = new JSlider(500, 5000, 2000);
        speedSlider.setInverted(true); // left = faster
        speedSlider.setPreferredSize(new Dimension(90, 24));
        speedSlider.setOpaque(false);
        speedSlider.setToolTipText("Slideshow speed (left = faster)");
        speedSlider.addChangeListener((ChangeEvent ce) -> {
            slideshowTimer.setDelay(speedSlider.getValue());
        });

        panel.add(openBtn);
        panel.add(openFileBtn);
        panel.add(makeSeparator());
        panel.add(prevBtn);
        panel.add(nextBtn);
        panel.add(makeSeparator());
        panel.add(zoomInBtn);
        panel.add(zoomOutBtn);
        panel.add(fitBtn);
        panel.add(makeSeparator());
        panel.add(slideBtn);
        panel.add(speedLabel);
        panel.add(speedSlider);

        for (JButton btn : new JButton[]{openBtn, openFileBtn, nextBtn, prevBtn,
                zoomInBtn, zoomOutBtn, fitBtn, slideBtn}) {
            btn.addActionListener(this);
        }

        add(panel, BorderLayout.SOUTH);
    }

    private void buildStatusBar() {
        statusLabel = new JLabel("No image loaded");
        statusLabel.setForeground(new Color(160, 160, 165));
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(36, 36, 38));

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(36, 36, 38));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(55, 55, 58)));
        bar.add(statusLabel, BorderLayout.WEST);
        add(bar, BorderLayout.NORTH);
    }

    // -------------------------------------------------------------------------
    // Keyboard Shortcuts
    // -------------------------------------------------------------------------

    private void setupKeyboardShortcuts() {
        JRootPane root = getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();

        bindKey(im, am, "RIGHT",          KeyEvent.VK_RIGHT, 0,             () -> nextImage());
        bindKey(im, am, "LEFT",           KeyEvent.VK_LEFT,  0,             () -> prevImage());
        bindKey(im, am, "ZOOM_IN_PLUS",   KeyEvent.VK_PLUS,  0,             () -> zoomIn());
        bindKey(im, am, "ZOOM_IN_EQUALS", KeyEvent.VK_EQUALS,0,             () -> zoomIn());
        bindKey(im, am, "ZOOM_OUT",       KeyEvent.VK_MINUS, 0,             () -> zoomOut());
        bindKey(im, am, "FIT",            KeyEvent.VK_F,     0,             () -> toggleFit());
        bindKey(im, am, "SLIDE",          KeyEvent.VK_SPACE, 0,             () -> toggleSlideshow());
        bindKey(im, am, "OPEN",           KeyEvent.VK_O,     InputEvent.CTRL_DOWN_MASK, () -> openFolder());
        bindKey(im, am, "ESC",            KeyEvent.VK_ESCAPE,0,             () -> {
            if (slideshowTimer.isRunning()) toggleSlideshow();
        });
    }

    private void bindKey(InputMap im, ActionMap am, String name,
                         int keyCode, int modifiers, Runnable action) {
        im.put(KeyStroke.getKeyStroke(keyCode, modifiers), name);
        am.put(name, new AbstractAction() {
            public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }

    // -------------------------------------------------------------------------
    // Drag & Drop
    // -------------------------------------------------------------------------

    private void setupDragAndDrop() {
        imageLabel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            @SuppressWarnings("unchecked")
            public boolean importData(TransferSupport support) {
                try {
                    java.util.List<File> files =
                            (java.util.List<File>) support.getTransferable()
                                    .getTransferData(DataFlavor.javaFileListFlavor);

                    // If a single directory is dropped, load it as a folder
                    if (files.size() == 1 && files.get(0).isDirectory()) {
                        loadImages(files.get(0));
                        return true;
                    }

                    // Otherwise filter for supported image files
                    File[] imgs = files.stream()
                            .filter(f -> isSupportedImage(f.getName()))
                            .sorted(Comparator.comparing(File::getName))
                            .toArray(File[]::new);

                    if (imgs.length > 0) {
                        imageFiles = imgs;
                        currentIndex = 0;
                        zoom = 1.0;
                        isFitToWindow = false;
                        showImage();
                    }
                    return true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return false;
            }
        });
    }

    // -------------------------------------------------------------------------
    // Image Loading
    // -------------------------------------------------------------------------

    public void loadImages(File folder) {
        File[] files = folder.listFiles((dir, name) -> isSupportedImage(name));
        if (files == null || files.length == 0) {
            JOptionPane.showMessageDialog(this, "No supported images found in folder.",
                    "Empty Folder", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Arrays.sort(files, Comparator.comparing(File::getName));
        imageFiles = files;
        currentIndex = 0;
        zoom = 1.0;
        isFitToWindow = false;
        showImage();
    }

    public void loadSingleImage(File file) {
        if (!isSupportedImage(file.getName())) {
            JOptionPane.showMessageDialog(this, "Unsupported file format.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        imageFiles = new File[]{file};
        currentIndex = 0;
        zoom = 1.0;
        isFitToWindow = false;
        showImage();
    }

    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    public void showImage() {
        if (imageFiles == null || imageFiles.length == 0) return;

        File file = imageFiles[currentIndex];
        ImageIcon icon = new ImageIcon(file.getAbsolutePath());
        Image img = icon.getImage();

        int imgW = img.getWidth(null);
        int imgH = img.getHeight(null);

        if (imgW <= 0 || imgH <= 0) {
            imageLabel.setText("Could not load: " + file.getName());
            imageLabel.setIcon(null);
            return;
        }

        int displayW, displayH;
        if (isFitToWindow) {
            Dimension pane = imageLabel.getParent().getSize();
            double scaleX = (double) pane.width  / imgW;
            double scaleY = (double) pane.height / imgH;
            double scale  = Math.min(scaleX, scaleY);
            displayW = (int) (imgW * scale);
            displayH = (int) (imgH * scale);
            zoom = scale; // keep zoom in sync for status display
        } else {
            displayW = (int) (imgW * zoom);
            displayH = (int) (imgH * zoom);
        }

        Image scaled = img.getScaledInstance(displayW, displayH, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(scaled));
        imageLabel.setText("");

        // Update title and status bar
        setTitle(String.format("Image Viewer — [%d / %d] %s",
                currentIndex + 1, imageFiles.length, file.getName()));
        statusLabel.setText(String.format("  %d / %d  |  %s  |  %dx%d  |  Zoom: %.0f%%",
                currentIndex + 1, imageFiles.length,
                file.getName(), imgW, imgH, zoom * 100));
    }

    // -------------------------------------------------------------------------
    // Navigation & Zoom
    // -------------------------------------------------------------------------

    public void nextImage() {
        if (imageFiles == null) return;
        currentIndex = (currentIndex + 1) % imageFiles.length; // loop around
        showImage();
    }

    public void prevImage() {
        if (imageFiles == null) return;
        currentIndex = (currentIndex - 1 + imageFiles.length) % imageFiles.length;
        showImage();
    }

    private void zoomIn() {
        isFitToWindow = false;
        zoom = Math.min(zoom + ZOOM_STEP, ZOOM_MAX);
        showImage();
    }

    private void zoomOut() {
        isFitToWindow = false;
        zoom = Math.max(zoom - ZOOM_STEP, ZOOM_MIN);
        showImage();
    }

    private void toggleFit() {
        isFitToWindow = !isFitToWindow;
        if (!isFitToWindow) zoom = 1.0;
        fitBtn.setText(isFitToWindow ? "⊡ Actual" : "⊡ Fit");
        showImage();
    }

    private void toggleSlideshow() {
        if (slideshowTimer.isRunning()) {
            slideshowTimer.stop();
            slideBtn.setText("▶ Slideshow");
        } else {
            slideshowTimer.setDelay(speedSlider.getValue());
            slideshowTimer.start();
            slideBtn.setText("⏹ Stop");
        }
    }

    private void openFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            loadImages(chooser.getSelectedFile());
        }
    }

    // -------------------------------------------------------------------------
    // ActionListener
    // -------------------------------------------------------------------------

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == openBtn) {
            openFolder();
        } else if (src == openFileBtn) {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                loadSingleImage(chooser.getSelectedFile());
            }
        } else if (src == nextBtn)    nextImage();
        else if (src == prevBtn)      prevImage();
        else if (src == zoomInBtn)    zoomIn();
        else if (src == zoomOutBtn)   zoomOut();
        else if (src == fitBtn)       toggleFit();
        else if (src == slideBtn)     toggleSlideshow();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private JButton createButton(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(72, 72, 76));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(90, 90, 95), 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        btn.setToolTipText(tooltip);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JSeparator makeSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 22));
        sep.setForeground(new Color(80, 80, 84));
        return sep;
    }

    private boolean isSupportedImage(String name) {
        String lower = name.toLowerCase();
        for (String fmt : SUPPORTED_FORMATS) {
            if (lower.endsWith(fmt)) return true;
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Entry Point
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(ImageViewer::new);
    }
}
