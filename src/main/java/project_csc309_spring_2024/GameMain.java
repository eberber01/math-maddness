package project_csc309_spring_2024;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class GameMain extends JFrame implements ActionListener, PropertyChangeListener {

    JPanel startScreen = new JPanel();
    JPanel levelScreen = new JPanel();
    JPanel timeAttackScreen = new JPanel();

    JPanel cpuPvp = new JPanel();
    JPanel duelScreen = new JPanel();


    LevelPanel levelPanel;
    DuelPanel duelPanel;
    ModePanel modePanel;
    MapPanel mapPanel;
    TimeScorePanel timeScorePanel;
    Button selectButton;

    String host = "localhost";
    int port = 3091;
    Client client = new Client(host, port);

    Font customFont;
    AudioPlayer audioPlayer;


    public GameMain() {

        CharacterManager characterManager = new CharacterManager();

        GameData.getInstance().setMainFrame(this);
        GameData.getInstance().addPropertyChangeListener(this);
        GameData data = GameData.getInstance();

        audioPlayer = new AudioPlayer();
        customFont = FontLoader.loadCustomFont("/fonts/Minecraftia-Regular.ttf", 12f);
        SoundLoader.loadSounds(audioPlayer);

        GameData.getInstance().setCustomFont(customFont);
        GameData.getInstance().setAudioPlayer(audioPlayer);

        // ---- CREATE : Start Screen
        startScreen.setLayout(new GridLayout(1, 1));
        StartPanel startPanel = new StartPanel();
        Button playButton = new Button("PRESS TO START", 600, 400, 200, 60);
        playButton.setButtonColor(Color.blue, Color.white);
        playButton.addSelf(startPanel);
        playButton.setFont(customFont.deriveFont(12f));
        playButton.addActionListener(this);
        Button changeHost = new Button("Change Host", 80, 10, 110, 20);
        changeHost.addActionListener(this);
        startPanel.add(changeHost);
        startScreen.add(startPanel);

        // ---- CREATE : Mode Select Screen
        modePanel = new ModePanel();
        Button continueButton = new Button("CONTINUE", 300, 390, 150, 40);
        continueButton.setButtonColor(Color.white, Color.black);
        continueButton.setFont(customFont.deriveFont(12f));
        continueButton.addSelf(modePanel.getButtonSidePanel());
        continueButton.addActionListener(this);

        // ---- CREATE : Default Duel
        UserPlayer player = new UserPlayer(75, 200, "player 1", 100);
        player.setLocalPlayer();
        CharacterImages initialCpuImages = characterManager.getCharacterImages("gramps");
        CpuPlayer cpu = new CpuPlayer(325, 200, 100, initialCpuImages);

        GameData.getInstance().setCpuPlayer(cpu);
        Duel computerDuel = new Duel(player, cpu);
        Level level = new Level(computerDuel, 0);
        data.setLevel(level);
        duelPanel = new DuelPanel(data.getLevel().getDuel());


        // ---- CREATE : Level Select Screen
        levelScreen.setLayout(null);
        levelPanel = new LevelPanel();
        levelPanel.setBounds(0, 0, 600, 600);
        levelScreen.add(levelPanel);
        mapPanel = new MapPanel();
        mapPanel.setBounds(600, 0, 600, 500);
        levelScreen.add(mapPanel);
        selectButton = new Button("SELECT SCENE", 300, 450, 150, 40);
        selectButton.addSelf(mapPanel);
        selectButton.setFont(customFont.deriveFont(12f));
        selectButton.addActionListener(this);
        WeatherPanel weatherPanel = new WeatherPanel();
        weatherPanel.setBounds(600, 500, 600, 100);
        levelScreen.add(weatherPanel);

        GameData.getInstance().recalculate();
        MathPanel mathPanel = new MathPanel();
        mathPanel.add(GameData.getInstance().getTutor());
        add(startScreen);

        GameController controller = new GameController(player, cpu);
        mathPanel.addMouseListener(controller);
        mathPanel.addMouseMotionListener(controller);

        GameData.getInstance().addPropertyChangeListener(mathPanel);

        duelScreen.setLayout(new GridLayout(1, 2));
        duelScreen.add(duelPanel);
        duelScreen.add(mathPanel);

        // ---- CREATE : Time Attack Screen
        timeAttackScreen.setLayout(new GridLayout(1, 2));
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(2, 1));
        MathPanel mathPanelTT = new MathPanel();
        mathPanelTT.remove(GameData.getInstance().getTutor());
        mathPanelTT.addMouseListener(controller);
        mathPanelTT.addMouseMotionListener(controller);
        GameData.getInstance().addPropertyChangeListener(mathPanelTT);
        timeScorePanel = new TimeScorePanel();
        leftPanel.add(timeScorePanel);
        //leftPanel.add(new LeaderBoardPanel());
        timeAttackScreen.add(leftPanel);
        timeAttackScreen.add(mathPanelTT);



        // ---- CREATE : Cpu PVP Screen
        cpuPvp.setLayout(new GridLayout(1, 2));
        JPanel lPanel = new JPanel();
        lPanel.setLayout(new GridLayout(2, 1));
        MathPanel mpanel = new MathPanel();
        mpanel.remove(GameData.getInstance().getTutor());
        mpanel.addMouseListener(controller);
        mpanel.addMouseMotionListener(controller);
        GameData.getInstance().addPropertyChangeListener(mpanel);
        cpuPvp.add(lPanel);
        cpuPvp.add(mpanel);

    }

    public static void main(String[] args) {
        GameMain main = new GameMain();
        main.setTitle("Math Madness");
        main.setSize(1200, 600);
        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main.setVisible(true);
    }

    public void setLevelScreen(){
        getContentPane().removeAll();
        mapPanel.loadMaps();
        getContentPane().add(levelScreen);
        revalidate();
        repaint();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("gameOver".equals(evt.getPropertyName()) && GameData.getInstance().getMainFrame() == this) {
            boolean isWinner = (boolean) evt.getNewValue();
            new GameOverDialog(GameData.getInstance().getMainFrame(), isWinner);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof Button) {
            Button button = (Button) e.getSource();
            String label = button.getLabel();

            getContentPane().removeAll();
            String gameMode = GameData.getInstance().getGameMode();
            switch (label) {
                case "PRESS TO START":
                    GameData.getInstance().getMusicManager().playMusic("background");
                    audioPlayer.play("select");
                    getContentPane().add(modePanel);
                    break;
                case "CONTINUE":
                    if(!gameMode.equals("Story Mode")){
                        GameData.getInstance().getLevel().unlockAllOperations();
                    }
                    audioPlayer.play("select");
                    if (gameMode.equals("Time Attack")) {
                        getContentPane().add(timeAttackScreen);
                        timeScorePanel.startTimer();
                    } else {
                        getContentPane().add(levelScreen);
                    }
                    break;
                case "SELECT SCENE":

                    if (gameMode.equals("Join PvP Game")) {
                        GameData.getInstance().getLevel().unlockAllOperations();
                        client.start();
                        while (!client.isReady()) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                        duelPanel.setDuel(GameData.getInstance().getLevel().getDuel());
                        duelPanel.setBackgroundImage(levelPanel.getBackgroundImage());
                        getContentPane().add(duelScreen) ;
                    }
                    else if (gameMode.equals("Time Attack")) {
                        getContentPane().add(timeAttackScreen);
                        timeScorePanel.startTimer();
                    }
                    else{
                        duelPanel.setDuel(GameData.getInstance().getLevel().getDuel());
                        duelPanel.setBackgroundImage(levelPanel.getBackgroundImage());
                        getContentPane().add(duelScreen) ;
                    }

                    audioPlayer.play("select");
                    break;
                case "Change Host":
                    changeHostDialogue();
                default:
                    break;
            }
            revalidate();
            repaint();
        }
    }


    public void changeHostDialogue(){
        String defaultValue = host;
        JTextField textField = new JTextField(defaultValue);
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Enter Host:"));
        panel.add(textField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Enter Host:",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String enteredText = textField.getText();
            client.setHost(enteredText);
        }

        add(startScreen);
    }
}
