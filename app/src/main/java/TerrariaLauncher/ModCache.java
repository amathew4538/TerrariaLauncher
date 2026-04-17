package TerrariaLauncher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import com.sun.jna.platform.win32.Shell32Util;
import com.sun.jna.platform.win32.ShlObj;

public class ModCache {
    /**
     * Swaps the enabled.json from the Instance into the Global folder.
     * @apiNote Call this RIGHT BEFORE launching the game.
     */
    public static void loadInstanceMods(File instanceDir) {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("mac")) {
            final String macGlobalPath = System.getProperty("user.home")
                + "/Library/Application Support/Terraria/tModLoader-preview/Mods/enabled.json";
            File globalFile = new File(macGlobalPath);
            File instanceCache = new File(instanceDir, "enabled.json");

            try {
                if (instanceCache.exists()) {
                    // Ensure the directory exists
                    globalFile.getParentFile().mkdirs();

                    // Overwrite the global enabled.json with the instance's specific one
                    Files.copy(instanceCache.toPath(), globalFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    DebugLogger.log("ModCache: Loaded enabled.json for " + instanceDir.getName());
                } else {
                    DebugLogger.log("ModCache: No enabled.json found in instance. Using global default.");
                }
            } catch (IOException e) {
                System.err.println("ModCache Error (Load): " + e.getMessage());
            }
        } else if (os.contains("win")) {
                final String windowsDocumentsPath = Shell32Util.getFolderPath(ShlObj.CSIDL_PERSONAL);
                final String windowsGlobalPath = windowsDocumentsPath + "\\My Games\\Terraria\\tModLoader-preview\\Mods\\enabled.json";
            File globalFile = new File(windowsGlobalPath);
            File instanceCache = new File(instanceDir, "enabled.json");

            try {
                if (instanceCache.exists()) {
                    // Ensure the directory exists
                    globalFile.getParentFile().mkdirs();

                    // Overwrite the global enabled.json with the instance's specific one
                    Files.copy(instanceCache.toPath(), globalFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    DebugLogger.log("ModCache: Loaded enabled.json for " + instanceDir.getName());
                } else {
                    DebugLogger.log("ModCache: No enabled.json found in instance. Using global default.");
                }
            } catch (IOException e) {
                System.err.println("ModCache Error (Load): " + e.getMessage());
            }
        }
    }

    /**
     * Saves the current Global enabled.json back into the Instance folder.
     * @apiNote Call this AFTER the game closes or when a Save trigger happens.
     */
    public static void saveInstanceMods(File instanceDir) {
        String os = System.getProperty("os.name").toLowerCase();
        String path;
        // Select the correct path based on OS
        if (os.contains("mac")) {
            path = System.getProperty("user.home")
                + "/Library/Application Support/Terraria/tModLoader-preview/Mods/enabled.json";
        } else {
            final String windowsDocumentsPath = Shell32Util.getFolderPath(ShlObj.CSIDL_PERSONAL);
            path = windowsDocumentsPath + "\\My Games\\Terraria\\tModLoader-preview\\Mods\\enabled.json";
        }

        File globalFile = new File(path);
        File instanceCache = new File(instanceDir, "enabled.json");

        try {
            if (globalFile.exists()) {
                // Ensure the instance directory exists before copying into it
                if (!instanceDir.exists()) {
                    DebugLogger.log("Instance Directory not found");
                    throw new IOException("No Instance Directory Found!");
                }

                Files.copy(globalFile.toPath(), instanceCache.toPath(), StandardCopyOption.REPLACE_EXISTING);
                DebugLogger.log("ModCache: Saved enabled.json for " + instanceDir.getName());
            } else {
                DebugLogger.log("ModCache: Global enabled.json not found at " + path);
            }
        } catch (IOException e) {
            System.err.println("ModCache Error (Save): " + e.getMessage());
        }
    }
}