// Full updated code with mode selection, difficulty, and character skills with cooldowns
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.Timer;

public class GameSimpleMultiplayer extends JPanel implements ActionListener, KeyListener {
    final int ROWS = 20, COLS = 60;
    char[][] arena = new char[ROWS][COLS];
    int heartX1 = ROWS - 2, heartY1 = COLS / 4;
    int heartX2 = ROWS - 2, heartY2 = COLS - COLS / 4;

    Timer timer;
    Random rand = new Random();

    int score1 = 0, score2 = 0;
    int highScore1 = 0, highScore2 = 0;
    boolean player1Dead = false;
    boolean player2Dead = false;

    boolean isSinglePlayer = false;
    int difficulty = 10; // default spawn chance base

    boolean skill1Ready = true;
    boolean skill2Ready = true;
    long skill1LastUsed = 0;
    long skill2LastUsed = 0;
    boolean timeStopped = false;
    long timeStopStart = 0;

    public GameSimpleMultiplayer() {
        setFocusable(true);
        addKeyListener(this);
        showMenu();
        timer = new Timer(1000 / 10, this);
        timer.start();
    }

    void showMenu() {
        String[] modes = {"Single Player", "Multiplayer"};
        int mode = JOptionPane.showOptionDialog(null, "Pilih Mode:", "Mode",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, modes, modes[0]);
        isSinglePlayer = mode == 0;

        String[] levels = {"Easy", "Medium", "Hard", "Insane"};
        int diff = JOptionPane.showOptionDialog(null, "Pilih Difficulty:", "Difficulty",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, levels, levels[0]);
        switch (diff) {
            case 0: difficulty = 5; break;
            case 1: difficulty = 10; break;
            case 2: difficulty = 15; break;
            case 3: difficulty = 25; break;
        }
    }

    public void actionPerformed(ActionEvent e) {
        long now = System.currentTimeMillis();

        // Manage time stop duration
        if (timeStopped && now - timeStopStart > 5000) {
            timeStopped = false;
        }

        if (!player1Dead || !player2Dead) {
            if (!timeStopped) updateBullets();
            spawnBullets();
        }

        if (!player1Dead) score1++;
        if (!player2Dead) score2++;

        if (!player1Dead && arena[heartX1][heartY1] == '*') {
            player1Dead = true;
            if (score1 > highScore1) highScore1 = score1;
            repaint();
            JOptionPane.showMessageDialog(this, "\uD83D\uDC80 Player 1 Kena peluru!\nScore: " + score1);
        }
        if (!player2Dead && arena[heartX2][heartY2] == '*') {
            player2Dead = true;
            if (score2 > highScore2) highScore2 = score2;
            repaint();
            JOptionPane.showMessageDialog(this, "\uD83D\uDC80 Player 2 Kena peluru!\nScore: " + score2);
        }

        if ((isSinglePlayer && player1Dead) || (!isSinglePlayer && player1Dead && player2Dead)) {
            JOptionPane.showMessageDialog(this, "\uD83C\uDFAE Game Over!");
            resetGame();
        }

        if (!player1Dead) arena[heartX1][heartY1] = '♥';
        if (!player2Dead) arena[heartX2][heartY2] = '♦';

        repaint();
    }

    void resetGame() {
        for (int i = 0; i < ROWS; i++) Arrays.fill(arena[i], ' ');
        heartX1 = ROWS - 2;
        heartY1 = COLS / 4;
        heartX2 = ROWS - 2;
        heartY2 = COLS - COLS / 4;
        score1 = score2 = 0;
        player1Dead = player2Dead = false;
        skill1Ready = skill2Ready = true;
        timer.start();
    }

    void updateBullets() {
        for (int i = ROWS - 2; i >= 0; i--) {
            for (int j = 0; j < COLS; j++) {
                if (arena[i][j] == '*') {
                    if (arena[i + 1][j] == '♥' || arena[i + 1][j] == '♦') {
                        arena[i + 1][j] = 'X';
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
            if (rand.nextInt(300) < difficulty) {
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

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                char c = arena[i][j];
                if (c == '♥') g.setColor(Color.RED);
                else if (c == '♦') g.setColor(Color.BLUE);
                else if (c == '*') g.setColor(Color.YELLOW);
                else if (c == 'X') g.setColor(Color.ORANGE);
                else g.setColor(Color.WHITE);

                g.drawString(String.valueOf(c == '\0' ? ' ' : c), j * cellWidth + cellWidth / 4,
                        i * cellHeight + (3 * cellHeight / 4));
            }
        }

        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("P1 Score: " + score1 + " | High: " + highScore1, 20, 30);
        g.drawString("Skill: " + (skill1Ready ? "Ready" : "Cooldown"), 20, 60);
        if (!isSinglePlayer) {
            g.drawString("P2 Score: " + score2 + " | High: " + highScore2, 20, 90);
            g.drawString("Skill: " + (skill2Ready ? "Ready" : "Cooldown"), 20, 120);
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        long now = System.currentTimeMillis();

        if (!player1Dead) arena[heartX1][heartY1] = ' ';
        if (!player2Dead) arena[heartX2][heartY2] = ' ';

        if (!player1Dead) {
            switch (key) {
                case KeyEvent.VK_W: if (heartX1 > 0) heartX1--; break;
                case KeyEvent.VK_S: if (heartX1 < ROWS - 1) heartX1++; break;
                case KeyEvent.VK_A: if (heartY1 > 0) heartY1--; break;
                case KeyEvent.VK_D: if (heartY1 < COLS - 1) heartY1++; break;
                case KeyEvent.VK_E:
                    if (skill1Ready) {
                        timeStopped = true;
                        timeStopStart = now;
                        skill1Ready = false;
                        skill1LastUsed = now;
                    }
                    break;
            }
        }

        if (!player2Dead && !isSinglePlayer) {
            switch (key) {
                case KeyEvent.VK_UP: if (heartX2 > 0) heartX2--; break;
                case KeyEvent.VK_DOWN: if (heartX2 < ROWS - 1) heartX2++; break;
                case KeyEvent.VK_LEFT: if (heartY2 > 0) heartY2--; break;
                case KeyEvent.VK_RIGHT: if (heartY2 < COLS - 1) heartY2++; break;
                case KeyEvent.VK_END:
                    if (skill2Ready) {
                        for (int i = Math.max(0, heartX2 - 2); i <= Math.min(ROWS - 1, heartX2 + 2); i++) {
                            for (int j = Math.max(0, heartY2 - 2); j <= Math.min(COLS - 1, heartY2 + 2); j++) {
                                if (arena[i][j] == '*') arena[i][j] = ' ';
                            }
                        }
                        skill2Ready = false;
                        skill2LastUsed = now;
                    }
                    break;
            }
        }

        // Cooldown check
        if (!skill1Ready && now - skill1LastUsed >= 15000) skill1Ready = true;
        if (!skill2Ready && now - skill2LastUsed >= 5000) skill2Ready = true;
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Undertale Mini Arena - Multiplayer");
        GameSimpleMultiplayer game = new GameSimpleMultiplayer();
        frame.add(game);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
