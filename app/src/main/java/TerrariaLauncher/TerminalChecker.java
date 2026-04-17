package TerrariaLauncher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TerminalChecker {
    
    /**
     * At boot, checks if Terminal works
     * @apiNote Only runs if onfig.txt doesnt exist
     */
    public static void checkTerminalCompatibility() {
        File rootDir;
        try {
            String path = TerrariaLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File jarFile = new File(path);
            if (path.contains(".app")) {
                // Move out of Contents/Java/ to the main folder
                rootDir = jarFile.getParentFile().getParentFile().getParentFile().getParentFile();
            } else {
                rootDir = new File(".");
            }
        } catch (Exception e) {
            rootDir = new File(".");
        }

        final File finalRoot = rootDir;

        File configFile = new File(finalRoot + "/config.txt");

        // Only run this check if config.txt doesn't exist
        if (configFile.exists()) return;

        DebugLogger.log("First boot detected. Testing Terminal.app compatibility...");

        boolean terminalSuccess = false;
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            // Run a simple AppleScript command to see if Terminal responds
            Callable<Boolean> task = () -> {
                DebugLogger.log("Running ls in Terminal");
                // Tell Terminal to open, run 'ls', and then returns success if Terminal does
                String script = "tell application \"Terminal\" to do script \"ls\"";
                ProcessBuilder pb = new ProcessBuilder("/usr/bin/osascript", "-e", script);

                Process p = pb.start();
                int exitCode = p.waitFor();

                // If /usr/bin/osascript exits with 0, it means Terminal ran the command.
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

        // If Terminal works, delete iTerm.app to save space
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

    /**
     * Quickly deletes files
     * @param file a directory/file to delete
     */
    private static void deleteDirectory(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) deleteDirectory(f);
        }
        file.delete();
    }

    /**
     * Checks config.txt to see if terminal should be used
     * @return true if terminalWorks is true, else false
     */
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
