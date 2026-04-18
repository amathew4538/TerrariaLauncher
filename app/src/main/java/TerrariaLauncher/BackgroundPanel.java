package TerrariaLauncher;

import java.awt.*;
import java.net.URL;
import javax.swing.*;

class BackgroundPanel extends JPanel {
    private Image backgroundImage;
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isDark;
    private final double scrollSpeed = 0.5;
    private final double scale = 2.0;

    /**
     * Initialize the background of the app
     */
    public BackgroundPanel() {
        setLayout(new BorderLayout());
        updateTheme(); // Initialize with current theme

        Timer timer = new Timer(16, e -> {
            xOffset -= scrollSpeed;
            // Only scroll Y if in dark mode
            if (isDark) {
                yOffset -= scrollSpeed;
            } else {
                yOffset = 0; 
            }

            if (backgroundImage != null) {
                int scaledW = (int) (backgroundImage.getWidth(null) * scale);
                int scaledH = (int) (backgroundImage.getHeight(null) * scale);

                if (Math.abs(xOffset) >= scaledW) xOffset = 0;
                if (Math.abs(yOffset) >= scaledH) yOffset = 0;
            }
            repaint();
        });
        timer.start();
    }

    /**
     * Helper method to refresh the background image and scroll logic
     */
    public void updateTheme() {
        this.isDark = ThemeManager.isDarkMode();
        String resourcePath = isDark ? "/background.png" : "/backgroundLight.png";
        
        URL imgUrl = TerrariaLauncher.class.getResource(resourcePath);
        if (imgUrl != null) {
            this.backgroundImage = new ImageIcon(imgUrl).getImage();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        // Keep pixelated look for Terraria aesthetic
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        super.paintComponent(g);
        
        if (backgroundImage != null) {
            int scaledW = (int) (backgroundImage.getWidth(this) * scale);
            int scaledH = (int) (backgroundImage.getHeight(this) * scale);

            int ix = (int) xOffset;
            int iy = (int) yOffset;

            for (int x = ix - scaledW; x < getWidth(); x += scaledW) {
                for (int y = iy - scaledH; y < getHeight(); y += scaledH) {
                    g.drawImage(backgroundImage, x, y, scaledW, scaledH, this);
                }
            }
        }
    }
}