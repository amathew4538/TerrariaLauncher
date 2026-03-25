package TerrariaLauncher;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Enumeration;

public class LauncherUtils {

    public static void setUIFont(Font f) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (UIManager.get(key) instanceof FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

    public static String formatFolderName(String name) {
        if (name == null || name.isEmpty()) return name;
        String result = name.replaceAll("([a-z])([A-Z])", "$1 $2");
        return result.substring(0, 1).toUpperCase() + result.substring(1);
    }
    public static void launchInstance(Path path) {
        String os = System.getProperty("os.name").toLowerCase();
        File folder = path.toFile();
        ProcessBuilder pb = new ProcessBuilder();
        pb.directory(folder);

        try {
            if (os.contains("win")) {
                String cmd = folder.getName().toLowerCase().contains("base") ? "Terraria.exe" : "start-tmodloader.bat";
                pb.command("cmd", "/c", "start", cmd);
            } else {
                // macOS Logic
                if (folder.getName().endsWith(".app")) {
                    pb.command("open", "-a", folder.getAbsolutePath());
                } else {
                    File script = new File(folder, "start-tmodloader.sh");
                    if (script.exists()) {
                        script.setExecutable(true);
                        pb.command("zsh", "-c", "./start-tModLoader.sh");
                        pb.redirectErrorStream(true);
                        Process process = pb.start();
                        showLogWindow(process.getInputStream());
                        return;
                    }
                }
            }
        
            // Ensure we actually have a command before starting
            if (pb.command().isEmpty()) {
                throw new Exception("No launch command generated for: " + folder.getName());
            }
        
            pb.start();
        
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Launch Error: " + e.getMessage());
        }
    }

    public static void scanAndPopulate(JPanel container, File rootDir) {
        container.removeAll();

        String os = System.getProperty("os.name").toLowerCase();
        String baseName = os.contains("win") ? "Terraria.exe" : "Terraria.app";
    
        File baseFile = new File(rootDir, baseName);
        if (baseFile.exists()) {
            container.add(new InstanceRow("Base Terraria", baseFile.toPath(), true));
            container.add(Box.createVerticalStrut(10));
        }

        File[] files = rootDir.listFiles();
        if (files != null) {
            for (File file : files) {
                String name = file.getName();

                if (!file.isDirectory()) continue;
                if (name.startsWith(".") || name.equals("app") || name.equals("dist")) continue;
                if (name.equals(baseName)) continue;
                if (name.contains("Terraria Launcher")) continue;

                container.add(new InstanceRow(name, file.toPath(), false));
                container.add(Box.createVerticalStrut(10));
            }
        }
        container.revalidate();
        container.repaint();
    }
    public static void showLogWindow(InputStream inputStream) {
        JFrame logFrame = new JFrame("TModLoader Console Log");
        logFrame.setSize(600, 400);
        JTextArea textArea = new JTextArea();
        textArea.setBackground(new Color(30, 30, 30));
        textArea.setForeground(Color.GREEN);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);

        logFrame.add(new JScrollPane(textArea));
        logFrame.setVisible(true);

        new Thread(() -> {
            try (java.util.Scanner scanner = new java.util.Scanner(inputStream)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    SwingUtilities.invokeLater(() -> {
                        textArea.append(line + "\n");
                        textArea.setCaretPosition(textArea.getDocument().getLength());
                    });
                }
            }
        }).start();
    }
}
