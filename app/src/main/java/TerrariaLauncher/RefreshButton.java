package TerrariaLauncher;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;

public class RefreshButton extends JButton {
    /**
     * Refreshes the instance list
     * @param panelToRefresh the JPanel to refresh
     * @param rootPath the root path that the instances are in
     */
    public RefreshButton(JPanel panelToRefresh, File rootPath) {
        URL refreshUrl = RefreshButton.class.getResource("/refresh.png");
        if (refreshUrl != null) {
            setIcon(new ImageIcon(new ImageIcon(refreshUrl).getImage()
                .getScaledInstance(25, 25, Image.SCALE_SMOOTH)));
        } else {
            setText("Refresh");
        }

        putClientProperty("JButton.buttonType", "toolbarButton");
        putClientProperty("FlatLaf.style", "arc: 10; margin: 5,5,5,5");
        
        // Hover effect
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setToolTipText("Refresh Instance List");

        addActionListener(e -> LauncherUtils.scanAndPopulate(panelToRefresh, rootPath));

        // Use MouseListener only to toggle the background fill on hover
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {

            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {

            }
        });
    }
}