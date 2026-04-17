package TerrariaLauncher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
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
        final File configFile = LauncherUtils.getConfigFile();

        // Check if config.txt doesn't exist
        if (configFile.exists()) return;

        final File finalRoot = configFile.getParentFile();

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
        try {
            List<String> lines = configFile.exists() ? Files.readAllLines(configFile.toPath()) : new ArrayList<>();
            boolean found = false;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith("terminalWorks=")) {
                    lines.set(i, "terminalWorks=" + terminalSuccess);
                    found = true;
                    break;
                }
            }
            if (!found) lines.add("terminalWorks=" + terminalSuccess);
            Files.write(configFile.toPath(), lines);
        } catch (IOException e) {
            DebugLogger.log("Error writing to config.txt: " + e.getMessage());
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
