package TerrariaLauncher;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Window;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import com.formdev.flatlaf.FlatLaf;
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
        System.setProperty("flatlaf.animatedLafChange", "true");

        try {
            if (isDarkMode()) {
                FlatMacDarkLaf.setup();
            } else {
                FlatMacLightLaf.setup();
            }
        
            // Handle Font
            URL fontUrl = ThemeManager.class.getResource("/Andy-Bold.ttf");
            if (fontUrl != null) {
                Font andyFont = Font.createFont(Font.TRUETYPE_FONT, fontUrl.openStream());
                setUIFont(andyFont.deriveFont(18f));
            }
        
            FlatLaf.updateUI();
        
            // Border removal
            for (Window window : Window.getWindows()) {
                if (window instanceof JFrame) {
                    Container contentPane = ((JFrame) window).getContentPane();

                    refreshInstanceRows(contentPane);
                    removeScrollPaneBorders(contentPane);
                    refreshHeaderPanel(contentPane);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Removes the scrollpane borders on the instance panel
     * @param container the container to remove the border from
     */
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

    /**
     * Refreshes the instance row to change colors
     * @param container the container to update
     */
    private static void refreshInstanceRows(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof InstanceRow) {
                ((InstanceRow) c).updateContainerTheme();
            } else if (c instanceof Container) {
                refreshInstanceRows((Container) c);
            }
        }
    }

    /**
     * Refreshes the color of the instance label
     * @param container
     */
    private static void refreshHeaderPanel(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JComponent && "headerPanel".equals(((JComponent)c).getClientProperty("id"))) {
                updateComponentTheme((JComponent)c);
            } else if (c instanceof Container) {
                refreshHeaderPanel((Container) c);
            }
        }
    }

    /**
     * Updates the theme of a component to the app theme
     * @param component JComponent to update
     */
    public static void updateComponentTheme(JComponent component) {
        boolean isDark = isDarkMode();

        // Define colors
        String background = isDark ? "rgba(30, 35, 60, 200)" : "rgba(30, 145, 225, 200)";
        Color borderColor = isDark ? new Color(60, 70, 110) : new Color(100, 180, 240);
        Color textColor = isDark ? Color.WHITE : Color.BLACK;
        
        // Apply FlatLaf dynamic style
        component.putClientProperty("FlatLaf.style", "arc: 20; background: " + background);
        
        // Update border
        component.setBorder(BorderFactory.createCompoundBorder(
            new com.formdev.flatlaf.ui.FlatLineBorder(new java.awt.Insets(0,0,0,0), borderColor, 2, 20),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
    
        // Update text for all children
        updateChildColors(component, textColor);
    
        component.repaint();
    }

    /**
     * Updates the colors of the JComponent children
     * @param container container to update
     * @param color the color
     */
    private static void updateChildColors(Container container, Color color) {
        for (Component c : container.getComponents()) {
            if (c instanceof JLabel || (c instanceof JButton && !((JButton)c).isContentAreaFilled())) {
                c.setForeground(color);
            }
            if (c instanceof Container) {
                updateChildColors((Container) c, color);
            }
        }
    }
}