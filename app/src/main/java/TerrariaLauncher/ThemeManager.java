package TerrariaLauncher;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.net.URL;
import java.util.Enumeration;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;

public class ThemeManager {
    /**
     * Apply the theme to the app
     */
    public static void applyTheme() {
        try {
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
            URL fontUrl = ThemeManager.class.getResource("/Andy-Bold.ttf");
            if (fontUrl != null) {
                Font andyFont = Font.createFont(Font.TRUETYPE_FONT, fontUrl.openStream());
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(andyFont);
                setUIFont(andyFont.deriveFont(18f));
            }
        } catch (Exception ex) { ex.printStackTrace(); }
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
