package TerrariaLauncher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.nio.file.Path;
import com.formdev.flatlaf.ui.FlatLineBorder;

public class InstanceRow extends JPanel {
    
    public InstanceRow(String folderName, Path folderPath, boolean isBase) {
        setLayout(new BorderLayout(20, 0));
        setMaximumSize(new Dimension(800, 100));
        setOpaque(false);
        
        // Rounded Styling
        putClientProperty("FlatLaf.style", "arc: 20; background: rgba(30, 35, 60, 200)");
        setBorder(BorderFactory.createCompoundBorder(
            new FlatLineBorder(new Insets(0,0,0,0), new Color(60, 70, 110), 2, 20),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Icon Logic
        ImageIcon displayIcon = null;
        if (isBase) {
            URL baseIconUrl = getClass().getResource("/Terraria.png");
            if (baseIconUrl != null) displayIcon = new ImageIcon(baseIconUrl);
        } else {
            java.io.File localIcon = new java.io.File(folderPath.toFile(), "icon.png");
            if (localIcon.exists()) displayIcon = new ImageIcon(localIcon.getAbsolutePath());
        }
    
        if (displayIcon == null || displayIcon.getImage() == null) {
            URL fallback = getClass().getResource("/Terraria.png");
            if(fallback != null) displayIcon = new ImageIcon(fallback);
        }

        if (displayIcon != null) {
            Image img = displayIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            add(new JLabel(new ImageIcon(img)), BorderLayout.WEST);
        }
    
        // Text
        JLabel titleLabel = new JLabel(isBase ? "Base Terraria" : LauncherUtils.formatFolderName(folderName));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(titleLabel.getFont().deriveFont(24f));
        add(titleLabel, BorderLayout.CENTER);
    
        // Button
        JButton launchBtn = new JButton("Launch");
        launchBtn.setPreferredSize(new Dimension(120, 40));
        launchBtn.addActionListener(e -> LauncherUtils.launchInstance(folderPath));
        add(launchBtn, BorderLayout.EAST);
    
        // Hover Logic
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                putClientProperty("FlatLaf.style", "arc: 20; background: rgba(50, 60, 100, 255)");
                repaint();
            }
            public void mouseExited(MouseEvent e) { 
                putClientProperty("FlatLaf.style", "arc: 20; background: rgba(30, 35, 60, 200)");
                repaint();
            }
        });
    }
}