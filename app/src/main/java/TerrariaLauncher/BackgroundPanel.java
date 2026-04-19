package TerrariaLauncher;

import java.awt.*;
import java.net.URL;
import javax.swing.*;

class BackgroundPanel extends JPanel {
    private Image currentImage;
    private Image oldImage;
    private float alpha = 1.0f;
    private Timer fadeTimer;
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isDark;
    private double targetWidth;
    private final double scrollSpeed = 0.5;



    public BackgroundPanel() {
        setLayout(new BorderLayout());
        updateTheme(); 

        // Main animation timer for scrolling
        Timer scrollTimer = new Timer(16, e -> {
            xOffset -= scrollSpeed;
            yOffset = isDark ? yOffset - scrollSpeed : 0;

            if (currentImage != null) {
                double currentScale = getDynamicScale(currentImage);
                int scaledW = (int) (currentImage.getWidth(null) * currentScale);
                int scaledH = (int) (currentImage.getHeight(null) * currentScale);
                if (Math.abs(xOffset) >= scaledW) xOffset = 0;
                if (Math.abs(yOffset) >= scaledH) yOffset = 0;
            }
            repaint();
        });
        scrollTimer.start();
    }

    /**
     * Updates the background based on theme
     */
    public void updateTheme() {
        this.isDark = ThemeManager.isDarkMode();
        String resourcePath = isDark ? "/darkModeBackground.png" : "/lightModeBackground.png";
        
        URL imgUrl = TerrariaLauncher.class.getResource(resourcePath);
        if (imgUrl != null) {
            // Set the current image as the "old" one for the transition
            if (this.currentImage != null) {
                this.oldImage = this.currentImage;
                this.alpha = 0.0f; // Start new image at transparent
                startFade();
            }
            this.currentImage = new ImageIcon(imgUrl).getImage();
        }
        repaint();
    }

    /**
     * Fades the background
     */
    private void startFade() {
        if (fadeTimer != null && fadeTimer.isRunning()) fadeTimer.stop();

        fadeTimer = new Timer(20, e -> {
            alpha += 0.05f; // Adjust this value to change fade speed
            if (alpha >= 1.0f) {
                alpha = 1.0f;
                oldImage = null; // Fade finished, clear memory
                ((Timer)e.getSource()).stop();
            }
            repaint();
        });
        fadeTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        if (oldImage != null) {
            double oldScale = getDynamicScale(oldImage); // Calculate specific scale for old image
            drawTiledImage(g2d, oldImage, 1.0f, oldScale);
        }
    
        if (currentImage != null) {
            double currentScale = getDynamicScale(currentImage); // Calculate specific scale for new image
            drawTiledImage(g2d, currentImage, alpha, currentScale);
        }
    }

    /**
     * Tiles the image one after the other
     * @param g2d a graphics2d
     * @param img the image to tile
     * @param opacity the opacity, use something like {@code 1.0f}
     * @param dynamicScale the scale of the image
     */
    private void drawTiledImage(Graphics2D g2d, Image img, float opacity, double dynamicScale) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

        int scaledW = (int) (img.getWidth(this) * dynamicScale);
        int scaledH = (int) (img.getHeight(this) * dynamicScale);
        int ix = (int) xOffset;
        int iy = (int) yOffset;

        for (int x = ix - scaledW; x < getWidth(); x += scaledW) {
            for (int y = iy - scaledH; y < getHeight(); y += scaledH) {
                g2d.drawImage(img, x, y, scaledW, scaledH, this);
            }
        }
        // Reset composite so other UI elements aren't transparent
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    /**
     * Get scale of image needed to fill the screen
     * @param img input image
     * @return the scale needed
     */
    private double getDynamicScale(Image img) {
        if (img == null) return 1.0;

        if (ThemeManager.isDarkMode()){
            targetWidth = 2000.0;
        } else {
            targetWidth = 3000.0;
        }
        return targetWidth / img.getWidth(null);
    }
}