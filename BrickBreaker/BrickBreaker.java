import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

// ── Particle effect ──────────────────────────────────────────────────────────
class Particle {
    float x, y, vx, vy, life, maxLife;
    Color color;

    Particle(float x, float y, Color color) {
        this.x = x; this.y = y; this.color = color;
        Random r = new Random();
        float angle = (float)(Math.random() * Math.PI * 2);
        float speed = 1.5f + r.nextFloat() * 3f;
        this.vx = (float)Math.cos(angle) * speed;
        this.vy = (float)Math.sin(angle) * speed;
        this.life = 1f;
        this.maxLife = 1f;
    }

    void update() {
        x += vx; y += vy;
        vy += 0.1f;
        life -= 0.035f;
    }

    void draw(Graphics2D g) {
        float alpha = Math.max(0, life / maxLife);
        int radius = (int)(4 * alpha);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setColor(color);
        g.fillOval((int)x - radius, (int)y - radius, radius * 2, radius * 2);
        g.setComposite(AlphaComposite.SrcOver);
    }

    boolean isDead() { return life <= 0; }
}

// ── Brick ────────────────────────────────────────────────────────────────────
class Brick {
    int x, y, w, h, hp, maxHp;
    Color[] colors = {
        new Color(226, 75, 74),   // red
        new Color(239, 159, 39),  // amber
        new Color(29, 158, 117),  // teal
        new Color(55, 138, 221),  // blue
        new Color(127, 119, 221), // purple
    };
    Color fill, shine;
    int row;

    Brick(int x, int y, int w, int h, int hp, int row) {
        this.x = x; this.y = y; this.w = w; this.h = h;
        this.hp = hp; this.maxHp = hp; this.row = row;
        this.fill = colors[row % colors.length];
        this.shine = fill.brighter().brighter();
    }

    Rectangle rect() { return new Rectangle(x, y, w, h); }
    boolean alive() { return hp > 0; }

    void draw(Graphics2D g) {
        float frac = (float) hp / maxHp;
        Color c = frac < 0.5f ? fill.darker().darker() : fill;

        // body
        g.setColor(c);
        g.fillRoundRect(x, y, w, h, 6, 6);

        // shine strip
        g.setColor(new Color(255, 255, 255, 60));
        g.fillRect(x + 3, y + 3, w - 6, 4);

        // border
        g.setColor(new Color(0, 0, 0, 80));
        g.drawRoundRect(x, y, w, h, 6, 6);

        // HP number for multi-hit bricks
        if (maxHp > 1) {
            g.setColor(new Color(255, 255, 255, 200));
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            FontMetrics fm = g.getFontMetrics();
            String s = String.valueOf(hp);
            g.drawString(s, x + (w - fm.stringWidth(s)) / 2, y + (h + fm.getAscent()) / 2 - 2);
        }
    }

    Color getColor() { return fill; }
}

// ── Main game panel ──────────────────────────────────────────────────────────
public class BrickBreaker extends JPanel implements ActionListener, KeyListener, MouseMotionListener, MouseListener {

    // Layout
    static final int W = 700, H = 560;
    static final int COLS = 9, ROWS = 4;
    static final int BRICK_OFF_X = 36, BRICK_OFF_Y = 55;
    static final int BRICK_GAP = 4;
    static final int PAD_W = 100, PAD_H = 10, PAD_Y = H - 52;
    static final int BALL_R = 8;
    static final int MAX_LIVES = 3;
    static final int MAX_LEVELS = 6;

    // Colors
    static final Color BG_TOP    = new Color(12, 12, 24);
    static final Color BG_BOTTOM = new Color(20, 10, 38);
    static final Color BALL_COL  = new Color(226, 75, 74);
    static final Color PAD_COL   = new Color(133, 183, 235);
    static final Color TEXT_COL  = new Color(220, 220, 240);
    static final Color DIM_COL   = new Color(140, 130, 170);

    // State
    enum Phase { WAITING, PLAYING, DEAD, GAMEOVER, WIN, PAUSED }
    Phase phase = Phase.WAITING;

    int score, lives, level, combo, best;
    double padX, ballX, ballY, vx, vy, speed;
    boolean launched;
    Brick[][] bricks;
    List<Particle> particles = new ArrayList<>();
    List<Point2D.Double> trail = new ArrayList<>();

    // Off-screen buffer
    BufferedImage buffer;
    Graphics2D bg;

    Timer timer;
    boolean[] keyDown = new boolean[256];

    // ── Setup ────────────────────────────────────────────────────────────────
    public BrickBreaker() {
        setPreferredSize(new Dimension(W, H));
        setFocusable(true);
        addKeyListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);

        buffer = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        bg = buffer.createGraphics();
        bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        bg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        newGame();
        timer = new Timer(15, this);
        timer.start();
    }

    int brickW() { return (W - BRICK_OFF_X * 2 + BRICK_GAP) / COLS - BRICK_GAP; }
    int brickH() { return 22; }

    int brickX(int c) { return BRICK_OFF_X + c * (brickW() + BRICK_GAP); }
    int brickY(int r) { return BRICK_OFF_Y + r * (brickH() + BRICK_GAP); }

    void newGame() {
        score = 0; lives = MAX_LIVES; level = 1; combo = 0;
        initLevel();
        phase = Phase.WAITING;
    }

    void initLevel() {
        padX = W / 2.0 - PAD_W / 2.0;
        ballX = W / 2.0;
        ballY = PAD_Y - BALL_R - 2;
        speed = 3.5 + (level - 1) * 0.45;
        double angle = -Math.PI / 2 + (Math.random() - 0.5) * 0.7;
        vx = Math.cos(angle) * speed;
        vy = Math.sin(angle) * speed;
        launched = false;
        combo = 0;
        particles.clear();
        trail.clear();
        makeBricks();
    }

    void makeBricks() {
        int[] hpTable = {1, 1, 2, 2, 3, 3};
        int base = hpTable[Math.min(level - 1, hpTable.length - 1)];
        bricks = new Brick[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int hp = (r == 0 && level >= 3) ? base + 1 : base;
                bricks[r][c] = new Brick(brickX(c), brickY(r), brickW(), brickH(), hp, r);
            }
        }
    }

    // ── Game tick ────────────────────────────────────────────────────────────
    @Override
    public void actionPerformed(ActionEvent e) {
        if (phase == Phase.PAUSED || phase == Phase.GAMEOVER || phase == Phase.WIN) {
            repaint(); return;
        }

        // Keyboard paddle movement
        if (keyDown[KeyEvent.VK_LEFT])  padX = Math.max(0, padX - 22);
        if (keyDown[KeyEvent.VK_RIGHT]) padX = Math.min(W - PAD_W, padX + 22);

        if (!launched) {
            ballX = padX + PAD_W / 2.0;
            ballY = PAD_Y - BALL_R - 2;
        }

        if (phase == Phase.PLAYING && launched) {
            // Trail
            trail.add(new Point2D.Double(ballX, ballY));
            if (trail.size() > 14) trail.remove(0);

            ballX += vx; ballY += vy;

            // Walls
            if (ballX - BALL_R < 0) { ballX = BALL_R; vx = Math.abs(vx); }
            if (ballX + BALL_R > W) { ballX = W - BALL_R; vx = -Math.abs(vx); }
            if (ballY - BALL_R < 0) { ballY = BALL_R; vy = Math.abs(vy); }

            // Paddle collision
            if (vy > 0 && ballY + BALL_R >= PAD_Y && ballY - BALL_R < PAD_Y + PAD_H
                    && ballX + BALL_R > padX && ballX - BALL_R < padX + PAD_W) {
                double rel = (ballX - padX) / PAD_W;
                double angle = -Math.PI / 2 + (rel - 0.5) * 1.6;
                vx = Math.cos(angle) * speed;
                vy = -Math.abs(Math.sin(angle) * speed);
                combo = 0;
            }

            // Brick collisions
            outer:
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    Brick b = bricks[r][c];
                    if (!b.alive()) continue;
                    Rectangle br = b.rect();
                    if (ballIntersects(br)) {
                        resolveCollision(br);
                        b.hp--;
                        if (b.hp == 0) {
                            combo++;
                            int pts = 10 * level * Math.max(1, combo);
                            score += pts;
                            if (score > best) best = score;
                            spawnParticles(b.x + b.w / 2, b.y + b.h / 2, b.getColor());
                        }
                        break outer;
                    }
                }
            }

            // Particle update
            Iterator<Particle> it = particles.iterator();
            while (it.hasNext()) { Particle p = it.next(); p.update(); if (p.isDead()) it.remove(); }

            // Lost ball
            if (ballY - BALL_R > H) {
                lives--;
                combo = 0;
                if (lives <= 0) { phase = Phase.GAMEOVER; }
                else { phase = Phase.DEAD; initLevel(); }
            }

            // Won level
            boolean cleared = true;
            for (Brick[] row : bricks) for (Brick b : row) if (b.alive()) { cleared = false; break; }
            if (cleared) {
                level++;
                if (level > MAX_LEVELS) { phase = Phase.WIN; }
                else { initLevel(); phase = Phase.WAITING; }
            }
        }

        repaint();
    }

    boolean ballIntersects(Rectangle r) {
        return ballX + BALL_R > r.x && ballX - BALL_R < r.x + r.width
            && ballY + BALL_R > r.y && ballY - BALL_R < r.y + r.height;
    }

    void resolveCollision(Rectangle r) {
        double overlapL = (ballX + BALL_R) - r.x;
        double overlapR = (r.x + r.width) - (ballX - BALL_R);
        double overlapT = (ballY + BALL_R) - r.y;
        double overlapB = (r.y + r.height) - (ballY - BALL_R);
        double minH = Math.min(overlapL, overlapR);
        double minV = Math.min(overlapT, overlapB);
        if (minH < minV) vx = -vx; else vy = -vy;
    }

    void spawnParticles(int x, int y, Color col) {
        for (int i = 0; i < 12; i++) particles.add(new Particle(x, y, col));
    }

    // ── Rendering ────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawFrame(bg);
        g.drawImage(buffer, 0, 0, null);
    }

    void drawFrame(Graphics2D g) {
        // Background gradient
        GradientPaint grad = new GradientPaint(0, 0, BG_TOP, 0, H, BG_BOTTOM);
        g.setPaint(grad);
        g.fillRect(0, 0, W, H);

        // Subtle grid lines
        g.setColor(new Color(255, 255, 255, 6));
        for (int x = 0; x < W; x += 40) g.drawLine(x, 0, x, H);
        for (int y = 0; y < H; y += 40) g.drawLine(0, y, W, y);

        // HUD
        drawHUD(g);

        // Bricks
        for (Brick[] row : bricks) for (Brick b : row) if (b.alive()) b.draw(g);

        // Launch guide
        if (!launched && (phase == Phase.WAITING || phase == Phase.DEAD)) drawGuide(g);

        // Trail
        for (int i = 0; i < trail.size(); i++) {
            float alpha = (float) i / trail.size() * 0.25f;
            float radius = BALL_R * (float) i / trail.size() * 0.9f;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.setColor(BALL_COL);
            Point2D.Double p = trail.get(i);
            g.fillOval((int)(p.x - radius), (int)(p.y - radius), (int)(radius*2), (int)(radius*2));
        }
        g.setComposite(AlphaComposite.SrcOver);

        // Ball
        drawBall(g);

        // Paddle
        drawPaddle(g);

        // Particles
        for (Particle p : particles) p.draw(g);

        // Overlays
        if (phase == Phase.WAITING || phase == Phase.DEAD) {
            String msg = phase == Phase.DEAD ? "Life lost!  Press Space to relaunch" : "Press Space or click to launch";
            drawHint(g, msg);
        }
        if (phase == Phase.PAUSED)  drawOverlay(g, "Paused", "Press P to continue", null);
        if (phase == Phase.GAMEOVER) drawOverlay(g, "Game Over", "Score: " + score, "Press Enter to play again");
        if (phase == Phase.WIN)      drawOverlay(g, "You Win!", "Final score: " + score, "Press Enter to play again");
    }

    void drawHUD(Graphics2D g) {
        // Score
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setColor(DIM_COL);
        g.drawString("SCORE", 16, 22);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.setColor(TEXT_COL);
        g.drawString(String.valueOf(score), 16, 42);

        // Best
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setColor(DIM_COL);
        g.drawString("BEST", 110, 22);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.setColor(TEXT_COL);
        g.drawString(String.valueOf(best), 110, 42);

        // Level
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setColor(DIM_COL);
        g.drawString("LEVEL", W / 2 - 22, 22);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.setColor(TEXT_COL);
        g.drawString(level + " / " + MAX_LEVELS, W / 2 - 22, 42);

        // Combo
        if (combo > 1) {
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            g.setColor(new Color(239, 159, 39));
            g.drawString("COMBO x" + combo, W - 110, 22);
        }

        // Lives (pip circles)
        for (int i = 0; i < MAX_LIVES; i++) {
            if (i < lives) g.setColor(BALL_COL);
            else           g.setColor(new Color(80, 60, 80));
            g.fillOval(W - 26 - i * 20, 28, 13, 13);
        }

        // Separator line
        g.setColor(new Color(255, 255, 255, 25));
        g.drawLine(0, 50, W, 50);
    }

    void drawBall(Graphics2D g) {
        // Glow ring
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
        g.setColor(BALL_COL);
        g.fillOval((int)ballX - BALL_R - 4, (int)ballY - BALL_R - 4, (BALL_R + 4) * 2, (BALL_R + 4) * 2);
        g.setComposite(AlphaComposite.SrcOver);

        // Ball body
        g.setColor(BALL_COL);
        g.fillOval((int)ballX - BALL_R, (int)ballY - BALL_R, BALL_R * 2, BALL_R * 2);

        // Specular
        g.setColor(new Color(255, 255, 255, 140));
        g.fillOval((int)ballX - 3, (int)ballY - 3, 4, 4);
    }

    void drawPaddle(Graphics2D g) {
        // Shadow
        g.setColor(new Color(0, 0, 0, 60));
        g.fillRoundRect((int)padX + 4, PAD_Y + 4, PAD_W, PAD_H, 6, 6);

        // Body
        g.setColor(PAD_COL);
        g.fillRoundRect((int)padX, PAD_Y, PAD_W, PAD_H, 6, 6);

        // Shine
        g.setColor(new Color(255, 255, 255, 80));
        g.fillRect((int)padX + 6, PAD_Y + 2, PAD_W - 12, 3);
    }

    void drawGuide(Graphics2D g) {
        float[] dash = {4f, 8f};
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dash, 0));
        g.setColor(new Color(255, 255, 255, 45));
        double tx = ballX, ty = ballY, tvx = vx, tvy = vy;
        g.drawLine((int)tx, (int)ty, (int)tx, (int)ty);
        GeneralPath path = new GeneralPath();
        path.moveTo(tx, ty);
        for (int i = 0; i < 80; i++) {
            tx += tvx; ty += tvy;
            if (tx < BALL_R || tx > W - BALL_R) tvx = -tvx;
            if (ty < BALL_R) tvy = -tvy;
            path.lineTo(tx, ty);
            if (ty > PAD_Y) break;
        }
        g.draw(path);
        g.setStroke(new BasicStroke(1));
    }

    void drawHint(Graphics2D g, String msg) {
        g.setColor(new Color(255, 255, 255, 90));
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (W - fm.stringWidth(msg)) / 2, H - 18);
    }

    void drawOverlay(Graphics2D g, String title, String sub, String hint) {
        g.setColor(new Color(10, 8, 20, 180));
        g.fillRect(0, 0, W, H);

        g.setFont(new Font("SansSerif", Font.BOLD, 36));
        g.setColor(TEXT_COL);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (W - fm.stringWidth(title)) / 2, H / 2 - 30);

        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g.setColor(DIM_COL);
        fm = g.getFontMetrics();
        g.drawString(sub, (W - fm.stringWidth(sub)) / 2, H / 2 + 10);

        if (hint != null) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g.setColor(new Color(140, 130, 170, 180));
            fm = g.getFontMetrics();
            g.drawString(hint, (W - fm.stringWidth(hint)) / 2, H / 2 + 44);
        }
    }

    // ── Input ────────────────────────────────────────────────────────────────
    void launch() {
        if (phase == Phase.WAITING || phase == Phase.DEAD) {
            launched = true;
            phase = Phase.PLAYING;
        }
    }

    @Override public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k < keyDown.length) keyDown[k] = true;

        if (k == KeyEvent.VK_SPACE) launch();

        if (k == KeyEvent.VK_P) {
            if (phase == Phase.PLAYING) phase = Phase.PAUSED;
            else if (phase == Phase.PAUSED) phase = Phase.PLAYING;
        }

        if (k == KeyEvent.VK_ENTER) {
            if (phase == Phase.GAMEOVER || phase == Phase.WIN) newGame();
        }
    }

    @Override public void keyReleased(KeyEvent e) { if (e.getKeyCode() < keyDown.length) keyDown[e.getKeyCode()] = false; }
    @Override public void keyTyped(KeyEvent e) {}

    @Override public void mouseMoved(MouseEvent e) {
        padX = Math.min(Math.max(e.getX() - PAD_W / 2.0, 0), W - PAD_W);
    }
    @Override public void mouseDragged(MouseEvent e) { mouseMoved(e); }

    @Override public void mouseClicked(MouseEvent e) { launch(); }
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // ── Entry point ──────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Brick Breaker");
            BrickBreaker game = new BrickBreaker();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.add(game);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
