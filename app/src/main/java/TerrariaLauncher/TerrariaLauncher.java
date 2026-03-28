package TerrariaLauncher;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.ui.FlatLineBorder;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;

public class TerrariaLauncher {
    public static void main(String[] args) {
        File runningLocation;
        try {
            String path = TerrariaLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File jarFile = new File(path);
            if (path.contains(".app")) {
                runningLocation = jarFile.getParentFile().getParentFile().getParentFile().getParentFile();
            } else {
                runningLocation = new File(".");
            }
        } catch (Exception e) {
            runningLocation = new File(".");
        }

        final File finalLocation = runningLocation;

        try {
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
            URL fontUrl = TerrariaLauncher.class.getResource("/Andy-Bold.ttf");
            if (fontUrl != null) {
                Font andyFont = Font.createFont(Font.TRUETYPE_FONT, fontUrl.openStream());
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(andyFont);
                LauncherUtils.setUIFont(andyFont.deriveFont(18f));
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        String[] messages = {"Now launching Terraria 3: Electric Boogalee", "dunno ran out of ideas", "Terraria? More like... uhh... Terraria! OOOOOHHHHHH!!!1!1!!1!"};
        JFrame mainFrame = new JFrame("Terraria Launcher: " + messages[(int)(Math.random() * messages.length)]);
        
        BackgroundPanel bgPanel = new BackgroundPanel("/background.png");
        mainFrame.setContentPane(bgPanel);
        mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        AnimatedLogo logo = new AnimatedLogo("/TerrariaLauncherLogo.png");
        
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setOpaque(false);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        logoPanel.add(logo, BorderLayout.CENTER);
        
        bgPanel.add(logoPanel, BorderLayout.NORTH);

        JPanel headerWrapper = new JPanel();
        headerWrapper.setOpaque(false);
        JPanel headerPanel = new JPanel(new BorderLayout(20, 0));
        headerPanel.setPreferredSize(new Dimension(400, 60));
        headerPanel.setOpaque(false);
        headerPanel.putClientProperty("FlatLaf.style", "arc: 20; background: rgba(30, 35, 60, 200)");
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new FlatLineBorder(new Insets(0,0,0,0), new Color(60, 70, 110), 2, 20),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        JLabel selectLabel = new JLabel("Select Instance:", JLabel.CENTER);
        selectLabel.setForeground(Color.WHITE);
        selectLabel.setFont(selectLabel.getFont().deriveFont(Font.BOLD, 26f));
        headerPanel.add(selectLabel, BorderLayout.CENTER);

        JPanel instancePanel = new JPanel();
        instancePanel.setLayout(new BoxLayout(instancePanel, BoxLayout.Y_AXIS));
        instancePanel.setOpaque(false);
        instancePanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 50, 50));

        RefreshButton refreshBtn = new RefreshButton(instancePanel, finalLocation);
        headerPanel.add(refreshBtn, BorderLayout.EAST);

        headerPanel.add(Box.createHorizontalStrut(40), BorderLayout.WEST);
        headerWrapper.add(headerPanel);

        LauncherUtils.scanAndPopulate(instancePanel, finalLocation);

        JScrollPane scrollPane = new JScrollPane(instancePanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        JPanel middleWrapper = new JPanel(new BorderLayout());
        middleWrapper.setOpaque(false);
        middleWrapper.add(headerWrapper, BorderLayout.NORTH);
        middleWrapper.add(scrollPane, BorderLayout.CENTER);
        bgPanel.add(middleWrapper, BorderLayout.CENTER);

        JButton quitBtn = new JButton("Quit");
        quitBtn.setPreferredSize(new Dimension(200, 50));
        quitBtn.addActionListener(e -> System.exit(0));

        String currentVersion = DebugLogger.getAppVersion();

        JLabel versionLabel = new JLabel("v" + currentVersion + " ");
        versionLabel.setForeground(new Color(255, 255, 255, 255));
        versionLabel.setFont(versionLabel.getFont().deriveFont(24f));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));

        JPanel btnWrapper = new JPanel();
        btnWrapper.setOpaque(false);
        btnWrapper.add(quitBtn);

        bottomPanel.add(btnWrapper, BorderLayout.CENTER);
        bottomPanel.add(versionLabel, BorderLayout.EAST);

        bgPanel.add(bottomPanel, BorderLayout.SOUTH);

        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);

        if (!currentVersion.equals("Dev-Build")) {
            AutoUpdate.checkForUpdates(currentVersion);
        } else {
            System.out.println("Running in Dev Mode: Skipping Auto-Update.");
        }
    }
}