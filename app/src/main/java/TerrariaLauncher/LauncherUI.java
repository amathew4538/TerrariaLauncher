package TerrariaLauncher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.formdev.flatlaf.ui.FlatLineBorder;

/**
 * This class exists to shorten {@link TerrariaLauncher}
 */
public class LauncherUI {
    /**
     * Figures out wherethe instances are
     * @return the path, File
     */
    public static File determineRunningLocation() {
        try {
            String path = TerrariaLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            if (path.contains(".app")) {
                return new File(path).getParentFile().getParentFile().getParentFile().getParentFile();
            }
        } catch (Exception e) {}
        return new File(".");
    }

    /**
     * Create the main frame of the app
     * @return the main frame, JFrame
     * @apiNote Called in {@link TerrariaLauncher#TerrariaLauncher()}
     */
    public static JFrame createMainFrame() {
        String[] messages = {"Now launching Terraria 3: Electric Boogalee",
            "dunno ran out of ideas",
            "Terraria? More like... uhh... Terraria! OOOOOHHHHHH!!!1!1!!1!",
            "Into the Sky!",
            "Huston, the lanterns are coming!",
            "*insert launcher message here*",
            "An epic dev wrote this.",
            "April Fools!",
            "Also check out Prism Launcher!",
            "Woopdedoo!",
            "Hey Michael, Vsauce here. What if Terraria was easy?",
            "Terraria exists... or does it?"
        };

        return new JFrame("Terraria Launcher: " + messages[(int)(Math.random() * messages.length)]);
    }

    /**
     * Creates the animated logo
     * @apiNote Called in {@link TerrariaLauncher#TerrariaLauncher()}
     * @return the animated logo, JPanel
     */
    public static JPanel createLogo() {
        AnimatedLogo logo = new AnimatedLogo("/TerrariaLauncherLogo.png");

        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setOpaque(false);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        logoPanel.add(logo, BorderLayout.CENTER);

        return logoPanel;
    }

    /**
     * Create the header items panel
     * @return the header panel, JPanel
     * @apiNote Called in {@link TerrariaLauncher#TerrariaLauncher()}
     */
    public static JPanel createHeaderPanel() {
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

        return headerPanel;
    }

    /**
     * Creates the wrapper for the instances and header panels
     * @return the header wrapper, JPanel
     * @apiNote Called in {@link TerrariaLauncher#TerrariaLauncher()}
     */
    public static JPanel createHeaderWrapper() {
        JPanel headerWrapper = new JPanel();
        headerWrapper.setOpaque(false);

        return headerWrapper;
    }

    /**
     * Creates the panel with all of the instances and the refresh button
     * @param headerWrapper the header wrapper, JPanel
     * @param headerPanel the header panel, JPanel
     * @param finalLocation the root folder, File
     * @return the instance panel, JPanel
     * @apiNote Called in {@link TerrariaLauncher#TerrariaLauncher()}
     */
    public static JPanel createInstancePanel(JPanel headerWrapper, JPanel headerPanel, File finalLocation) {
        JPanel instancePanel = new JPanel();
        instancePanel.setLayout(new BoxLayout(instancePanel, BoxLayout.Y_AXIS));
        instancePanel.setOpaque(false);
        instancePanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 50, 50));

        RefreshButton refreshBtn = new RefreshButton(instancePanel, finalLocation);
        headerPanel.add(refreshBtn, BorderLayout.EAST);

        headerPanel.add(Box.createHorizontalStrut(40), BorderLayout.WEST);
        headerWrapper.add(headerPanel);

        return instancePanel;
    }

    /**
     * Create the middle wrapper
     * @param instancePanel the instance panel, JPanel
     * @param headerWrapper the header wrapper, JPanel
     * @return the middle wrapper, JPanel
     * @apiNote Called in {@link TerrariaLauncher#TerrariaLauncher()}
     */
    public static JPanel createMiddleWrapper(JPanel instancePanel, JPanel headerWrapper) {
        JScrollPane scrollPane = new JScrollPane(instancePanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        JPanel middleWrapper = new JPanel(new BorderLayout());
        middleWrapper.setOpaque(false);
        middleWrapper.add(headerWrapper, BorderLayout.NORTH);
        middleWrapper.add(scrollPane, BorderLayout.CENTER);

        return middleWrapper;
    }

    /**
     * Create the bottom panel <br> <br>
     * Creates the create instance button, quit button, and version text inside of it
     * @param mainFrame the main frame of the app, JPanel
     * @param finalLocation the root folder, File
     * @param instancePanel the instance panel, JPanel
     * @return the entire bottom panel, JPanel
     * @apiNote Called in {@link TerrariaLauncher#TerrariaLauncher()}
     */
    public static JPanel createBottomPanel(JPanel mainFrame, File finalLocation, JPanel instancePanel) {
        JButton createBtn = new JButton("Create Instance");
        createBtn.setPreferredSize(new Dimension(200, 50));
        createBtn.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(mainFrame, "Enter Instance Name:");
            if (name != null && !name.trim().isEmpty()) {
                JFileChooser iconChooser = new JFileChooser();

                FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (*.png)", "png");

                iconChooser.addChoosableFileFilter(filter);
                iconChooser.setFileFilter(filter);

                File icon = null;

                int result = iconChooser.showOpenDialog(mainFrame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    icon = iconChooser.getSelectedFile();
                }

                EditInstance.createInstance(name, icon, finalLocation, instancePanel);
            }
        });

        String creditsURL = "https://github.com/amathew4538/TerrariaLauncher/blob/main/CREDITS.md";
        JButton creditsBtn = new JButton("Credits");
        creditsBtn.setPreferredSize(new Dimension(200, 50));
        creditsBtn.addActionListener(e -> {
            LauncherUtils.openWebpage(creditsURL);
        });

        JButton quitBtn = new JButton("Quit");
        quitBtn.setPreferredSize(new Dimension(200, 50));
        quitBtn.addActionListener(e -> System.exit(0));

        String currentVersion = DebugLogger.getAppVersion();
        JLabel versionLabel = new JLabel("v" + currentVersion + " ");
        versionLabel.setForeground(new Color(255, 255, 255, 255));
        versionLabel.setFont(versionLabel.getFont().deriveFont(24f));

        JButton themeSwitcherBtn = new JButton("Switch theme");
        quitBtn.setPreferredSize(new Dimension(200, 50));
        themeSwitcherBtn.addActionListener(e -> ThemeManager.themeSwitcher());

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JPanel btnWrapper = new JPanel();

        btnWrapper.setOpaque(false);
        btnWrapper.add(createBtn);
        btnWrapper.add(Box.createHorizontalStrut(20));

        btnWrapper.add(creditsBtn);
        btnWrapper.add(Box.createHorizontalStrut(20));

        btnWrapper.add(quitBtn);
        bottomPanel.add(themeSwitcherBtn, BorderLayout.WEST);
        bottomPanel.add(btnWrapper, BorderLayout.CENTER);
        bottomPanel.add(versionLabel, BorderLayout.EAST);

        return bottomPanel;
    }
}
