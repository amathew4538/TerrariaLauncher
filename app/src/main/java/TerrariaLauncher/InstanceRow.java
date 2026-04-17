package TerrariaLauncher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.nio.file.Path;
import com.formdev.flatlaf.ui.FlatLineBorder;

public class InstanceRow extends JPanel {
    private JPanel parentContainer;
    private java.io.File rootDir;

    /**
     * Adds an instance to the row of instances
     * @param folderName name of the instance folder
     * @param folderPath path to the instance folder
     * @param isBase whether it is base Terraria or not
     * @param container the JPanel to add the instance to0
     * @param root the root directory (folder the app is in)
     * @apiNote This gets called in {@link LauncherUtils#scanAndPopulate(JPanel, java.io.File) LauncherUtils.scanAndPopulate()}
     */
    public InstanceRow(String folderName, Path folderPath, boolean isBase, JPanel container, java.io.File root) {
        this.parentContainer = container;
        this.rootDir = root;

        setLayout(new BorderLayout(20, 0));
        setMaximumSize(new Dimension(800, 100));
        setOpaque(false);

        // Styling
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

        // Text (title remains in the center)
        JLabel titleLabel = new JLabel(isBase ? "Base Terraria" : LauncherUtils.formatFolderName(folderName));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(titleLabel.getFont().deriveFont(24f));
        add(titleLabel, BorderLayout.CENTER);

        // FlowLayout.CENTER ensures the button and dots stay vertically aligned
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        actionPanel.setOpaque(false);

        // Launch Button
        JButton launchBtn = new JButton("Launch");
        launchBtn.setPreferredSize(new Dimension(120, 40));
        launchBtn.addActionListener(e -> LauncherUtils.launchInstance(folderPath));
        actionPanel.add(launchBtn);

        // Three Dots Menu Button
        JButton menuBtn = new JButton("\u22ee");
        menuBtn.setFocusPainted(false);
        menuBtn.setBorderPainted(false);
        menuBtn.setContentAreaFilled(false);
        menuBtn.setForeground(Color.WHITE);
        menuBtn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24)); // Slightly larger font for the dots
        menuBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (isBase) {
            // Invisible but occupies space to keep Launch position consistent
            menuBtn.setVisible(false);
        }

        // Popup Menu logic
        JPopupMenu popup = new JPopupMenu();

        JMenuItem statsItem = new JMenuItem("Statistics");
        statsItem.addActionListener(e -> {
            // Get stats for this specific folder
            String stats = StatsManager.getStatsString(folderPath.toFile());

            JOptionPane.showMessageDialog(this,
                stats,
                "Statistics: " + folderName,
                JOptionPane.INFORMATION_MESSAGE);
        });
        popup.add(statsItem);

        JMenuItem editModsItem = new JMenuItem("Edit Mods");
        editModsItem.addActionListener(e -> {
            // Open the Mod Editor window for this specific folder
            EditInstance.editMods(folderPath);
        });
        popup.add(editModsItem);

        JMenuItem deleteItem = new JMenuItem("Delete Instance");
        deleteItem.setForeground(Color.RED);
        deleteItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to delete '" + folderName + "'?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                EditInstance.deleteInstance(folderPath);
                LauncherUtils.scanAndPopulate(parentContainer, rootDir);
            }
        });
        popup.add(deleteItem);
        menuBtn.addActionListener(e -> popup.show(menuBtn, 0, menuBtn.getHeight()));

        actionPanel.add(menuBtn);
        add(actionPanel, BorderLayout.EAST);

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