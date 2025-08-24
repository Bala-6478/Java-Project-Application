import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

/**
 * Main class for the Snake Game, sets up the main window (JFrame).
 */
public class SnakeGame extends JFrame {

    public SnakeGame() {
        // Add the main game panel to the frame
        add(new GamePanel());
        // Set the title of the window
        setTitle("Snake Game");
        // Ensure the application exits when the window is closed
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Make the window non-resizable
        setResizable(false);
        // Pack the components of the window snugly
        pack();
        // Center the window on the screen
        setLocationRelativeTo(null);
        // Make the window visible
        setVisible(true);
    }

    public static void main(String[] args) {
        // Create a new instance of the game on the Event Dispatch Thread
        new SnakeGame();
    }
}

/**
 * GamePanel class contains all the core game logic and graphics.
 */
class GamePanel extends JPanel implements ActionListener {

    // --- Game Constants ---
    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25; // Size of each grid unit (and snake body part)
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    static final int DELAY = 75; // The delay for the game timer (controls game speed)

    // --- Game State Variables ---
    // Arrays to hold the x and y coordinates of the snake's body parts
    final int x[] = new int[GAME_UNITS];
    final int y[] = new int[GAME_UNITS];
    int bodyParts = 6; // Initial length of the snake
    int applesEaten;
    int appleX; // x-coordinate of the apple
    int appleY; // y-coordinate of the apple
    char direction = 'R'; // Initial direction: 'R' for Right, 'L' for Left, 'U' for Up, 'D' for Down
    boolean running = false;
    Timer timer;
    Random random;

    /**
     * Constructor for the GamePanel.
     */
    GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        startGame();
    }

    /**
     * Initializes or resets the game state.
     */
    public void startGame() {
        // Set initial snake length, score, and direction
        bodyParts = 6;
        applesEaten = 0;
        direction = 'R';
        // Position the initial snake body parts in a line near the center
        for (int i = 0; i < bodyParts; i++) {
            x[i] = SCREEN_WIDTH / 2;
            y[i] = SCREEN_HEIGHT / 2;
        }
        
        newApple();
        running = true;
        timer = new Timer(DELAY, this);
        timer.start();
    }

    /**
     * Overrides the paintComponent method to draw the game graphics.
     * @param g The Graphics object to protect
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    /**
     * Draws all the game elements.
     * @param g The Graphics object for drawing
     */
    public void draw(Graphics g) {
        if (running) {
            // Draw the apple
            g.setColor(Color.red);
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

            // Draw the snake
            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) { // The head of the snake
                    g.setColor(Color.green);
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                } else { // The body of the snake
                    g.setColor(new Color(45, 180, 0));
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                }
            }
            // Draw the score
            g.setColor(Color.white);
            g.setFont(new Font("Ink Free", Font.BOLD, 40));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics.stringWidth("Score: " + applesEaten)) / 2, g.getFont().getSize());

        } else {
            gameOver(g);
        }
    }

    /**
     * Generates coordinates for a new apple, ensuring it doesn't spawn on the snake.
     */
    public void newApple() {
        boolean onSnake;
        do {
            onSnake = false;
            appleX = random.nextInt((int)(SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
            appleY = random.nextInt((int)(SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
            // Check if the new apple position is on the snake
            for (int i = 0; i < bodyParts; i++) {
                if (appleX == x[i] && appleY == y[i]) {
                    onSnake = true;
                    break;
                }
            }
        } while (onSnake);
    }

    /**
     * Moves the snake by updating its coordinates.
     */
    public void move() {
        // Shift the body parts of the snake
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        // Move the head of the snake based on the current direction
        switch (direction) {
            case 'U':
                y[0] = y[0] - UNIT_SIZE;
                break;
            case 'D':
                y[0] = y[0] + UNIT_SIZE;
                break;
            case 'L':
                x[0] = x[0] - UNIT_SIZE;
                break;
            case 'R':
                x[0] = x[0] + UNIT_SIZE;
                break;
        }
    }

    /**
     * Checks if the snake has eaten the apple.
     */
    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
        }
    }

    /**
     * Checks for collisions with the walls or the snake's own body.
     */
    public void checkCollisions() {
        // Check if head collides with body
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
            }
        }
        // Check if head touches left border
        if (x[0] < 0) {
            running = false;
        }
        // Check if head touches right border
        if (x[0] >= SCREEN_WIDTH) {
            running = false;
        }
        // Check if head touches top border
        if (y[0] < 0) {
            running = false;
        }
        // Check if head touches bottom border
        if (y[0] >= SCREEN_HEIGHT) {
            running = false;
        }

        if (!running) {
            timer.stop();
        }
    }

    /**
     * Displays the "Game Over" screen.
     * @param g The Graphics object for drawing
     */
    public void gameOver(Graphics g) {
        // Display Final Score
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics1.stringWidth("Score: " + applesEaten)) / 2, g.getFont().getSize());
        
        // Game Over text
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics2.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2);

        // Restart Message
        g.setColor(Color.white);
        g.setFont(new Font("Ink Free", Font.BOLD, 30));
        FontMetrics metrics3 = getFontMetrics(g.getFont());
        g.drawString("Press R to Restart", (SCREEN_WIDTH - metrics3.stringWidth("Press R to Restart")) / 2, SCREEN_HEIGHT / 2 + 50);
    }

    /**
     * This method is called by the Timer on each tick.
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    /**
     * Inner class to handle keyboard input.
     */
    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (running) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        if (direction != 'R') { // Prevent the snake from reversing
                            direction = 'L';
                        }
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (direction != 'L') {
                            direction = 'R';
                        }
                        break;
                    case KeyEvent.VK_UP:
                        if (direction != 'D') {
                            direction = 'U';
                        }
                        break;
                    case KeyEvent.VK_DOWN:
                        if (direction != 'U') {
                            direction = 'D';
                        }
                        break;
                }
            } else {
                // If the game is over, check for the restart key
                if (e.getKeyCode() == KeyEvent.VK_R) {
                    startGame();
                }
            }
        }
    }
}
