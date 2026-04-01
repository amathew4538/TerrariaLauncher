package TerrariaLauncher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ModCache {

    // The global path where tModLoader actually looks
    private static final String TMOD_GLOBAL_PATH = System.getProperty("user.home")
        + "/Library/Application Support/Terraria/tModLoader-preview/Mods/enabled.json";

    /**
     * Swaps the enabled.json from the Instance into the Global folder.
     * @apiNote Call this RIGHT BEFORE launching the game.
     */
    public static void loadInstanceMods(File instanceDir) {
        File globalFile = new File(TMOD_GLOBAL_PATH);
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

    /**
     * Saves the current Global enabled.json back into the Instance folder.
     * @apiNote Call this AFTER the game closes or when a Save trigger happens.
     */
    public static void saveInstanceMods(File instanceDir) {
        File globalFile = new File(TMOD_GLOBAL_PATH);
        File instanceCache = new File(instanceDir, "enabled.json");

        try {
            if (globalFile.exists()) {
                Files.copy(globalFile.toPath(), instanceCache.toPath(), StandardCopyOption.REPLACE_EXISTING);
                DebugLogger.log("ModCache: Saved enabled.json for " + instanceDir.getName());
            }
        } catch (IOException e) {
            System.err.println("ModCache Error (Save): " + e.getMessage());
        }
    }
}