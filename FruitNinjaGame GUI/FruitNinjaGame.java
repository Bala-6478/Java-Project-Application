import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class FruitNinjaGame extends JPanel implements ActionListener, MouseMotionListener, MouseListener {
    private ArrayList<Fruit> fruits;
    private ArrayList<Point> bladePath;
    private Timer timer;
    private int score;
    private int lives;
    private boolean gameActive;
    private Random random;
    private Image bombImage;
    private int missedFruits;
    private Font gameFont;
    private JFrame parentFrame;
    
    public FruitNinjaGame(JFrame frame) {
        this.parentFrame = frame;
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        addMouseMotionListener(this);
        addMouseListener(this);
        
        fruits = new ArrayList<>();
        bladePath = new ArrayList<>();
        timer = new Timer(16, this); // ~60 FPS
        random = new Random();
        score = 0;
        lives = 3;
        missedFruits = 0;
        gameActive = true;
        
        // Create a simple bomb image
        bombImage = createBombImage();
        
        // Create font for game text
        gameFont = new Font("Arial", Font.BOLD, 20);
        
        timer.start();
    }
    
    private Image createBombImage() {
        BufferedImage img = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillOval(0, 0, 30, 30);
        g2d.setColor(Color.RED);
        g2d.fillOval(5, 5, 20, 20);
        g2d.setColor(Color.WHITE);
        g2d.drawLine(10, 15, 20, 15);
        g2d.drawLine(15, 10, 15, 20);
        g2d.dispose();
        return img;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw background gradient
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gradient = new GradientPaint(0, 0, new Color(135, 206, 235), 0, getHeight(), new Color(0, 100, 200));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw ground
        g2d.setColor(new Color(34, 139, 34)); // Forest green
        g2d.fillRect(0, getHeight() - 50, getWidth(), 50);
        
        // Draw fruits and bombs
        for (Fruit fruit : fruits) {
            if (fruit.isSliced() && !fruit.isBomb()) {
                // Draw sliced fruit (two halves)
                g2d.setColor(fruit.getColor().darker());
                g2d.fillArc((int)fruit.getX() - 15, (int)fruit.getY(), 15, 15, 0, 180);
                g2d.fillArc((int)fruit.getX() + 5, (int)fruit.getY(), 15, 15, 0, -180);
            } else if (fruit.isBomb() && fruit.isSliced()) {
                // Draw exploded bomb
                g2d.setColor(Color.RED);
                g2d.fillOval((int)fruit.getX() - 20, (int)fruit.getY() - 20, 70, 70);
            } else if (fruit.isBomb()) {
                // Draw bomb
                g2d.drawImage(bombImage, (int)fruit.getX(), (int)fruit.getY(), this);
            } else {
                // Draw whole fruit
                g2d.setColor(fruit.getColor());
                g2d.fillOval((int)fruit.getX(), (int)fruit.getY(), 30, 30);
                
                // Draw highlight
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.fillOval((int)fruit.getX() + 5, (int)fruit.getY() + 5, 10, 10);
            }
        }
        
        // Draw blade trail
        if (bladePath.size() > 1) {
            g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.setColor(new Color(255, 255, 255, 200));
            for (int i = 1; i < bladePath.size(); i++) {
                Point p1 = bladePath.get(i-1);
                Point p2 = bladePath.get(i);
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
        
        // Draw score and lives
        g2d.setFont(gameFont);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Score: " + score, 20, 30);
        g2d.drawString("Lives: " + lives, 20, 60);
        
        // Draw instructions
        if (gameActive && fruits.isEmpty()) {
            g2d.setColor(new Color(255, 255, 255, 150));
            g2d.drawString("Drag mouse to slice fruits! Avoid bombs!", getWidth()/2 - 150, getHeight()/2);
        }
        
        // Game over message
        if (!gameActive) {
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            g2d.drawString("GAME OVER", getWidth()/2 - 120, getHeight()/2 - 20);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.drawString("Final Score: " + score, getWidth()/2 - 60, getHeight()/2 + 20);
            g2d.drawString("Click to play again", getWidth()/2 - 80, getHeight()/2 + 60);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameActive) return;
        
        // Add new fruits randomly
        if (random.nextInt(100) < 8) { // 8% chance each frame
            int type = random.nextInt(10); // 0-7 are fruits, 8-9 are bombs
            int x = random.nextInt(getWidth() - 50);
            boolean isBomb = (type >= 8);
            fruits.add(new Fruit(x, getHeight(), type % 8, isBomb));
        }
        
        // Update fruits
        for (int i = fruits.size() - 1; i >= 0; i--) {
            Fruit fruit = fruits.get(i);
            fruit.update();
            
            // Remove fruits that are off screen
            if (fruit.getY() < -50) {
                fruits.remove(i);
                if (!fruit.isSliced() && !fruit.isBomb()) {
                    missedFruits++;
                    if (missedFruits >= 5) {
                        lives--;
                        missedFruits = 0;
                        if (lives <= 0) {
                            gameOver("You ran out of lives!");
                        }
                    }
                }
            }
        }
        
        // Fade blade trail
        if (bladePath.size() > 20) {
            bladePath.remove(0);
        }
        
        repaint();
    }
    
    private void gameOver(String message) {
        gameActive = false;
        timer.stop();
        
        // Show game over dialog
        SwingUtilities.invokeLater(() -> {
            int option = JOptionPane.showOptionDialog(parentFrame,
                message + "\nYour final score: " + score + "\nWould you like to play again?",
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new Object[]{"Play Again", "Exit"},
                "Play Again");
            
            if (option == JOptionPane.YES_OPTION) {
                restartGame();
            } else {
                System.exit(0);
            }
        });
    }
    
    private void restartGame() {
        fruits.clear();
        bladePath.clear();
        score = 0;
        lives = 3;
        missedFruits = 0;
        gameActive = true;
        timer.start();
        repaint();
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        if (!gameActive) return;
        
        bladePath.add(e.getPoint());
        
        // Check for fruit slices
        for (Fruit fruit : fruits) {
            if (!fruit.isSliced()) {
                for (int i = 1; i < bladePath.size(); i++) {
                    Point p1 = bladePath.get(i-1);
                    Point p2 = bladePath.get(i);
                    
                    if (lineIntersectsCircle(p1.x, p1.y, p2.x, p2.y, 
                                           (int)fruit.getX() + 15, (int)fruit.getY() + 15, 15)) {
                        fruit.slice();
                        
                        if (fruit.isBomb()) {
                            // Bomb explosion - game over
                            gameOver("You hit a bomb!");
                        } else {
                            // Fruit slice - add to score
                            score += 10;
                            missedFruits = Math.max(0, missedFruits - 1);
                        }
                        
                        break;
                    }
                }
            }
        }
    }
    
    private boolean lineIntersectsCircle(int x1, int y1, int x2, int y2, int cx, int cy, int r) {
        // Calculate the distance between the line segment and circle center
        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.sqrt(dx*dx + dy*dy);
        
        // Normalize direction vector
        dx /= length;
        dy /= length;
        
        // Calculate projection of circle center onto line
        double t = dx*(cx - x1) + dy*(cy - y1);
        
        // Calculate closest point on line segment to circle center
        double closestX, closestY;
        if (t < 0) {
            closestX = x1;
            closestY = y1;
        } else if (t > length) {
            closestX = x2;
            closestY = y2;
        } else {
            closestX = x1 + t * dx;
            closestY = y1 + t * dy;
        }
        
        // Calculate distance between closest point and circle center
        double distanceX = cx - closestX;
        double distanceY = cy - closestY;
        double distance = Math.sqrt(distanceX*distanceX + distanceY*distanceY);
        
        return distance <= r;
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {}
    
    @Override
    public void mouseClicked(MouseEvent e) {
        if (!gameActive) {
            restartGame();
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        bladePath.clear();
        bladePath.add(e.getPoint());
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        bladePath.clear();
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {}
    
    @Override
    public void mouseExited(MouseEvent e) {}
    
    // Fruit class
    private class Fruit {
        private double x, y;
        private double velocityX, velocityY;
        private boolean sliced;
        private boolean bomb;
        private int type;
        
        public Fruit(int x, int y, int type, boolean isBomb) {
            this.x = x;
            this.y = y;
            this.velocityX = random.nextDouble() * 2 - 1; // -1 to 1
            this.velocityY = -random.nextDouble() * 10 - 5; // Upward velocity
            this.sliced = false;
            this.type = type;
            this.bomb = isBomb;
        }
        
        public void update() {
            x += velocityX;
            y += velocityY;
            velocityY += 0.2; // Gravity
            
            if (sliced && !bomb) {
                velocityY += 0.5; // Faster fall when sliced
            }
        }
        
        public void slice() {
            sliced = true;
            if (!bomb) {
                velocityX *= 1.5; // Add some extra force when sliced
                velocityY *= 1.5;
            }
        }
        
        public double getX() { return x; }
        public double getY() { return y; }
        public boolean isSliced() { return sliced; }
        public boolean isBomb() { return bomb; }
        public int getType() { return type; }
        
        public Color getColor() {
            if (bomb) return Color.BLACK;
            
            switch(type) {
                case 0: return Color.RED;      // Apple
                case 1: return Color.ORANGE;   // Orange
                case 2: return Color.GREEN;    // Watermelon
                case 3: return Color.YELLOW;   // Banana
                case 4: return Color.PINK;     // Peach
                case 5: return Color.MAGENTA;  // Grapes
                case 6: return Color.CYAN;     // Blueberry
                case 7: return Color.WHITE;    // Coconut
                default: return Color.PINK;
            }
        }
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Fruit Ninja");
        FruitNinjaGame game = new FruitNinjaGame(frame);
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}