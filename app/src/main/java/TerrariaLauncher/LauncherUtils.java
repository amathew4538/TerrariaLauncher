package TerrariaLauncher;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LauncherUtils {
    public static void launchInstance(Path path) {
        DebugLogger.initDebugWindow();
        String os = System.getProperty("os.name").toLowerCase();
        File folder = path.toFile();
        DebugLogger.log("Starting launch for: " + folder.getName());

        ModCache.loadInstanceMods(folder);

        if (folder.getName().equalsIgnoreCase("TerrariaLauncher.app") ||
            folder.getName().equalsIgnoreCase("iTerm.app")) {
            DebugLogger.log("Error: Targeting launcher or iTerm. Aborting.");
            return;
        }

        ProcessBuilder pb = new ProcessBuilder();
        pb.directory(folder);

        try {
            if (os.contains("win")) {
                DebugLogger.log("Platform: Windows detected.");
                String cmd = folder.getName().toLowerCase().contains("base") ? "Terraria.exe" : "start-tmodloader.bat";
                pb.command("cmd", "/c", "start", cmd);
                pb.start();
            } else if (os.contains("mac")) {
                DebugLogger.log("Platform: macOS detected.");
                File script = new File(folder, "start-tmodloader.sh");

                if (script.exists()) {
                    DebugLogger.log("Found tModLoader script. Preparing iTerm...");
                    script.setExecutable(true);

                    String rawJarPath = LauncherUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                    String jarPath = URLDecoder.decode(rawJarPath, StandardCharsets.UTF_8.name());

                    File current = new File(jarPath);
                    while (current != null && !current.getName().endsWith(".app")) {
                        current = current.getParentFile();
                    }

                    File parentDir = (current != null) ? current.getParentFile() : new File(".");
                    File iTerm = new File(parentDir, "iTerm.app");

                    String shellCmd = String.format(
                        "cd '%s' && ./start-tmodloader.sh & " +
                        "echo 'Launcher: Waiting for game window...' && " +
                        "sleep 5 && " +
                        "tail -f tModLoader-Logs/client.log | grep -m 1 'Device Created' && " +
                        "echo 'Launcher: Game initialized. Closing terminal...' && sleep 2 && exit",
                        folder.getAbsolutePath());

                    boolean useTerminal = shouldUseTerminal();

                    if (useTerminal) {
                        DebugLogger.log("Config: Using Terminal.app");
                        pb.command("osascript", "-e", "tell application \"Terminal\" to do script \"" + shellCmd + "\"");
                    } else if (iTerm.exists()) {
                        DebugLogger.log("iTerm.app FOUND. Sending Single-Window AppleScript...");
                        String appleScript = String.format(
                            "tell application \"%s\"\n" +
                            "    activate\n" +
                            "    if (count windows) = 0 then\n" +
                            "        create window with default profile\n" +
                            "    end if\n" +
                            "    delay 0.5\n" +
                            "    tell current session of current window\n" +
                            "        write text \"%s\"\n" +
                            "    end tell\n" +
                            "end tell", iTerm.getAbsolutePath(), shellCmd);

                        pb.command("osascript", "-e", appleScript);
                    } else {
                        DebugLogger.log("iTerm.app NOT FOUND. Using Terminal.app fallback.");
                        pb.command("osascript", "-e", "tell application \"Terminal\" to do script \"" + shellCmd + "\"");
                    }

                    pb.start();
                    DebugLogger.log("Process started successfully.");
                } else if (folder.getName().endsWith(".app")) {
                    DebugLogger.log("Base App detected. Using 'open' command.");
                    pb.command("open", "-a", folder.getAbsolutePath());
                    pb.start();
                }
            }

            // WATCHER THREAD: Closes the Java Launcher UI
            new Thread(() -> {
                try {
                    DebugLogger.log("Watcher: Monitoring for game process (max 60s)...");
                    boolean gameStarted = false;
                    for (int i = 0; i < 60; i++) {
                        if (isGameRunning()) {
                            gameStarted = true;
                            DebugLogger.log("Watcher: Game process DETECTED!");
                            break;
                        }
                        Thread.sleep(1000);
                    }

                    if (gameStarted) {
                        DebugLogger.log("Watcher: Game confirmed. iTerm will be closed in 15 seconds...");

                        // Start a separate mini-thread to kill iTerm after 15 seconds
                        new Thread(() -> {
                            try {
                                Thread.sleep(15000); // Wait 15 seconds
                                DebugLogger.log("Watcher: 15s elapsed. Sending quit command to iTerm...");

                                // Use osascript to tell iTerm to quit gracefully
                                String closeScript = "tell application \"iTerm\" to quit";
                                new ProcessBuilder("osascript", "-e", closeScript).start();
                            } catch (Exception e) {
                                DebugLogger.log("Watcher Error (iTerm Close): " + e.getMessage());
                            }
                        }).start();

                        // Continue monitoring for game exit to save mods
                        DebugLogger.log("Watcher: Monitoring for game exit to save mods...");
                        while (isGameRunning()) {
                            Thread.sleep(2000);
                        }

                        DebugLogger.log("Watcher: Game exit detected. Saving Mod Cache...");
                        ModCache.saveInstanceMods(folder);

                        DebugLogger.log("Watcher: Closing Launcher in 3s...");
                        Thread.sleep(3000);
                        System.exit(0);
                    } else {
                        DebugLogger.log("Watcher: Game not detected within 60s. Keeping launcher open.");
                    }
                } catch (Exception e) {
                    DebugLogger.log("Watcher Error: " + e.getMessage());
                }
            }).start();

        } catch (Exception e) {
            DebugLogger.log("CRITICAL ERROR: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Launch Error: " + e.getMessage());
        }
    }

    public static boolean isGameRunning() {
        return ProcessHandle.allProcesses().anyMatch(process -> {
            String info = process.info().command().orElse("").toLowerCase();
            // Filter out the launcher and terminal to avoid false positives
            if (info.contains("terrarialauncher") || info.contains("iterm") || info.contains("terminal")) return false;

            boolean match = info.contains("dotnet") || info.contains("tmodloader") || info.contains("terraria");
            if (match) DebugLogger.log("Matched Process: " + info);
            return match;
        });
    }

    public static void scanAndPopulate(JPanel container, File rootDir) {
        container.removeAll();
        String os = System.getProperty("os.name").toLowerCase();
        String baseName = os.contains("win") ? "Terraria.exe" : "Terraria.app";

        File baseFile = new File(rootDir, baseName);
        if (baseFile.exists()) {
            // Pass 'container' and 'rootDir' so the row can trigger a refresh if needed
            container.add(new InstanceRow("Base Terraria", baseFile.toPath(), true, container, rootDir));
            container.add(Box.createVerticalStrut(10));
        }

        File[] files = rootDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory()) continue;
                String name = file.getName();
                if (name.startsWith(".") || name.equals("app") || name.equals("dist") || name.equals(baseName) 
                    || name.contains("TerrariaLauncher") || name.contains("iTerm")) continue;

                container.add(new InstanceRow(name, file.toPath(), false, container, rootDir));
                container.add(Box.createVerticalStrut(10));
            }
        }
        container.revalidate();
        container.repaint();
    }

    public static String formatFolderName(String name) {
        if (name == null || name.isEmpty()) return name;
        String result = name.replaceAll("([a-z])([A-Z])", "$1 $2");
        return result.substring(0, 1).toUpperCase() + result.substring(1);
    }

    public static void setUIFont(Font f) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (UIManager.get(key) instanceof FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

    public static void checkTerminalCompatibility() {
        File rootDir;
        try {
            String path = TerrariaLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File jarFile = new File(path);
            if (path.contains(".app")) {
                // Traverse out of Contents/Java/ to the main folder
                rootDir = jarFile.getParentFile().getParentFile().getParentFile().getParentFile();
            } else {
                rootDir = new File(".");
            }
        } catch (Exception e) {
            rootDir = new File(".");
        }

        final File finalRoot = rootDir;

        File configFile = new File(finalRoot + "/config.txt");

        // Only run this check if config.txt doesn't exist (first boot)
        if (configFile.exists()) return;
        
        DebugLogger.log("First boot detected. Testing Terminal.app compatibility...");

        boolean terminalSuccess = false;
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            // Try to run a simple AppleScript command to see if Terminal responds
            Callable<Boolean> task = () -> {
                DebugLogger.log("Running ls in Terminal");
                // This script tells Terminal to open, run 'ls', and then returns success if Terminal acknowledged the command.
                String script = "tell application \"Terminal\" to do script \"ls\"";
                ProcessBuilder pb = new ProcessBuilder("osascript", "-e", script);

                Process p = pb.start();
                int exitCode = p.waitFor();
                    
                // If osascript exits with 0, it means Terminal accepted and started the command.
                return exitCode == 0;
            };
            Future<Boolean> future = executor.submit(task);

            try {
                // Wait up to 15 seconds for a response
                terminalSuccess = future.get(15, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                DebugLogger.log("Terminal.app timed out after 15s. Using iTerm fallback.");
                future.cancel(true);
            }
        } catch (Exception e) {
            DebugLogger.log("Error testing Terminal: " + e.getMessage());
        } finally {
            executor.shutdownNow();
        }

        // Create config.txt
        try (PrintWriter writer = new PrintWriter(new FileWriter(configFile))) {
            writer.println("terminalWorks=" + terminalSuccess);
            DebugLogger.log("Config saved: terminalWorks=" + terminalSuccess);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If Terminal works, delete the bundled iTerm.app to save space
        if (terminalSuccess) {
            File iTerm = new File(finalRoot + "/iTerm.app"); // Adjust path if nested
            if (iTerm.exists()) {
                DebugLogger.log("Terminal.app is functional. Deleting iTerm.app at" + iTerm.getAbsolutePath());
                deleteDirectory(iTerm);
            } else {
                DebugLogger.log("Terminal functional, but iTerm.app not found in root: " + finalRoot.getAbsolutePath());
            }
        }
    }

    // Helper for deleting iTerm.app folder
    private static void deleteDirectory(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) deleteDirectory(f);
        }
        file.delete();
    }
    public static boolean shouldUseTerminal() {
        File configFile = new File("config.txt");
        if (!configFile.exists()) return false; // Default to iTerm if check hasn't run

        try {
            String content = Files.readString(configFile.toPath());
            return content.contains("terminalWorks=true");
        } catch (Exception e) {
            return false;
        }
    }
}