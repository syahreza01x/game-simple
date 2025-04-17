
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.Timer;

public class GameSimpleMultiplayer extends JPanel implements ActionListener, KeyListener {
    final int ROWS = 20, COLS = 60;
    char[][] arena = new char[ROWS][COLS];
    int heartX1 = ROWS - 2, heartY1 = COLS / 4;  // Player 1 (WASD) Heart Position
    int heartX2 = ROWS - 2, heartY2 = COLS - COLS / 4; // Player 2 (Arrow keys) Heart Position

    Timer timer;
    Random rand = new Random();

    int score1 = 0, score2 = 0;
    int highScore1 = 0, highScore2 = 0;
    boolean player1Dead = false;
    boolean player2Dead = false;

    public GameSimpleMultiplayer() {
        setFocusable(true);
        addKeyListener(this);
        timer = new Timer(1000 / 10, this); // 60 FPS
        timer.start();
    }

    public void actionPerformed(ActionEvent e) {
        // Update bullets and spawn new bullets only if players are not dead
        if (!player1Dead || !player2Dead) {
            updateBullets();
            spawnBullets();
        }

        // Increment score for both players, but only if they are not dead
        if (!player1Dead) score1++;
        if (!player2Dead) score2++;

        // Check collision for player 1
        if (!player1Dead && arena[heartX1][heartY1] == '*') {
            player1Dead = true;
            if (score1 > highScore1) {
                highScore1 = score1;
            }
            repaint();
            JOptionPane.showMessageDialog(this, "💀 Player 1 Kena peluru! Game Over.\nScore: " + score1 + "\nHigh Score: " + highScore1);
        }

        // Check collision for player 2
        if (!player2Dead && arena[heartX2][heartY2] == '*') {
            player2Dead = true;
            if (score2 > highScore2) {
                highScore2 = score2;
            }
            repaint();
            JOptionPane.showMessageDialog(this, "💀 Player 2 Kena peluru! Game Over.\nScore: " + score2 + "\nHigh Score: " + highScore2);
        }

        // If both players are dead, reset the game
        if (player1Dead && player2Dead) {
            JOptionPane.showMessageDialog(this, "🎮 Both players are dead! Game Over.");
            resetGame();
        }

        // Update the arena positions based on player statuses
        arena[heartX1][heartY1] = player1Dead ? ' ' : '♥'; // Player 1
        arena[heartX2][heartY2] = player2Dead ? ' ' : '♦'; // Player 2
        repaint();
    }

    void resetGame() {
        for (int i = 0; i < ROWS; i++) {
            Arrays.fill(arena[i], ' ');
        }
        heartX1 = ROWS - 2;
        heartY1 = COLS / 4;
        heartX2 = ROWS - 2;
        heartY2 = COLS - COLS / 4;
        score1 = 0;
        score2 = 0;
        player1Dead = false;
        player2Dead = false;
        timer.start();
    }

    void updateBullets() {
        for (int i = ROWS - 2; i >= 0; i--) {
            for (int j = 0; j < COLS; j++) {
                if (arena[i][j] == '*') {
                    if (arena[i + 1][j] == '♥' || arena[i + 1][j] == '♦') {
                        arena[i + 1][j] = 'X';  // Bullet hit player
                    } else {
                        arena[i + 1][j] = '*';
                    }
                    arena[i][j] = ' ';
                }
            }
        }
    }

    void spawnBullets() {
        for (int i = 0; i < COLS; i++) {
            if (rand.nextInt(300) < 10) { // 10% chance to spawn bullet
                arena[0][i] = '*';
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.black);

        int cellWidth = getWidth() / COLS;
        int cellHeight = getHeight() / ROWS;
        int fontSize = Math.min(cellWidth, cellHeight) - 2;

        g.setFont(new Font("Monospaced", Font.BOLD, fontSize));

        // Draw the game arena
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                char c = arena[i][j];
                if (c == '♥') {
                    g.setColor(Color.RED); // Player 1's Heart (red)
                } else if (c == '♦') {
                    g.setColor(Color.BLUE); // Player 2's Heart (blue)
                } else if (c == '*') {
                    g.setColor(Color.YELLOW); // Bullet (yellow)
                } else if (c == 'X') {
                    g.setColor(Color.ORANGE); // Collision mark (orange)
                } else {
                    g.setColor(Color.WHITE);
                }
                g.drawString(String.valueOf(c == '\0' ? ' ' : c),
                        j * cellWidth + cellWidth / 4,
                        i * cellHeight + (3 * cellHeight / 4));
            }
        }

        // Draw scores
        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Player 1 Score: " + score1, 20, 30);
        g.drawString("Player 2 Score: " + score2, 20, 60);
        g.drawString("High Score P1: " + highScore1, 20, 90);
        g.drawString("High Score P2: " + highScore2, 20, 120);
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (player1Dead && player2Dead) return; // Do nothing if both players are dead

        arena[heartX1][heartY1] = ' '; // clear previous position for player 1
        arena[heartX2][heartY2] = ' '; // clear previous position for player 2

        // Player 1 controls (WASD)
        if (!player1Dead) {
            switch (key) {
                case KeyEvent.VK_W:
                    if (heartX1 > 0) heartX1--;
                    break;
                case KeyEvent.VK_S:
                    if (heartX1 < ROWS - 1) heartX1++;
                    break;
                case KeyEvent.VK_A:
                    if (heartY1 > 0) heartY1--;
                    break;
                case KeyEvent.VK_D:
                    if (heartY1 < COLS - 1) heartY1++;
                    break;
            }
        }

        // Player 2 controls (Arrow keys)
        if (!player2Dead) {
            switch (key) {
                case KeyEvent.VK_UP:
                    if (heartX2 > 0) heartX2--;
                    break;
                case KeyEvent.VK_DOWN:
                    if (heartX2 < ROWS - 1) heartX2++;
                    break;
                case KeyEvent.VK_LEFT:
                    if (heartY2 > 0) heartY2--;
                    break;
                case KeyEvent.VK_RIGHT:
                    if (heartY2 < COLS - 1) heartY2++;
                    break;
            }
        }
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Undertale Mini Arena - Multiplayer");
        GameSimpleMultiplayer game = new GameSimpleMultiplayer();
        frame.add(game);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}