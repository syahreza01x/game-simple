// (sama seperti bagian import kamu)
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
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
    int lives1 = 3, lives2 = 3;
    boolean player1Dead = false;
    boolean player2Dead = false;
    boolean isSinglePlayer = true;

    boolean timeStopActive = false;
    boolean areaClearActive = false;
    long timeStopStart = 0;
    long areaClearStart = 0;
    long timeStopCooldownStart = -15000;
    long areaClearCooldownStart = -20000;

    boolean showTimeStopEffect = false;
    boolean showAreaClearEffect = false;

    int timeStopKey;
    int areaClearKey;

    static int defaultTimeStopKey = KeyEvent.VK_E;
    static int defaultAreaClearKey = KeyEvent.VK_END;

    private void playSound(String relativePath) {
        try {
            File soundFile = new File(relativePath);
            if (!soundFile.exists()) {
                System.out.println("File tidak ditemukan: " + relativePath);
                return;
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public GameSimpleMultiplayer(boolean singlePlayer) {
        this(singlePlayer, defaultTimeStopKey, defaultAreaClearKey);
    }

    public GameSimpleMultiplayer(boolean singlePlayer, int p1Key, int p2Key) {
        this.isSinglePlayer = singlePlayer;
        this.timeStopKey = p1Key;
        this.areaClearKey = p2Key;
        setFocusable(true);
        addKeyListener(this);
        timer = new Timer(1000 / 10, this);
        timer.start();
    }

    public void actionPerformed(ActionEvent e) {
        long now = System.currentTimeMillis();
        boolean timeStopWindow = timeStopActive && (now - timeStopStart < 5000);

        if (!player1Dead) score1++;
        if (!player2Dead && !isSinglePlayer && !timeStopWindow) score2++;

        showTimeStopEffect = timeStopActive && (now - timeStopStart <= 5000);
        showAreaClearEffect = areaClearActive && (now - areaClearStart <= 5000);

        if (timeStopWindow) {
            // Player 2 frozen
        } else {
            if (timeStopActive) {
                timeStopActive = false;
                timeStopCooldownStart = System.currentTimeMillis();
            }
            updateBullets();
            spawnBullets();
        }

        if (!player1Dead && arena[heartX1][heartY1] == '*') {
            lives1--;
            if (lives1 <= 0) {
                player1Dead = true;
                if (score1 > highScore1) highScore1 = score1;
                JOptionPane.showMessageDialog(this, "\uD83D\uDC80 Player 1 Kehabisan nyawa! Game Over.\nScore: " + score1 + "\nHigh Score: " + highScore1);
            }
        }

        if (!player2Dead && !isSinglePlayer && arena[heartX2][heartY2] == '*') {
            lives2--;
            if (lives2 <= 0) {
                player2Dead = true;
                if (score2 > highScore2) highScore2 = score2;
                JOptionPane.showMessageDialog(this, "\uD83D\uDC80 Player 2 Kehabisan nyawa! Game Over.\nScore: " + score2 + "\nHigh Score: " + highScore2);
            }
        }

        if ((player1Dead && (player2Dead || isSinglePlayer))) {
            JOptionPane.showMessageDialog(this, "\uD83C\uDFAE Game Over. Player mati.");
            resetGame();
        }

        if (!player1Dead) arena[heartX1][heartY1] = '♥'; else arena[heartX1][heartY1] = ' ';
        if (!player2Dead && !isSinglePlayer) arena[heartX2][heartY2] = '♦'; else arena[heartX2][heartY2] = ' ';

        if (areaClearActive && (now - areaClearStart <= 5000)) {
            clearBulletsAroundPlayer2();
        } else {
            if (areaClearActive) {
                areaClearActive = false;
                areaClearCooldownStart = System.currentTimeMillis();
            }
        }

        repaint();
    }

    boolean isTimeStopReady() {
        return !timeStopActive && (System.currentTimeMillis() - timeStopCooldownStart >= 15000);
    }

    boolean isAreaClearReady() {
        return !areaClearActive && (System.currentTimeMillis() - areaClearCooldownStart >= 20000);
    }

    void resetGame() {
        for (int i = 0; i < ROWS; i++) Arrays.fill(arena[i], ' ');
        heartX1 = ROWS - 2; heartY1 = COLS / 4;
        heartX2 = ROWS - 2; heartY2 = COLS - COLS / 4;
        score1 = 0; score2 = 0;
        lives1 = 3; lives2 = 3;
        player1Dead = false; player2Dead = false;
        timeStopActive = false; areaClearActive = false;
        timeStopCooldownStart = -15000; areaClearCooldownStart = -20000;
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
            if (rand.nextInt(300) < 10) arena[0][i] = '*';
        }
    }

    void clearBulletsAroundPlayer2() {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int x = heartX2 + i, y = heartY2 + j;
                if (x >= 0 && x < ROWS && y >= 0 && y < COLS && arena[x][y] == '*') {
                    arena[x][y] = ' ';
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.black);

        int cw = getWidth() / COLS;
        int ch = getHeight() / ROWS;
        int fontSize = Math.min(cw, ch) - 2;

        g.setFont(new Font("Monospaced", Font.BOLD, fontSize));

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                char c = arena[i][j];
                g.setColor(c == '♥' ? Color.RED : c == '♦' ? Color.BLUE : c == '*' ? Color.YELLOW : c == 'X' ? Color.ORANGE : Color.WHITE);
                g.drawString(String.valueOf(c == '\0' ? ' ' : c), j * cw + cw / 4, i * ch + (3 * ch / 4));
            }
        }

        if (showTimeStopEffect) {
            g.setColor(new Color(0, 255, 255, 80));
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        if (showAreaClearEffect && !isSinglePlayer) {
            int px = heartY2 * getWidth() / COLS;
            int py = heartX2 * getHeight() / ROWS;
            g.setColor(new Color(255, 255, 0, 80));
            g.fillOval(px - 30, py - 30, 60, 60);
        }

        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Player 1 Score: " + score1, 20, 30);
        g.drawString("High Score P1: " + highScore1, 20, 60);
        g.drawString("Skill: " + (isTimeStopReady() ? "Ready" : "Cooldown"), 20, 90);

        g.setColor(Color.WHITE);
        g.drawString("Lives P1 : ", 250, 30);
        for (int i = 0; i < lives1; i++) {
            g.setColor(Color.PINK);
            g.drawString("♥", 350 + (i * 20), 30);
        }

        if (!isSinglePlayer) {
            g.setColor(Color.GREEN);
            g.drawString("Player 2 Score: " + score2, 20, 120);
            g.drawString("High Score P2: " + highScore2, 20, 150);
            g.drawString("Skill: " + (isAreaClearReady() ? "Ready" : "Cooldown"), 20, 180);

            g.setColor(Color.WHITE);
            g.drawString("Lives P2 : ", 250, 60);
            for (int i = 0; i < lives2; i++) {
                g.setColor(Color.CYAN);
                g.drawString("♦", 350 + (i * 20), 60);
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (player1Dead && (player2Dead || isSinglePlayer)) return;

        arena[heartX1][heartY1] = ' ';
        arena[heartX2][heartY2] = ' ';

        if (!player1Dead) {
            switch (key) {
                case KeyEvent.VK_W -> { if (heartX1 > 0) heartX1--; }
                case KeyEvent.VK_S -> { if (heartX1 < ROWS - 1) heartX1++; }
                case KeyEvent.VK_A -> { if (heartY1 > 0) heartY1--; }
                case KeyEvent.VK_D -> { if (heartY1 < COLS - 1) heartY1++; }
            }
            if (key == timeStopKey && isTimeStopReady()) {
                timeStopActive = true;
                timeStopStart = System.currentTimeMillis();
                playSound("sounds/player1.wav");
            }
        }

        if (!player2Dead && !isSinglePlayer && !timeStopActive) {
            switch (key) {
                case KeyEvent.VK_UP -> { if (heartX2 > 0) heartX2--; }
                case KeyEvent.VK_DOWN -> { if (heartX2 < ROWS - 1) heartX2++; }
                case KeyEvent.VK_LEFT -> { if (heartY2 > 0) heartY2--; }
                case KeyEvent.VK_RIGHT -> { if (heartY2 < COLS - 1) heartY2++; }
            }
            if (key == areaClearKey && isAreaClearReady()) {
                areaClearActive = true;
                areaClearStart = System.currentTimeMillis();
                playSound("sounds/player2.wav");
            }
        }
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        String[] difficulties = {"Easy", "Normal", "Hard"};
        int difficultyChoice = JOptionPane.showOptionDialog(null, "Pilih Difficulty:", "Difficulty",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, difficulties, difficulties[0]);

        int delay = switch (difficultyChoice) {
            case 0 -> 1000 / 5;
            case 2 -> 1000 / 15;
            default -> 1000 / 10;
        };

        String[] options = {"Single Player", "Multiplayer", "Settings"};
        int choice = JOptionPane.showOptionDialog(null, "Pilih Mode:", "Mode Game",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (choice == 2) {
            String input1 = JOptionPane.showInputDialog("Tombol skill Player 1 (E.g., E, Q, R):");
            String input2 = JOptionPane.showInputDialog("Tombol skill Player 2 (E.g., END, L, O):");
            if (input1 != null && input1.length() == 1) {
                defaultTimeStopKey = KeyEvent.getExtendedKeyCodeForChar(input1.toUpperCase().charAt(0));
            }
            if (input2 != null && input2.length() == 1) {
                defaultAreaClearKey = KeyEvent.getExtendedKeyCodeForChar(input2.toUpperCase().charAt(0));
            }
            main(null);
            return;
        }

        JFrame frame = new JFrame("ReZy Retro Game");
        GameSimpleMultiplayer game = new GameSimpleMultiplayer(choice == 0, defaultTimeStopKey, defaultAreaClearKey);
        game.timer.setDelay(delay);
        frame.add(game);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
