package TerrariaLauncher;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class StatsManager {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    /**
     * Increases the launch count by 1 for the specific instance and logs last played date.
     * @param instanceFolder The folder of the mod instance being launched.
     */
    public static void incrementLaunchCount(File instanceFolder) {
        Properties prop = loadStats(instanceFolder);
        int count = 0;

        try {
            count = Integer.parseInt(prop.getProperty("launches", "0"));
        } catch (NumberFormatException e) {
            DebugLogger.log("Invalid integer increment: " + e.getMessage());
        }

        prop.setProperty("launches", String.valueOf(count + 1));

        String now = LocalDateTime.now().format(dateFormatter);
        prop.setProperty("lastPlayed", now);

        saveStats(instanceFolder, prop);
    }

    /**
     * Adds minutes to the total playtime of the specific instance.
     * @param instanceFolder The folder of the mod instance.
     * @param minutes The duration of the play session in minutes.
     */
    public static void addPlayTime(File instanceFolder, long minutes) {
        Properties prop = loadStats(instanceFolder);
        long total = 0;

        try {
            total = Long.parseLong(prop.getProperty("playtime", "0"));
        } catch (NumberFormatException e) {
            DebugLogger.log("Invalid playtime value input: " + e.getMessage());
        }

        prop.setProperty("playtime", String.valueOf(total + minutes));
        saveStats(instanceFolder, prop);
    }

    /**
     * Formats the statistics into a readable string for the JPopupMenu.
     * @param instanceFolder The folder of the mod instance.
     * @return A formatted string with hours, minutes, and launch count.
     * @apiNote used in {@link InstanceRow}
     */
    public static String getStatsString(File instanceFolder) {
        Properties prop = loadStats(instanceFolder);
        long totalMins = 0;
        try {
            totalMins = Long.parseLong(prop.getProperty("playtime", "0"));
        } catch (NumberFormatException e) {
            DebugLogger.log("Invalid playtime value in stats file: " + e.getMessage());
        }
        
        String launches = prop.getProperty("launches", "0");
        
        // NEW: Get Last Played date, default to "Never"
        String lastPlayed = prop.getProperty("lastPlayed", "Never");
        
        long hours = totalMins / 60;
        long mins = totalMins % 60;
        
        return String.format(
            "Time Played: %dh %dm\n" +
            "Launches: %s\n" +
            "Last Played: %s",
            hours, mins, launches, lastPlayed
        );
    }

    /**
     * Loads the properties file from the instance folder.
     * @param instanceFolder The folder of the mod instance
     */
    private static Properties loadStats(File instanceFolder) {
        Properties prop = new Properties();
        File file = new File(instanceFolder, "launcher_stats.properties");
        if (file.exists()) {
            try (InputStream is = new FileInputStream(file)) {
                prop.load(is);
            } catch (IOException e) {
                System.err.println("Could not load stats: " + e.getMessage());
            }
        }
        return prop;
    }

    /**
     * Saves the properties file to the instance folder.
     * @param instanceFolder The folder of the mod instance
     * @param prop Properties that store stats
     */
    private static void saveStats(File instanceFolder, Properties prop) {
        File file = new File(instanceFolder, "launcher_stats.properties");
        try (OutputStream os = new FileOutputStream(file)) {
            prop.store(os, "Instance Statistics");
        } catch (IOException e) {
            DebugLogger.log("Could not save stats: " + e.getMessage());
        }
    }
}