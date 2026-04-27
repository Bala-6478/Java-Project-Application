import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class FruitNinjaGame extends JPanel implements ActionListener, MouseMotionListener, MouseListener {

    // ── Constants ─────────────────────────────────────────────────────────────
    private static final int WIDTH            = 800;
    private static final int HEIGHT           = 600;
    private static final int FPS              = 60;
    private static final int SPAWN_CHANCE_BASE= 5;   // % chance per frame at start
    private static final int BOMB_CHANCE      = 15;  // % of spawns that are bombs
    private static final int MAX_MISSED       = 3;   // missed fruits before losing a life
    private static final int MAX_LIVES        = 3;
    private static final int BLADE_MAX_LEN    = 25;
    private static final int FRUIT_RADIUS     = 22;
    private static final int SCORE_PER_FRUIT  = 10;
    private static final int COMBO_WINDOW_MS  = 1000; // ms window for combo

    private static final Color SKY_TOP    = new Color(10,  10,  30);
    private static final Color SKY_BOT    = new Color(20,  60, 120);
    private static final Color GROUND_COL = new Color(20,  80,  20);

    private static final String[] FRUIT_EMOJI = {
        "🍎","🍊","🍉","🍋","🍑","🍇","🫐","🥥"
    };
    private static final Color[] FRUIT_COLORS = {
        new Color(220, 50,  50),   // Apple
        new Color(240, 140, 30),   // Orange
        new Color(60,  180, 60),   // Watermelon
        new Color(240, 220, 50),   // Banana
        new Color(240, 160, 120),  // Peach
        new Color(140, 60,  180),  // Grapes
        new Color(60,  100, 220),  // Blueberry
        new Color(240, 230, 210),  // Coconut
    };

    // ── Game State ────────────────────────────────────────────────────────────
    private enum GameState { PLAYING, GAME_OVER }
    private GameState state;

    private final List<Fruit>     fruits     = new ArrayList<>();
    private final List<Particle>  particles  = new ArrayList<>();
    private final List<ScorePopup> popups    = new ArrayList<>();
    private final List<Point>     bladePath  = new ArrayList<>();

    private Timer  timer;
    private Random random = new Random();

    private int  score;
    private int  lives;
    private int  missedFruits;
    private int  combo;
    private long lastSliceTime;
    private int  frameCount;        // used for difficulty scaling
    private String gameOverReason;

    // ── Fonts ──────────────────────────────────────────────────────────────────
    private Font hudFont;
    private Font bigFont;
    private Font comboFont;

    public FruitNinjaGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addMouseMotionListener(this);
        addMouseListener(this);

        hudFont   = new Font("SansSerif", Font.BOLD, 22);
        bigFont   = new Font("SansSerif", Font.BOLD, 52);
        comboFont = new Font("SansSerif", Font.BOLD, 36);

        timer = new Timer(1000 / FPS, this);
        startGame();
    }

    private void startGame() {
        fruits.clear();
        particles.clear();
        popups.clear();
        bladePath.clear();
        score        = 0;
        lives        = MAX_LIVES;
        missedFruits = 0;
        combo        = 0;
        frameCount   = 0;
        lastSliceTime= 0;
        gameOverReason = "";
        state = GameState.PLAYING;
        timer.start();
        repaint();
    }

    // ── Main Loop ─────────────────────────────────────────────────────────────
    @Override
    public void actionPerformed(ActionEvent e) {
        if (state != GameState.PLAYING) return;
        frameCount++;

        spawnFruits();
        updateFruits();
        updateParticles();
        updatePopups();
        fadeBlade();

        repaint();
    }

    private void spawnFruits() {
        // Difficulty: spawn chance grows every 300 frames (5 s)
        int difficulty = frameCount / 300;
        int spawnChance = Math.min(SPAWN_CHANCE_BASE + difficulty * 2, 20);

        if (random.nextInt(100) < spawnChance) {
            boolean isBomb = random.nextInt(100) < BOMB_CHANCE;
            int type = isBomb ? -1 : random.nextInt(FRUIT_COLORS.length);
            // Spawn along the bottom, toss upward
            int x = FRUIT_RADIUS + random.nextInt(WIDTH - FRUIT_RADIUS * 2);
            double vx = random.nextDouble() * 4 - 2;
            double vy = -(random.nextDouble() * 8 + 10); // strong upward toss
            fruits.add(new Fruit(x, HEIGHT + FRUIT_RADIUS, type, isBomb, vx, vy));
        }
    }

    private void updateFruits() {
        Iterator<Fruit> it = fruits.iterator();
        while (it.hasNext()) {
            Fruit f = it.next();
            f.update();

            // Remove fruits that have fallen below the screen after peaking
            if (f.getY() > HEIGHT + FRUIT_RADIUS * 2 && f.getVY() > 0) {
                it.remove();
                if (!f.isSliced() && !f.isBomb()) {
                    missedFruits++;
                    if (missedFruits >= MAX_MISSED) {
                        missedFruits = 0;
                        loseLife("Too many fruits missed!");
                    }
                }
            }
        }
    }

    private void updateParticles() {
        particles.removeIf(p -> { p.update(); return p.isDead(); });
    }

    private void updatePopups() {
        popups.removeIf(p -> { p.update(); return p.isDead(); });
    }

    private void fadeBlade() {
        if (bladePath.size() > BLADE_MAX_LEN) {
            bladePath.subList(0, bladePath.size() - BLADE_MAX_LEN).clear();
        }
    }

    private void loseLife(String reason) {
        lives--;
        if (lives <= 0) {
            triggerGameOver(reason);
        }
    }

    private void triggerGameOver(String reason) {
        state = GameState.GAME_OVER;
        gameOverReason = reason;
        timer.stop();
        repaint();
    }

    // ── Rendering ─────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBackground(g2);
        drawParticles(g2);
        drawFruits(g2);
        drawBlade(g2);
        drawHUD(g2);
        drawPopups(g2);

        if (state == GameState.GAME_OVER) drawGameOver(g2);
    }

    private void drawBackground(Graphics2D g2) {
        g2.setPaint(new GradientPaint(0, 0, SKY_TOP, 0, HEIGHT, SKY_BOT));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        // Subtle star dots
        g2.setColor(new Color(255, 255, 255, 60));
        Random r = new Random(42);
        for (int i = 0; i < 80; i++) {
            int sx = r.nextInt(WIDTH);
            int sy = r.nextInt(HEIGHT - 80);
            g2.fillOval(sx, sy, 2, 2);
        }

        // Ground
        g2.setColor(GROUND_COL);
        g2.fillRect(0, HEIGHT - 40, WIDTH, 40);
        g2.setColor(new Color(30, 120, 30));
        g2.fillRect(0, HEIGHT - 40, WIDTH, 6);
    }

    private void drawFruits(Graphics2D g2) {
        for (Fruit f : fruits) {
            int fx = (int) f.getX();
            int fy = (int) f.getY();

            if (f.isBomb()) {
                drawBomb(g2, fx, fy, f.isSliced());
            } else if (f.isSliced()) {
                drawSlicedFruit(g2, f, fx, fy);
            } else {
                drawWholeFruit(g2, f, fx, fy);
            }
        }
    }

    private void drawWholeFruit(Graphics2D g2, Fruit f, int cx, int cy) {
        int r = FRUIT_RADIUS;
        Color col = FRUIT_COLORS[f.getType()];

        // Shadow
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillOval(cx - r + 4, cy - r + 4, r * 2, r * 2);

        // Body
        g2.setPaint(new RadialGradientPaint(
            new Point2D.Float(cx - r / 3f, cy - r / 3f),
            r * 1.2f,
            new float[]{0f, 1f},
            new Color[]{col.brighter(), col.darker()}
        ));
        g2.fillOval(cx - r, cy - r, r * 2, r * 2);

        // Highlight
        g2.setColor(new Color(255, 255, 255, 80));
        g2.fillOval(cx - r / 2, cy - r + 4, r / 2, r / 3);

        // Emoji label
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        String emoji = FRUIT_EMOJI[f.getType()];
        g2.drawString(emoji, cx - fm.stringWidth(emoji) / 2, cy + fm.getAscent() / 2 - 2);
    }

    private void drawSlicedFruit(Graphics2D g2, Fruit f, int cx, int cy) {
        Color col = FRUIT_COLORS[f.getType()];
        int r = FRUIT_RADIUS;
        float angle = f.getRotation();

        // Left half
        AffineTransform old = g2.getTransform();
        g2.translate(cx - r / 2, cy);
        g2.rotate(angle - 0.3);
        g2.setColor(col.darker());
        g2.fillArc(-r / 2, -r, r, r * 2, 90, 180);
        g2.setColor(new Color(255, 220, 180, 180));
        g2.fillArc(-r / 2 + 2, -r + 2, r - 4, r * 2 - 4, 90, 180);
        g2.setTransform(old);

        // Right half
        g2.translate(cx + r / 2, cy);
        g2.rotate(angle + 0.3);
        g2.setColor(col.darker());
        g2.fillArc(-r / 2, -r, r, r * 2, 270, 180);
        g2.setColor(new Color(255, 220, 180, 180));
        g2.fillArc(-r / 2 + 2, -r + 2, r - 4, r * 2 - 4, 270, 180);
        g2.setTransform(old);
    }

    private void drawBomb(Graphics2D g2, int cx, int cy, boolean exploded) {
        if (exploded) {
            // Explosion ring
            for (int ring = 0; ring < 3; ring++) {
                int alpha = 200 - ring * 60;
                g2.setColor(new Color(255, 140 - ring * 40, 0, alpha));
                int size = 30 + ring * 20;
                g2.fillOval(cx - size / 2, cy - size / 2, size, size);
            }
        } else {
            // Bomb body
            g2.setColor(new Color(40, 40, 40));
            g2.fillOval(cx - FRUIT_RADIUS, cy - FRUIT_RADIUS, FRUIT_RADIUS * 2, FRUIT_RADIUS * 2);
            g2.setColor(new Color(80, 80, 80));
            g2.fillOval(cx - FRUIT_RADIUS + 4, cy - FRUIT_RADIUS + 4, 10, 10);
            // Fuse
            g2.setColor(new Color(180, 120, 40));
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(cx, cy - FRUIT_RADIUS, cx + 8, cy - FRUIT_RADIUS - 10);
            // Spark
            g2.setColor(Color.YELLOW);
            g2.fillOval(cx + 6, cy - FRUIT_RADIUS - 12, 5, 5);
            g2.setStroke(new BasicStroke(1));
        }
    }

    private void drawBlade(Graphics2D g2) {
        if (bladePath.size() < 2) return;
        int n = bladePath.size();
        for (int i = 1; i < n; i++) {
            float progress = (float) i / n;
            int alpha = (int) (200 * progress);
            float width = 1 + 4 * progress;
            g2.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(255, 255, 255, alpha));
            Point p1 = bladePath.get(i - 1);
            Point p2 = bladePath.get(i);
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
        g2.setStroke(new BasicStroke(1));
    }

    private void drawParticles(Graphics2D g2) {
        for (Particle p : particles) {
            int alpha = (int) (255 * p.getLife());
            g2.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), alpha));
            int size = Math.max(1, (int) (p.size * p.getLife()));
            g2.fillOval((int) p.x - size / 2, (int) p.y - size / 2, size, size);
        }
    }

    private void drawHUD(Graphics2D g2) {
        // Score
        g2.setFont(hudFont);
        g2.setColor(new Color(0, 0, 0, 100));
        g2.drawString("SCORE", 22, 34);
        g2.setColor(Color.WHITE);
        g2.drawString("SCORE", 20, 32);
        g2.setFont(new Font("SansSerif", Font.BOLD, 32));
        g2.setColor(new Color(255, 220, 50));
        g2.drawString(String.valueOf(score), 20, 68);

        // Lives as hearts
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        StringBuilder hearts = new StringBuilder();
        for (int i = 0; i < MAX_LIVES; i++) hearts.append(i < lives ? "❤️" : "🖤");
        g2.setColor(Color.WHITE);
        g2.drawString(hearts.toString(), WIDTH - 120, 36);

        // Missed fruits bar
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2.setColor(new Color(255, 255, 255, 150));
        g2.drawString("Missed: " + missedFruits + "/" + MAX_MISSED, WIDTH - 120, 58);

        // Combo
        if (combo >= 2) {
            long now = System.currentTimeMillis();
            long elapsed = now - lastSliceTime;
            if (elapsed < COMBO_WINDOW_MS) {
                float fade = 1f - (float) elapsed / COMBO_WINDOW_MS;
                int alpha = (int) (255 * fade);
                g2.setFont(comboFont);
                String comboStr = combo + "x COMBO!";
                FontMetrics fm = g2.getFontMetrics();
                int cx = (WIDTH - fm.stringWidth(comboStr)) / 2;
                g2.setColor(new Color(255, 200, 0, alpha));
                g2.drawString(comboStr, cx, 80);
            }
        }

        // Difficulty label
        int diff = frameCount / 300 + 1;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2.setColor(new Color(255, 255, 255, 100));
        g2.drawString("Level " + diff, 20, HEIGHT - 50);
    }

    private void drawPopups(Graphics2D g2) {
        for (ScorePopup p : popups) {
            int alpha = (int) (255 * p.getLife());
            g2.setColor(new Color(255, 230, 50, alpha));
            g2.setFont(new Font("SansSerif", Font.BOLD, 18));
            g2.drawString(p.text, (int) p.x, (int) p.y);
        }
    }

    private void drawGameOver(Graphics2D g2) {
        // Dark overlay
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        // Panel
        int pw = 480, ph = 260;
        int px = (WIDTH - pw) / 2, py = (HEIGHT - ph) / 2;
        g2.setColor(new Color(20, 20, 40, 230));
        g2.fillRoundRect(px, py, pw, ph, 30, 30);
        g2.setColor(new Color(200, 50, 50));
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(px, py, pw, ph, 30, 30);
        g2.setStroke(new BasicStroke(1));

        // Title
        g2.setFont(bigFont);
        g2.setColor(new Color(220, 50, 50));
        centerText(g2, "GAME OVER", py + 70);

        // Reason
        g2.setFont(hudFont);
        g2.setColor(new Color(200, 200, 200));
        centerText(g2, gameOverReason, py + 110);

        // Score
        g2.setFont(new Font("SansSerif", Font.BOLD, 28));
        g2.setColor(new Color(255, 220, 50));
        centerText(g2, "Score: " + score, py + 155);

        // Restart prompt
        g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g2.setColor(new Color(180, 180, 180));
        centerText(g2, "Click anywhere to play again", py + 210);
    }

    private void centerText(Graphics2D g2, String text, int y) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, (WIDTH - fm.stringWidth(text)) / 2, y);
    }

    // ── Input Handling ─────────────────────────────────────────────────────────
    @Override
    public void mouseDragged(MouseEvent e) {
        if (state != GameState.PLAYING) return;
        bladePath.add(e.getPoint());
        checkSlices();
    }

    private void checkSlices() {
        int n = bladePath.size();
        if (n < 2) return;
        Point p1 = bladePath.get(n - 2);
        Point p2 = bladePath.get(n - 1);

        int slicedThisSwipe = 0;
        for (Fruit f : fruits) {
            if (f.isSliced()) continue;
            int cx = (int) f.getX(), cy = (int) f.getY();
            if (lineIntersectsCircle(p1.x, p1.y, p2.x, p2.y, cx, cy, FRUIT_RADIUS)) {
                f.slice();
                if (f.isBomb()) {
                    spawnExplosionParticles(cx, cy);
                    triggerGameOver("You hit a bomb!");
                    return;
                }
                slicedThisSwipe++;
                spawnSliceParticles(cx, cy, FRUIT_COLORS[f.getType()]);
                awardScore(cx, cy);
            }
        }
    }

    private void awardScore(int x, int y) {
        long now = System.currentTimeMillis();
        if (now - lastSliceTime < COMBO_WINDOW_MS) {
            combo++;
        } else {
            combo = 1;
        }
        lastSliceTime = now;

        int points = SCORE_PER_FRUIT * combo;
        score += points;
        missedFruits = Math.max(0, missedFruits - 1);

        String label = combo > 1 ? "+" + points + " x" + combo : "+" + points;
        popups.add(new ScorePopup(label, x, y - FRUIT_RADIUS));
    }

    private void spawnSliceParticles(int cx, int cy, Color col) {
        for (int i = 0; i < 14; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 2 + random.nextDouble() * 4;
            particles.add(new Particle(cx, cy,
                Math.cos(angle) * speed, Math.sin(angle) * speed,
                col, 4 + random.nextInt(6)));
        }
    }

    private void spawnExplosionParticles(int cx, int cy) {
        for (int i = 0; i < 30; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 3 + random.nextDouble() * 8;
            Color col = random.nextBoolean() ? Color.ORANGE : Color.RED;
            particles.add(new Particle(cx, cy,
                Math.cos(angle) * speed, Math.sin(angle) * speed,
                col, 6 + random.nextInt(10)));
        }
    }

    @Override public void mouseMoved(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        bladePath.clear();
        bladePath.add(e.getPoint());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        bladePath.clear();
        combo = 0;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (state == GameState.GAME_OVER) startGame();
    }

    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // ── Math ──────────────────────────────────────────────────────────────────
    private boolean lineIntersectsCircle(int x1, int y1, int x2, int y2, int cx, int cy, int r) {
        double dx = x2 - x1, dy = y2 - y1;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len == 0) return Math.hypot(cx - x1, cy - y1) <= r;
        double t = Math.max(0, Math.min(1, ((cx - x1) * dx + (cy - y1) * dy) / (len * len)));
        double nearX = x1 + t * dx, nearY = y1 + t * dy;
        return Math.hypot(cx - nearX, cy - nearY) <= r;
    }

    // ── Inner Classes ──────────────────────────────────────────────────────────
    private class Fruit {
        private double x, y, vx, vy;
        private boolean sliced, bomb;
        private int type;
        private float rotation;

        Fruit(int x, int y, int type, boolean isBomb, double vx, double vy) {
            this.x = x; this.y = y; this.type = type; this.bomb = isBomb;
            this.vx = vx; this.vy = vy;
        }

        void update() {
            x += vx; y += vy;
            vy += 0.25;
            rotation += 0.04f;
            if (sliced && !bomb) { vx *= 1.01; vy += 0.3; }
        }

        void slice() {
            sliced = true;
            if (!bomb) { vx *= 1.5; vy *= 1.5; }
        }

        double getX()       { return x; }
        double getY()       { return y; }
        double getVY()      { return vy; }
        boolean isSliced()  { return sliced; }
        boolean isBomb()    { return bomb; }
        int getType()       { return type; }
        float getRotation() { return rotation; }
    }

    private static class Particle {
        double x, y, vx, vy;
        Color  color;
        int    size;
        float  life = 1f;

        Particle(double x, double y, double vx, double vy, Color color, int size) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.color = color; this.size = size;
        }

        void update() {
            x += vx; y += vy;
            vy += 0.3;
            life -= 0.03f;
        }

        float   getLife() { return Math.max(0, life); }
        boolean isDead()  { return life <= 0; }
    }

    private static class ScorePopup {
        String text;
        double x, y;
        float  life = 1f;

        ScorePopup(String text, double x, double y) {
            this.text = text; this.x = x; this.y = y;
        }

        void update() { y -= 1.5; life -= 0.025f; }
        float   getLife() { return Math.max(0, life); }
        boolean isDead()  { return life <= 0; }
    }

    // ── Entry Point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("🍉 Fruit Ninja");
            FruitNinjaGame game = new FruitNinjaGame();
            frame.add(game);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }
}
