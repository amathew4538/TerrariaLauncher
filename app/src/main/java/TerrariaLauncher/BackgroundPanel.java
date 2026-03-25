package TerrariaLauncher;

import java.awt.BorderLayout;
import java.net.URL;
import javax.swing.JPanel;
import java.awt.Image;
import java.awt.Graphics;
import javax.swing.ImageIcon;

class BackgroundPanel extends JPanel {
    private Image backgroundImage;

     public BackgroundPanel(String resourcePath) {
        URL imgUrl = TerrariaLauncher.class.getResource(resourcePath);
        if (imgUrl != null) {
            this.backgroundImage = new ImageIcon(imgUrl).getImage();
        }
        setLayout(new BorderLayout());
    }

     @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
