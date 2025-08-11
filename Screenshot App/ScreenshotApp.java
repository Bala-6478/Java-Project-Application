import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

class ScreenshotApp extends JFrame {

    private JButton captureButton;

    public ScreenshotApp() {
        setTitle("Screenshot Capturer");
        setSize(300, 100);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        captureButton = new JButton("Capture Screenshot");
        captureButton.addActionListener(e -> {
            new Thread(() -> {
                try {
                    Thread.sleep(3000); // 3 second delay
                    Rectangle area = selectCaptureArea();
                    if (area != null) {
                        captureAndSave(area);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }).start();
            JOptionPane.showMessageDialog(this, "Screenshot will be taken in 3 seconds. Select area after this.");
        });

        add(captureButton);
    }

    private void captureAndSave(Rectangle area) throws Exception {
        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(area);

        // Let user choose file location
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Screenshot");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        File defaultFile = new File("screenshot_" + sdf.format(new Date()) + ".png");
        fileChooser.setSelectedFile(defaultFile);

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            ImageIO.write(image, "png", fileToSave);
            JOptionPane.showMessageDialog(this, "Screenshot saved to:\n" + fileToSave.getAbsolutePath());
        }
    }

    private Rectangle selectCaptureArea() throws Exception {
        // Full screen transparent window
        JWindow window = new JWindow();
        window.setAlwaysOnTop(true);
        window.setOpacity(0.5f);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        window.setBounds(0, 0, screenSize.width, screenSize.height);

        Point[] points = new Point[2];

        window.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                points[0] = e.getPoint();
            }

            public void mouseReleased(MouseEvent e) {
                points[1] = e.getPoint();
                window.setVisible(false);
                window.dispose();
            }
        });

        window.setVisible(true);

        // Wait until second point is captured
        while (points[1] == null) {
            Thread.sleep(100);
        }

        int x = Math.min(points[0].x, points[1].x);
        int y = Math.min(points[0].y, points[1].y);
        int w = Math.abs(points[0].x - points[1].x);
        int h = Math.abs(points[0].y - points[1].y);
        return new Rectangle(x, y, w, h);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ScreenshotApp app = new ScreenshotApp();
            app.setVisible(true);
        });
    }
}