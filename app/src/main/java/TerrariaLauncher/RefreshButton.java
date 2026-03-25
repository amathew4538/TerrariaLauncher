package TerrariaLauncher;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;

public class RefreshButton extends JButton {
    
    public RefreshButton(JPanel panelToRefresh, File rootPath) {
        URL refreshUrl = getClass().getResource("/refresh.png");
        if (refreshUrl != null) {
            setIcon(new ImageIcon(new ImageIcon(refreshUrl).getImage()
                .getScaledInstance(25, 25, Image.SCALE_SMOOTH)));
        } else {
            setText("Refresh");
        }

        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setToolTipText("Refresh Instance List");

        addActionListener(e -> LauncherUtils.scanAndPopulate(panelToRefresh, rootPath));

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
              
            }
        });
    }
}