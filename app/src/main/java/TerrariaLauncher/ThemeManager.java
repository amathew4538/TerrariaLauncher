package TerrariaLauncher;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

public class ThemeManager {
    private static final File configFile = LauncherUtils.getConfigFile();
    /**
     * Swaps the theme in the config and reapplies it
     */
    public static void themeSwitcher() {
        File configFile = LauncherUtils.getConfigFile();
        boolean currentIsDark = isDarkMode();
        String newTheme = currentIsDark ? "macLight" : "macDark";

        try {
            List<String> lines = configFile.exists() ? Files.readAllLines(configFile.toPath()) : new ArrayList<>();
            boolean found = false;
            
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith("theme=")) {
                    lines.set(i, "theme=" + newTheme);
                    found = true;
                    break;
                }
            }
            if (!found) lines.add("theme=" + newTheme);

            Files.write(configFile.toPath(), lines);
            
            // Re-apply and refresh UI
            applyTheme();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the app is in dark mode
     * @return if the file contains the theme
     */
    public static boolean isDarkMode() {
        try {
            if (!configFile.exists()) return true; // Default to Dark
            String content = Files.readString(configFile.toPath());
            return !content.contains("theme=macLight");
        } catch (Exception e) {
            DebugLogger.log("Dark mode check failed: " + e.getMessage());
            return true;
        }
    }

    /**
     * Applies the theme to the app
     */
    public static void applyTheme() {
        try {
            if (isDarkMode()) {
                UIManager.setLookAndFeel(new FlatMacDarkLaf());
            } else {
                UIManager.setLookAndFeel(new FlatMacLightLaf());
            }

            URL fontUrl = ThemeManager.class.getResource("/Andy-Bold.ttf");
            if (fontUrl != null) {
                Font andyFont = Font.createFont(Font.TRUETYPE_FONT, fontUrl.openStream());
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(andyFont);
                setUIFont(andyFont.deriveFont(18f));
            }

            // This updates all open windows instantly
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);

                // After updating the UI, find the JScrollPane and kill its border again
                if (window instanceof JFrame) {
                    removeScrollPaneBorders((JFrame) window);
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private static void removeScrollPaneBorders(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JScrollPane) {
                ((JScrollPane) c).setBorder(BorderFactory.createEmptyBorder());
            } else if (c instanceof Container) {
                removeScrollPaneBorders((Container) c);
            }
        }
    }

    /**
     * Sets the font of the app
     * @param font a font file
     */
    public static void setUIFont(Font font) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (UIManager.get(key) instanceof FontUIResource) {
                UIManager.put(key, font);
            }
        }
    }
}