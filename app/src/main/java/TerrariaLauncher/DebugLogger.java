package TerrariaLauncher;

import java.awt.Color;
import java.awt.Font;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class DebugLogger {
    private static JTextArea debugTextArea;
    private static final boolean IS_DEBUG = checkDebugStatus();

    /**
     * Check if the app is in Debug Mode
     * @return false if release, else true
     */
    public static boolean checkDebugStatus() {
        try (InputStream input = LauncherUtils.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            if (input == null) return true; // Default to debug if file is missing

            prop.load(input);
            String buildType = prop.getProperty("build.type");

            // If the variable hasn't been replaced, it will still look like "${build.type}"
            // only return false if it is explicitly set to "release"
            return !"release".equalsIgnoreCase(buildType);
        } catch (Exception ex) {
            return true;
        }
    }

    /**
     * Print string to log window
     * @param message string to print in log
     * @see {@link DebugLogger#initDebugWindow()}
     */
    public static void log(String message) {
        if (!IS_DEBUG) return;

        System.out.println(message);
        if (debugTextArea != null) {
            SwingUtilities.invokeLater(() -> {
                debugTextArea.append(message + "\n");
                debugTextArea.setCaretPosition(debugTextArea.getDocument().getLength());
            });
        }
    }

    /**
     * Create a log window
     */
    public static void initDebugWindow() {
        if (!IS_DEBUG) return;

        JFrame frame = new JFrame("Launcher Debug Log");
        frame.setSize(500, 400);
        debugTextArea = new JTextArea();
        debugTextArea.setBackground(Color.BLACK);
        debugTextArea.setForeground(Color.GREEN);
        debugTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        debugTextArea.setEditable(false);
        frame.add(new JScrollPane(debugTextArea));
        frame.setVisible(true);
        log("--- Debug Window Initialized ---");
    }

    /**
     * Get the app version from config.properties
     */
    public static String getAppVersion() {
        try (InputStream input = LauncherUtils.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            if (input == null) return "Dev-Build";

            prop.load(input);
            String version = prop.getProperty("version");
        
            // Handle the case where the placeholder ${version} hasn't been replaced yet
            if (version == null || version.contains("${")) return "Dev-Build";

            return version;
        } catch (Exception ex) {
            return "Dev-Build";
        }
    }
}
