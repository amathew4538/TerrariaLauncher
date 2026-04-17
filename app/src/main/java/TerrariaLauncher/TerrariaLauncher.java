package TerrariaLauncher;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class TerrariaLauncher {
    /**
     * Initialize the main app
     */
    public TerrariaLauncher() {
        File finalLocation = LauncherUI.determineRunningLocation();
        DebugLogger.log("Root folder found at " + finalLocation);

        ThemeManager.applyTheme();
        DebugLogger.log("Theme Applied");

        JFrame mainFrame = LauncherUI.createMainFrame();
        DebugLogger.log("Main Frame Created");

        BackgroundPanel bgPanel = new BackgroundPanel("/background.png");
        bgPanel.setLayout(new BorderLayout());
        mainFrame.setContentPane(bgPanel);
        mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        DebugLogger.log("Background panel created");

        JPanel logoPanel = LauncherUI.createLogo();
        bgPanel.add(logoPanel, BorderLayout.NORTH);
        DebugLogger.log("Logo Created");

        JPanel headerPanel = LauncherUI.createHeaderPanel();

        JPanel headerWrapper = LauncherUI.createHeaderWrapper();
        DebugLogger.log("Instance Panel Created");

        JPanel instancePanel = LauncherUI.createInstancePanel(headerWrapper, headerPanel, finalLocation);
        DebugLogger.log("Instance Panel Created");

        LauncherUtils.scanAndPopulate(instancePanel, finalLocation);
        DebugLogger.log("Instances added");

        JPanel middleWrapper = LauncherUI.createMiddleWrapper(instancePanel, headerWrapper);
        bgPanel.add(middleWrapper, BorderLayout.CENTER);
        DebugLogger.log("Middle Wrapper Created");

        JPanel bottomPanel = LauncherUI.createBottomPanel(middleWrapper, finalLocation, instancePanel);
        bgPanel.add(bottomPanel, BorderLayout.SOUTH);
        DebugLogger.log("Bottom Panel Created");

        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);

        AutoUpdate.handleUpdates(DebugLogger.getAppVersion());
    }
    public static void main(String[] args) {
        DebugLogger.initDebugWindow();

        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            TerminalChecker.checkTerminalCompatibility();
        }
    
        SwingUtilities.invokeLater(() -> new TerrariaLauncher());
    }
}