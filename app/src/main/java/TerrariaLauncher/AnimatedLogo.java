package TerrariaLauncher;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.net.URL;

public class AnimatedLogo extends JComponent {
    private Image logoImage;
    private double angle = 0;
    private double scale = 1.0;
    private double time = 0;

    /**
     * Initialize the logo and animate it
     * @param resourcePath path to image
     */
    public AnimatedLogo(String resourcePath) {
        URL imgUrl = AnimatedLogo.class.getResource(resourcePath);
        if (imgUrl != null) {
            this.logoImage = new ImageIcon(imgUrl).getImage();
        }

        Timer timer = new Timer(16, e -> {
            time += 0.001;

            scale = 2 + (Math.sin(time) * 0.5);

            angle = Math.sin(time) * -5;

            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (logoImage == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();
        int imgW = logoImage.getWidth(null);
        int imgH = logoImage.getHeight(null);

        AffineTransform at = new AffineTransform();
        at.translate(w / 2.0, h / 2.0);
        at.rotate(Math.toRadians(angle));
        at.scale(scale, scale);
        at.translate(-imgW / 2.0, -imgH / 2.0);

        g2d.drawImage(logoImage, at, null);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1104, 250);
    }
}