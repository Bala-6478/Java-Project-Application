import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Main class for the Snake Game.
 * Launches the game on the Event Dispatch Thread as required by Swing.
 */
public class SnakeGame extends JFrame {

    public SnakeGame() {
        add(new GamePanel());
        setTitle("Snake Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SnakeGame::new);
    }
}

/**
 * Represents a single point on the game grid.
 */
class Point {
    int x, y;
    Point(int x, int y) { this.x = x; this.y = y; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point)) return false;
        Point p = (Point) o;
        return x == p.x && y == p.y;
    }
}

/**
 * Enum for snake direction — eliminates magic char comparisons and
 * makes reversal checks clean and readable.
 */
enum Direction {
    UP, DOWN, LEFT, RIGHT;

    /** Returns true if moving in this direction would instantly reverse into {@code current}. */
    boolean isOpposite(Direction current) {
        return (this == UP   && current == DOWN)
            || (this == DOWN  && current == UP)
            || (this == LEFT  && current == RIGHT)
            || (this == RIGHT && current == LEFT);
    }
}

/**
 * Enum for the overall game state machine.
 */
enum GameState { WAITING, RUNNING, PAUSED, GAME_OVER }

/**
 * GamePanel contains all game logic and rendering.
 *
 * Key improvements over original:
 *  - Bug fix: collision loop used index bodyParts (out-of-bounds); fixed to bodyParts-1.
 *  - Bug fix: direction change buffered so rapid key presses can't cause self-reversal.
 *  - Feature: pause support (P key).
 *  - Feature: persistent high score across restarts.
 *  - Feature: progressive speed — game gets faster every 5 apples.
 *  - Feature: grid overlay, rounded snake segments, gradient-style coloring.
 *  - Code quality: Direction enum replaces magic chars; Point class replaces parallel arrays;
 *    GameState enum replaces boolean flags; magic numbers extracted to named constants.
 */
class GamePanel extends JPanel implements ActionListener {

    // --- Layout constants ---
    static final int UNIT        = 25;   // pixels per grid cell
    static final int COLS        = 24;
    static final int ROWS        = 24;
    static final int SCREEN_W    = COLS * UNIT;
    static final int SCREEN_H    = ROWS * UNIT;

    // --- Timing constants ---
    static final int BASE_DELAY  = 120;  // ms per tick at level 1
    static final int MIN_DELAY   = 50;   // fastest the game can go
    static final int SPEED_STEP  = 10;   // ms shaved off per level
    static final int APPLES_PER_LEVEL = 5;

    // --- Colours ---
    static final Color COL_BG         = new Color(15, 20, 15);
    static final Color COL_GRID       = new Color(25, 35, 25);
    static final Color COL_HEAD       = new Color(100, 255, 100);
    static final Color COL_BODY_LIGHT = new Color(50, 200, 50);
    static final Color COL_BODY_DARK  = new Color(20, 120, 20);
    static final Color COL_APPLE      = new Color(220, 50, 50);
    static final Color COL_APPLE_STEM = new Color(120, 80, 40);
    static final Color COL_HUD        = new Color(160, 255, 160);
    static final Color COL_OVERLAY_BG = new Color(0, 0, 0, 160);

    // --- Game state ---
    private final List<Point> snake = new ArrayList<>();
    private Direction direction;
    private Direction bufferedDir;   // next direction, applied at tick start
    private Point apple;
    private int score;
    private int highScore;
    private GameState state;
    private Timer timer;
    private final Random random = new Random();

    GamePanel() {
        setPreferredSize(new Dimension(SCREEN_W, SCREEN_H));
        setBackground(COL_BG);
        setFocusable(true);
        addKeyListener(new MyKeyAdapter());
        initGame();
    }

    // -------------------------------------------------------------------------
    // Game lifecycle
    // -------------------------------------------------------------------------

    /** Full reset — preserves high score. */
    private void initGame() {
        snake.clear();
        direction    = Direction.RIGHT;
        bufferedDir  = Direction.RIGHT;
        score        = 0;
        state        = GameState.WAITING;

        // Build initial snake (5 segments) near centre
        int startX = COLS / 2;
        int startY = ROWS / 2;
        for (int i = 0; i < 5; i++) {
            snake.add(new Point(startX - i, startY));
        }

        spawnApple();

        if (timer != null) timer.stop();
        timer = new Timer(BASE_DELAY, this);
        repaint();
    }

    /** Begin ticking (called on first key press or after restart). */
    private void startRunning() {
        state = GameState.RUNNING;
        timer.start();
    }

    private void togglePause() {
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;
            timer.stop();
        } else if (state == GameState.PAUSED) {
            state = GameState.RUNNING;
            timer.start();
        }
        repaint();
    }

    private void endGame() {
        state = GameState.GAME_OVER;
        timer.stop();
        if (score > highScore) highScore = score;
        repaint();
    }

    // -------------------------------------------------------------------------
    // Core game loop (called by Timer)
    // -------------------------------------------------------------------------

    @Override
    public void actionPerformed(ActionEvent e) {
        if (state != GameState.RUNNING) return;

        // Apply buffered direction (prevents reversing due to rapid key presses)
        direction = bufferedDir;

        move();
        checkApple();
        checkCollisions();
        repaint();
    }

    private void move() {
        Point head = snake.get(0);
        Point newHead = switch (direction) {
            case UP    -> new Point(head.x,     head.y - 1);
            case DOWN  -> new Point(head.x,     head.y + 1);
            case LEFT  -> new Point(head.x - 1, head.y);
            case RIGHT -> new Point(head.x + 1, head.y);
        };
        snake.add(0, newHead);
        snake.remove(snake.size() - 1); // remove tail (re-added in checkApple if eaten)
    }

    private void checkApple() {
        if (snake.get(0).equals(apple)) {
            score++;
            // Grow snake by re-inserting a tail copy
            snake.add(new Point(snake.get(snake.size() - 1).x, snake.get(snake.size() - 1).y));
            spawnApple();
            updateSpeed();
        }
    }

    private void checkCollisions() {
        Point head = snake.get(0);

        // Wall collision
        if (head.x < 0 || head.x >= COLS || head.y < 0 || head.y >= ROWS) {
            endGame();
            return;
        }

        // Self collision — start from index 1 (skip head itself)
        for (int i = 1; i < snake.size(); i++) {
            if (head.equals(snake.get(i))) {
                endGame();
                return;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void spawnApple() {
        Point candidate;
        do {
            candidate = new Point(random.nextInt(COLS), random.nextInt(ROWS));
        } while (snake.contains(candidate));
        apple = candidate;
    }

    private void updateSpeed() {
        int level = score / APPLES_PER_LEVEL;
        int newDelay = Math.max(MIN_DELAY, BASE_DELAY - level * SPEED_STEP);
        timer.setDelay(newDelay);
    }

    private int currentLevel() {
        return score / APPLES_PER_LEVEL + 1;
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawGrid(g2);
        drawApple(g2);
        drawSnake(g2);
        drawHUD(g2);

        if (state == GameState.WAITING)   drawWaitingOverlay(g2);
        if (state == GameState.PAUSED)    drawPausedOverlay(g2);
        if (state == GameState.GAME_OVER) drawGameOverOverlay(g2);
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(COL_GRID);
        g2.setStroke(new BasicStroke(0.5f));
        for (int x = 0; x <= COLS; x++) g2.drawLine(x * UNIT, 0, x * UNIT, SCREEN_H);
        for (int y = 0; y <= ROWS; y++) g2.drawLine(0, y * UNIT, SCREEN_W, y * UNIT);
    }

    private void drawApple(Graphics2D g2) {
        int ax = apple.x * UNIT;
        int ay = apple.y * UNIT;
        int pad = 2;
        g2.setColor(COL_APPLE);
        g2.fillOval(ax + pad, ay + pad, UNIT - pad * 2, UNIT - pad * 2);
        // Stem
        g2.setColor(COL_APPLE_STEM);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(ax + UNIT / 2, ay + pad, ax + UNIT / 2 + 3, ay - 2);
    }

    private void drawSnake(Graphics2D g2) {
        int size = snake.size();
        for (int i = size - 1; i >= 0; i--) {
            Point seg = snake.get(i);
            int px = seg.x * UNIT + 1;
            int py = seg.y * UNIT + 1;
            int dim = UNIT - 2;
            int arc = 6;

            if (i == 0) {
                g2.setColor(COL_HEAD);
            } else {
                // Gradient from bright to dark along the body
                float t = (float) i / size;
                g2.setColor(blend(COL_BODY_LIGHT, COL_BODY_DARK, t));
            }

            g2.fill(new RoundRectangle2D.Float(px, py, dim, dim, arc, arc));

            // Draw eyes on the head
            if (i == 0) drawEyes(g2, seg);
        }
    }

    private void drawEyes(Graphics2D g2, Point head) {
        g2.setColor(COL_BG);
        int cx = head.x * UNIT + UNIT / 2;
        int cy = head.y * UNIT + UNIT / 2;
        int eyeR = 2;
        int offset = 4;

        int ex1, ey1, ex2, ey2;
        switch (direction) {
            case UP    -> { ex1 = cx - offset; ey1 = cy - offset; ex2 = cx + offset; ey2 = cy - offset; }
            case DOWN  -> { ex1 = cx - offset; ey1 = cy + offset; ex2 = cx + offset; ey2 = cy + offset; }
            case LEFT  -> { ex1 = cx - offset; ey1 = cy - offset; ex2 = cx - offset; ey2 = cy + offset; }
            default    -> { ex1 = cx + offset; ey1 = cy - offset; ex2 = cx + offset; ey2 = cy + offset; }
        }
        g2.fillOval(ex1 - eyeR, ey1 - eyeR, eyeR * 2, eyeR * 2);
        g2.fillOval(ex2 - eyeR, ey2 - eyeR, eyeR * 2, eyeR * 2);
    }

    private void drawHUD(Graphics2D g2) {
        g2.setColor(COL_HUD);
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2.drawString("SCORE: " + score, 8, 18);
        g2.drawString("BEST: " + highScore, 8, 36);

        String lvl = "LVL " + currentLevel();
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(lvl, SCREEN_W - fm.stringWidth(lvl) - 8, 18);
    }

    private void drawWaitingOverlay(Graphics2D g2) {
        drawDimOverlay(g2);
        drawCentredText(g2, "SNAKE", new Font("Monospaced", Font.BOLD, 52), COL_HEAD, -40);
        drawCentredText(g2, "Press any arrow key to start", new Font("Monospaced", Font.PLAIN, 16), Color.WHITE, 20);
    }

    private void drawPausedOverlay(Graphics2D g2) {
        drawDimOverlay(g2);
        drawCentredText(g2, "PAUSED", new Font("Monospaced", Font.BOLD, 48), COL_HEAD, 0);
        drawCentredText(g2, "Press P to resume", new Font("Monospaced", Font.PLAIN, 16), Color.WHITE, 40);
    }

    private void drawGameOverOverlay(Graphics2D g2) {
        drawDimOverlay(g2);
        drawCentredText(g2, "GAME OVER", new Font("Monospaced", Font.BOLD, 44), new Color(220, 80, 80), -50);
        drawCentredText(g2, "Score: " + score + "   Best: " + highScore, new Font("Monospaced", Font.BOLD, 18), COL_HUD, 10);
        drawCentredText(g2, "Press R to restart", new Font("Monospaced", Font.PLAIN, 16), Color.WHITE, 45);
    }

    private void drawDimOverlay(Graphics2D g2) {
        g2.setColor(COL_OVERLAY_BG);
        g2.fillRect(0, 0, SCREEN_W, SCREEN_H);
    }

    private void drawCentredText(Graphics2D g2, String text, Font font, Color color, int yOffset) {
        g2.setFont(font);
        g2.setColor(color);
        FontMetrics fm = g2.getFontMetrics();
        int x = (SCREEN_W - fm.stringWidth(text)) / 2;
        int y = SCREEN_H / 2 + yOffset;
        g2.drawString(text, x, y);
    }

    /** Linear interpolation between two colours. t=0 → a, t=1 → b. */
    private Color blend(Color a, Color b, float t) {
        t = Math.max(0, Math.min(1, t));
        return new Color(
            (int) (a.getRed()   + t * (b.getRed()   - a.getRed())),
            (int) (a.getGreen() + t * (b.getGreen() - a.getGreen())),
            (int) (a.getBlue()  + t * (b.getBlue()  - a.getBlue()))
        );
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {

                case KeyEvent.VK_LEFT  -> requestDirection(Direction.LEFT);
                case KeyEvent.VK_RIGHT -> requestDirection(Direction.RIGHT);
                case KeyEvent.VK_UP    -> requestDirection(Direction.UP);
                case KeyEvent.VK_DOWN  -> requestDirection(Direction.DOWN);

                case KeyEvent.VK_P -> {
                    if (state == GameState.RUNNING || state == GameState.PAUSED)
                        togglePause();
                }
                case KeyEvent.VK_R -> {
                    if (state == GameState.GAME_OVER || state == GameState.WAITING)
                        initGame();
                    else {
                        // Allow mid-game restart
                        initGame();
                    }
                }
            }
        }

        private void requestDirection(Direction d) {
            if (state == GameState.WAITING) {
                bufferedDir = d;
                startRunning();
            } else if (state == GameState.RUNNING && !d.isOpposite(direction)) {
                bufferedDir = d;
            }
        }
    }
}
