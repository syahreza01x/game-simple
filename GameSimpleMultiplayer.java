import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import javax.swing.Timer;
import java.util.List;



public class GameSimpleMultiplayer extends JPanel implements ActionListener, KeyListener {

    private void playBGM(String filePath) {
        try {
            File soundFile = new File(filePath);
            if (!soundFile.exists()) {
                System.out.println("File tidak ditemukan: " + filePath);
                return;
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioStream);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY); // Putar terus-menerus
            bgmClip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    
    private void stopBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            bgmClip.close();
        }
    }

    final int ROWS = 20, COLS = 60;
    char[][] arena = new char[ROWS][COLS];
    int heartX1 = ROWS - 2, heartY1 = COLS / 4;
    int heartX2 = ROWS - 2, heartY2 = COLS - COLS / 4;
    Clip bgmClip;

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

    boolean shield1 = false, shield2 = false;
    long shield1Start = 0, shield2Start = 0;

    boolean speed1 = false, speed2 = false;
    long speed1Start = 0, speed2Start = 0;

    boolean skillLock1 = false, skillLock2 = false;
    long skillLock1Start = 0, skillLock2Start = 0;

    static int defaultTimeStopKey = KeyEvent.VK_E;
    static int defaultAreaClearKey = KeyEvent.VK_END;

    class Drop {
        int x, y;
        char icon;
        int type;
        Drop(int x, int y, char icon, int type) {
            this.x = x; this.y = y; this.icon = icon; this.type = type;
        }
    }
    List<Drop> drops = new ArrayList<>();

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

        // Update power-up/down durations
        if (shield1 && now - shield1Start > 5000) shield1 = false;
        if (shield2 && now - shield2Start > 5000) shield2 = false;
        if (speed1 && now - speed1Start > 5000) speed1 = false;
        if (speed2 && now - speed2Start > 5000) speed2 = false;
        if (skillLock1 && now - skillLock1Start > 20000) skillLock1 = false;
        if (skillLock2 && now - skillLock2Start > 20000) skillLock2 = false;

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
            spawnDrops();
            updateDrops();
        }

        if (!player1Dead && arena[heartX1][heartY1] == '*' && !shield1) {
            lives1--;
            if (lives1 <= 0) {
                player1Dead = true;
                if (score1 > highScore1) highScore1 = score1;
                JOptionPane.showMessageDialog(this, "\uD83D\uDC80 Player 1 Kehabisan nyawa! Game Over.\nScore: " + score1 + "\nHigh Score: " + highScore1);
            }
        }

        if (!player2Dead && !isSinglePlayer && arena[heartX2][heartY2] == '*' && !shield2) {
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

    void spawnDrops() {
        // Tingkatkan peluang spawn drop (1,5x lebih banyak)
        if (rand.nextInt(200) < 3) { // Sebelumnya: rand.nextInt(200) < 2
            int col = rand.nextInt(COLS);
            int type;
            char icon;
            int r = rand.nextInt(100);
            if (r < 30) { type = 1; icon = '+'; }
            else if (r < 55) { type = 2; icon = '⛨'; }
            else if (r < 80) { type = 3; icon = '⇶'; }
            else if (r < 90) { type = 4; icon = '⇄'; }
            else { type = 5; icon = '✖'; }
            drops.add(new Drop(0, col, icon, type));
        }
    }

void updateDrops() {
    List<Drop> toRemove = new ArrayList<>();
    for (Drop d : drops) {
        // Perlahankan kecepatan turun drop (turun setiap 2 frame)
        if (rand.nextInt(2) == 0) { // Turun lebih lambat dari peluru
            if (d.x < ROWS - 1) d.x++;
            else toRemove.add(d);
        }

        if (d.x == heartX1 && d.y == heartY1 && !player1Dead) {
            applyDropEffect(1, d.type);
            toRemove.add(d);
        } else if (!isSinglePlayer && d.x == heartX2 && d.y == heartY2 && !player2Dead) {
            applyDropEffect(2, d.type);
            toRemove.add(d);
        }
    }
    drops.removeAll(toRemove);
}

    void applyDropEffect(int player, int type) {
        if (player == 1) {
            switch (type) {
                case 1 -> { if (lives1 < 5) lives1++; }
                case 2 -> { shield1 = true; shield1Start = System.currentTimeMillis(); }
                case 3 -> { speed1 = true; speed1Start = System.currentTimeMillis(); }
                case 4 -> { if (!isSinglePlayer && lives2 > 1) lives2--; else player2Dead = true; lives1++; }
                case 5 -> { skillLock2 = true; skillLock2Start = System.currentTimeMillis(); }
            }
        } else {
            switch (type) {
                case 1 -> { if (lives2 < 5) lives2++; }
                case 2 -> { shield2 = true; shield2Start = System.currentTimeMillis(); }
                case 3 -> { speed2 = true; speed2Start = System.currentTimeMillis(); }
                case 4 -> { if (lives1 > 1) lives1--; else player1Dead = true; lives2++; }
                case 5 -> { skillLock1 = true; skillLock1Start = System.currentTimeMillis(); }
            }
        }
    }

    boolean isTimeStopReady() {
        return !timeStopActive && (System.currentTimeMillis() - timeStopCooldownStart >= 15000) && !skillLock1;
    }

    boolean isAreaClearReady() {
        return !areaClearActive && (System.currentTimeMillis() - areaClearCooldownStart >= 20000) && !skillLock2;
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
    shield1 = false; shield2 = false;
    speed1 = false; speed2 = false;
    skillLock1 = false; skillLock2 = false;
    drops.clear();

    // Hentikan BGM
    stopBGM();
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

        // Draw arena
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                char c = arena[i][j];
                g.setColor(c == '♥' ? Color.RED : c == '♦' ? Color.BLUE : c == '*' ? Color.YELLOW : c == 'X' ? Color.ORANGE : Color.WHITE);
                g.drawString(String.valueOf(c == '\0' ? ' ' : c), j * cw + cw / 4, i * ch + (3 * ch / 4));
            }
        }

        // Draw drops
        for (Drop d : drops) {
            g.setColor(d.type == 1 ? Color.GREEN : 
                      d.type == 2 ? Color.CYAN : 
                      d.type == 3 ? Color.MAGENTA : 
                      d.type == 4 ? Color.RED : Color.ORANGE);
            g.drawString(String.valueOf(d.icon), d.y * cw + cw / 4, d.x * ch + (3 * ch / 4));
        }

        // Draw effects
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

        // Draw player 1 info
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

        // Draw player 2 info if multiplayer
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

        // Draw active power-ups
        if (shield1) {
            g.setColor(new Color(0, 255, 255, 100));
            g.fillOval(heartY1 * cw - 5, heartX1 * ch - 5, fontSize + 10, fontSize + 10);
        }
        if (shield2 && !isSinglePlayer) {
            g.setColor(new Color(0, 255, 255, 100));
            g.fillOval(heartY2 * cw - 5, heartX2 * ch - 5, fontSize + 10, fontSize + 10);
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (player1Dead && (player2Dead || isSinglePlayer)) return;

        arena[heartX1][heartY1] = ' ';
        arena[heartX2][heartY2] = ' ';

        if (!player1Dead) {
            int moveStep = speed1 ? 2 : 1;
            switch (key) {
                case KeyEvent.VK_W -> { if (heartX1 > 0) heartX1 = Math.max(0, heartX1 - moveStep); }
                case KeyEvent.VK_S -> { if (heartX1 < ROWS - 1) heartX1 = Math.min(ROWS - 1, heartX1 + moveStep); }
                case KeyEvent.VK_A -> { if (heartY1 > 0) heartY1 = Math.max(0, heartY1 - moveStep); }
                case KeyEvent.VK_D -> { if (heartY1 < COLS - 1) heartY1 = Math.min(COLS - 1, heartY1 + moveStep); }
            }
            if (key == timeStopKey && isTimeStopReady()) {
                timeStopActive = true;
                timeStopStart = System.currentTimeMillis();
                playSound("sounds/player1.wav");
            }
        }

        if (!player2Dead && !isSinglePlayer && !timeStopActive) {
            int moveStep = speed2 ? 2 : 1;
            switch (key) {
                case KeyEvent.VK_UP -> { if (heartX2 > 0) heartX2 = Math.max(0, heartX2 - moveStep); }
                case KeyEvent.VK_DOWN -> { if (heartX2 < ROWS - 1) heartX2 = Math.min(ROWS - 1, heartX2 + moveStep); }
                case KeyEvent.VK_LEFT -> { if (heartY2 > 0) heartY2 = Math.max(0, heartY2 - moveStep); }
                case KeyEvent.VK_RIGHT -> { if (heartY2 < COLS - 1) heartY2 = Math.min(COLS - 1, heartY2 + moveStep); }
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
    
        // Mulai BGM
        game.playBGM("sounds/bgm.wav");
    }
}