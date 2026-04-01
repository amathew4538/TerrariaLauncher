package TerrariaLauncher;

import java.awt.*;
import java.net.URL;
import javax.swing.*;

class BackgroundPanel extends JPanel {
    private Image backgroundImage;
    private double xOffset = 0;
    private double yOffset = 0;
    
    // ADJUST THESE TWO VALUES
    private final double scrollSpeed = 0.5; 
    private final double scale = 2.0; // 2.0 = 200% size, 1.5 = 150%, etc.

    public BackgroundPanel(String resourcePath) {
        URL imgUrl = TerrariaLauncher.class.getResource(resourcePath);
        if (imgUrl != null) {
            this.backgroundImage = new ImageIcon(imgUrl).getImage();
        }
        setLayout(new BorderLayout());

        Timer timer = new Timer(16, e -> {
            xOffset -= scrollSpeed;
            yOffset -= scrollSpeed;

            if (backgroundImage != null) {
                // Calculate reset based on the SCALED size, not normal size
                int scaledW = (int) (backgroundImage.getWidth(null) * scale);
                int scaledH = (int) (backgroundImage.getHeight(null) * scale);

                if (Math.abs(xOffset) >= scaledW) xOffset = 0;
                if (Math.abs(yOffset) >= scaledH) yOffset = 0;
            }
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        super.paintComponent(g);
        if (backgroundImage != null) {
            // Calculate the new width and height based on scale
            int scaledW = (int) (backgroundImage.getWidth(this) * scale);
            int scaledH = (int) (backgroundImage.getHeight(this) * scale);

            int ix = (int) xOffset;
            int iy = (int) yOffset;

            // Tile the scaled image
            for (int x = ix - scaledW; x < getWidth(); x += scaledW) {
                for (int y = iy - scaledH; y < getHeight(); y += scaledH) {
                    // Draw with the scaled width and height
                    g.drawImage(backgroundImage, x, y, scaledW, scaledH, this);
                }
            }
        }
    }
}