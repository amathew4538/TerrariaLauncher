package TerrariaLauncher;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


public class LogFetcher {
    /**
     * Fetches logs from the instance and displays them in a tabbed popup
     * @param instancePath Path to the specific instance folder
     */
    public static void showLogs(Path instancePath) {
        File logDir = new File(instancePath.toFile(), "tModLoader-Logs");
        
        if (!logDir.exists() || !logDir.isDirectory()) {
            JOptionPane.showMessageDialog(null, "No logs found for this instance.");
            return;
        }

        JTabbedPane tabbedPane = new JTabbedPane();
        
        // skip the "old" folder
        String[] logFiles = {"client.log", "environment-client.log", "Launch.log", "Natives.log", "tModPorter.log"};

        for (String fileName : logFiles) {
            File logFile = new File(logDir, fileName);
            if (logFile.exists()) {
                tabbedPane.addTab(fileName, createLogTextPane(logFile));
            }
        }

        JDialog dialog = new JDialog();
        dialog.setTitle("Instance Logs: " + instancePath.getFileName());
        dialog.add(tabbedPane);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    /**
     * Creates a text area for the log info
     * @param file the log file
     * @return a scroll pane with the text
     */
    private static JComponent createLogTextPane(File file) {
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setBackground(new Color(30, 30, 30));
        textPane.setFont(new Font("Monospaced", Font.PLAIN, 12));

        StyledDocument doc = textPane.getStyledDocument();
    
        // Define Colors
        Style defaultStyle = textPane.addStyle("Default", null);
        StyleConstants.setForeground(defaultStyle, Color.LIGHT_GRAY);
    
        Style errorStyle = textPane.addStyle("Error", null);
        StyleConstants.setForeground(errorStyle, Color.RED);
    
        Style warnStyle = textPane.addStyle("Warn", null);
        StyleConstants.setForeground(warnStyle, Color.YELLOW);
    
        Style infoStyle = textPane.addStyle("Info", null);
        StyleConstants.setForeground(infoStyle, Color.CYAN);
    
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            Style activeStyle = defaultStyle; // Track the current color category

            while ((line = reader.readLine()) != null) {
                String upperLine = line.toUpperCase().trim();

                // Check if this is a new log entry (starts with a timestamp [HH:mm:ss])
                if (line.startsWith("[")) {
                    // Reset to default before determining new color
                    activeStyle = defaultStyle;

                    if (upperLine.contains("SILENTLY CAUGHT EXCEPTION")) {
                        activeStyle = warnStyle;
                    } else if (upperLine.contains("[ERROR]") || upperLine.contains("FAIL") || upperLine.contains("EXCEPTION")) {
                        activeStyle = errorStyle;
                    } else if (upperLine.contains("[WARN]")) {
                        activeStyle = warnStyle;
                    }
                }
                // Check if this is a stack trace
                // Most stack traces start with "at " or are heavily indented
                else if (line.startsWith("   at ") || line.startsWith("\tat ") || line.startsWith("System.")) {
                    // Keep using whatever 'activeStyle' was set by the header above it
                }
                // If it's just a random line, reset to default
                else if (!line.trim().isEmpty()) {
                    activeStyle = defaultStyle;
                }
            
                doc.insertString(doc.getLength(), line + "\n", activeStyle);
            }
        } catch (Exception e) {
            DebugLogger.log("Error Loading Log: " + e.getMessage());
            try { doc.insertString(doc.getLength(), "Error loading log: " + e.getMessage(), errorStyle); }
            catch (Exception ignored) {DebugLogger.log("Ignored:" + ignored.getMessage());}
        }
    
        return new JScrollPane(textPane);
    }
}
