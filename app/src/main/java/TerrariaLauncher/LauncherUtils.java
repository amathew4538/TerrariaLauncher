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

        if (folder.getName().equals("TerrariaLauncher.app") || folder.getName().equals("iTerm.app")) {
            return;
        }
    
        ProcessBuilder pb = new ProcessBuilder();
        pb.directory(folder);
    
        try {
            if (os.contains("win")) {
                String cmd = folder.getName().toLowerCase().contains("base") ? "Terraria.exe" : "start-tmodloader.bat";
                pb.command("cmd", "/c", "start", cmd);
            } else if (os.contains("mac")) {
                File script = new File(folder, "start-tmodloader.sh");
                if (script.exists()) {
                    script.setExecutable(true);
                
                    // Find iTerm.app sitting next to TerrariaLauncher.app
                    String jarPath = LauncherUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                    File bundleDir = new File(jarPath).getParentFile().getParentFile().getParentFile().getParentFile();
                    File iTerm = new File(bundleDir, "iTerm.app");
                
                    if (iTerm.exists()) {
                        // We use osascript to tell iTerm to stay open and run the command
                        String[] launchCmd = {
                            "osascript", "-e",
                            "tell application \"" + iTerm.getAbsolutePath() + "\"\n" +
                            "    activate\n" +
                            "    create window with default profile\n" +
                            "    tell current session of current window\n" +
                            "        write text \"cd \\\"" + folder.getAbsolutePath() + "\\\" && ./start-tmodloader.sh\"\n" +
                            "    end tell\n" +
                            "end tell"
                        };
                        pb.command(launchCmd);
                    } else {
                        // Fallback to native Terminal
                        pb.command("osascript", "-e", "tell application \"Terminal\" to do script \"cd '" + folder.getAbsolutePath() + "' && ./start-tmodloader.sh\"");
                    }
                    pb.start();
                    return;
                }
            }
        
            if (!pb.command().isEmpty()) {
                pb.start();
            }
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
                if (name.contains("TerrariaLauncher")) continue;
                if (name.contains("iTerm")) continue;

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
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream))) {
                String line;
                // readLine() is more reliable for streaming shell outputs than Scanner.hasNextLine()
                while ((line = reader.readLine()) != null) {
                    final String capturedLine = line;
                    SwingUtilities.invokeLater(() -> {
                        textArea.append(capturedLine + "\n");
                        textArea.setCaretPosition(textArea.getDocument().getLength());
                    });
                }
            } catch (java.io.IOException e) {
                SwingUtilities.invokeLater(() -> textArea.append("--- Stream Closed ---\n"));
            }
        }).start();
    }
}
