package TerrariaLauncher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.net.URL;

public class RefreshButton extends JButton {
    /**
     * Refreshes the instance list
     * @param panelToRefresh the JPanel to refresh
     * @param rootPath the root path that the instances are in
     */
    public RefreshButton(JPanel panelToRefresh, File rootPath) {
        updateIcon();

        putClientProperty("JButton.buttonType", "toolbarButton");
        putClientProperty("FlatLaf.style", "arc: 10; margin: 5,5,5,5");
        
        // Hover effect
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setToolTipText("Refresh Instance List");

        addActionListener(e -> LauncherUtils.scanAndPopulate(panelToRefresh, rootPath));

        // Use MouseListener only to toggle the background fill on hover
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {

            }
            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }
    private void updateIcon() {
        URL refreshUrl = RefreshButton.class.getResource("/refresh.png");
        if (refreshUrl != null) {
            ImageIcon icon = new ImageIcon(refreshUrl);
            Image img = icon.getImage();

            // swap image based on theme
            if (!ThemeManager.isDarkMode()) {
                img = createInvertedImage(img);
            }

            setIcon(new ImageIcon(img.getScaledInstance(25, 25, Image.SCALE_SMOOTH)));
        }
    }

    /**
     * Inverts an image
     * @param image the image to be inverted
     * @return the final inverted image
     */
    private Image createInvertedImage(Image image) {
        int w = image.getWidth(null);
        int h = image.getHeight(null);
        if (w <= 0 || h <= 0) return image;

        BufferedImage buffered = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = buffered.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgba = buffered.getRGB(x, y);
                Color col = new Color(rgba, true);
                // Invert RGB but keep Alpha (transparency)
                col = new Color(255 - col.getRed(), 255 - col.getGreen(), 255 - col.getBlue(), col.getAlpha());
                buffered.setRGB(x, y, col.getRGB());
            }
        }
        return buffered;
    }

    @Override
    public void updateUI() {
        super.updateUI();
        // runs update icon theme changes
        updateIcon();
    }
}