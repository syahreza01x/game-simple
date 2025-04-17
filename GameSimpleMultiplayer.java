import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.Timer;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;



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
    boolean isSinglePlayer = true;

    // Skill state
    boolean timeStopActive = false;
    boolean areaClearActive = false;
    long timeStopStart = 0;
    long areaClearStart = 0;
    long timeStopCooldownStart = -15000;
    long areaClearCooldownStart = -5000;

    // Skill key mapping
    int timeStopKey;
    int areaClearKey;

    // Default keys (static so bisa di-set sebelum buat objek)
    static int defaultTimeStopKey = KeyEvent.VK_E;
    static int defaultAreaClearKey = KeyEvent.VK_END;

    private void playSound(String filePath) {
        try {
            File soundFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // Constructors
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
    
        // Jika timeStop aktif, hanya Player 2 yang tidak bisa bergerak dan skornya berhenti
        if (timeStopActive && (now - timeStopStart < 5000)) {
            if (!player1Dead) score1++; // Player 1 tetap mendapatkan skor
        } else {
            timeStopActive = false; // Jika waktu skill sudah habis, lanjutkan aksi
            updateBullets();
            spawnBullets();
        }
    
        // Skor hanya diperbarui untuk Player 2 jika mereka tidak mati dan timeStop tidak aktif
        if (!player2Dead && !isSinglePlayer && !timeStopActive) score2++;
    
        // Pemeriksaan kematian Player 1 dan Player 2
        if (!player1Dead && arena[heartX1][heartY1] == '*') {
            player1Dead = true;
            if (score1 > highScore1) highScore1 = score1;
            repaint();
            JOptionPane.showMessageDialog(this, "💀 Player 1 Kena peluru! Game Over.\nScore: " + score1 + "\nHigh Score: " + highScore1);
        }
    
        if (!player2Dead && !isSinglePlayer && arena[heartX2][heartY2] == '*') {
            player2Dead = true;
            if (score2 > highScore2) highScore2 = score2;
            repaint();
            JOptionPane.showMessageDialog(this, "💀 Player 2 Kena peluru! Game Over.\nScore: " + score2 + "\nHigh Score: " + highScore2);
        }
    
        // Game Over jika kedua player mati
        if ((player1Dead && (player2Dead && isSinglePlayer))) {
            JOptionPane.showMessageDialog(this, "🎮 Game Over. Player mati.");
            resetGame();
        }
    
        if ((player1Dead && (player2Dead || isSinglePlayer))) {
            JOptionPane.showMessageDialog(this, "🎮 Game Over. Player mati.");
            resetGame();
        }
    
        // Perbarui posisi pemain
        if (!player1Dead) arena[heartX1][heartY1] = '♥'; else arena[heartX1][heartY1] = ' ';
        if (!player2Dead && !isSinglePlayer) arena[heartX2][heartY2] = '♦'; else arena[heartX2][heartY2] = ' ';
    
        // Cek areaClear jika aktif
        if (areaClearActive && (now - areaClearStart <= 5000)) {
            clearBulletsAroundPlayer2();
        } else {
            areaClearActive = false;
        }
    
        repaint();
    }
    
    
    

    void resetGame() {
        for (int i = 0; i < ROWS; i++) Arrays.fill(arena[i], ' ');
        heartX1 = ROWS - 2; heartY1 = COLS / 4;
        heartX2 = ROWS - 2; heartY2 = COLS - COLS / 4;
        score1 = 0; score2 = 0;
        player1Dead = false; player2Dead = false;
        timeStopActive = false; areaClearActive = false;
        timeStopCooldownStart = -15000; areaClearCooldownStart = -5000;
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

        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Player 1 Score: " + score1, 20, 30);
        g.drawString("High Score P1: " + highScore1, 20, 60);
        g.drawString("Skill: " + (System.currentTimeMillis() - timeStopCooldownStart >= 15000 ? "Ready" : "Cooldown"), 20, 90);

        if (!isSinglePlayer) {
            g.drawString("Player 2 Score: " + score2, 20, 120);
            g.drawString("High Score P2: " + highScore2, 20, 150);
            g.drawString("Skill: " + (System.currentTimeMillis() - areaClearCooldownStart >= 5000 ? "Ready" : "Cooldown"), 20, 180);
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (player1Dead && (player2Dead || isSinglePlayer)) return;
    
        arena[heartX1][heartY1] = ' ';
        arena[heartX2][heartY2] = ' ';
    
        if (!player1Dead) {
            switch (key) {
                case KeyEvent.VK_W: if (heartX1 > 0) heartX1--; break;
                case KeyEvent.VK_S: if (heartX1 < ROWS - 1) heartX1++; break;
                case KeyEvent.VK_A: if (heartY1 > 0) heartY1--; break;
                case KeyEvent.VK_D: if (heartY1 < COLS - 1) heartY1++; break;
            }
            if (key == timeStopKey && System.currentTimeMillis() - timeStopCooldownStart >= 15000) {
                timeStopActive = true;
                timeStopStart = System.currentTimeMillis();
                timeStopCooldownStart = timeStopStart;
                playSound("E:/Game/game-simple/sounds/player1.wav"); // Mainkan sound effect untuk Player 1
            }
        }
    
        if (!player2Dead && !isSinglePlayer && !timeStopActive) { // Pastikan Player 2 tidak bergerak saat timeStop aktif
            switch (key) {
                case KeyEvent.VK_UP: if (heartX2 > 0) heartX2--; break;
                case KeyEvent.VK_DOWN: if (heartX2 < ROWS - 1) heartX2++; break;
                case KeyEvent.VK_LEFT: if (heartY2 > 0) heartY2--; break;
                case KeyEvent.VK_RIGHT: if (heartY2 < COLS - 1) heartY2++; break;
            }
            if (key == areaClearKey && System.currentTimeMillis() - areaClearCooldownStart >= 5000) {
                areaClearActive = true;
                areaClearStart = System.currentTimeMillis();
                areaClearCooldownStart = areaClearStart;
                playSound("E:/Game/game-simple/sounds/player2.wav"); // Mainkan sound effect untuk Player 2
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
            case 0 -> 1000 / 5;   // Easy = lambat
            case 2 -> 1000 / 15;  // Hard = cepat
            default -> 1000 / 10; // Normal
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
            main(null); // restart
            return;
        }
    
        JFrame frame = new JFrame("Undertale Mini Arena");
        GameSimpleMultiplayer game = new GameSimpleMultiplayer(choice == 0, defaultTimeStopKey, defaultAreaClearKey);
        game.timer.setDelay(delay); // apply difficulty
        frame.add(game);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

