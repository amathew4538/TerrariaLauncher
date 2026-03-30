package TerrariaLauncher;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.Timer;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class EditInstance {

    public static void deleteInstance(Path path) {
        String folderName = path.getFileName().toString();

        if (folderName.equalsIgnoreCase("Terraria.app") || folderName.equalsIgnoreCase("Terraria.exe")) {
            JOptionPane.showMessageDialog(null, "Error: Cannot delete base game files.", "Protected File", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame)null, "Deleting Instance...", true);
        JProgressBar progressBar = new JProgressBar(0, 100);
        JLabel fileLabel = new JLabel("Preparing...");
    
        progressBar.setPreferredSize(new Dimension(350, 25));
        dialog.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        dialog.add(fileLabel);
        dialog.add(progressBar);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
    
        // Run Deletion in Background
        new Thread(() -> {
            try {
                // Count total files first for the progress bar
                List<Path> allFiles = Files.walk(path)
                                        .sorted(Comparator.reverseOrder())
                                        .toList();
                int total = allFiles.size();
            
                for (int i = 0; i < total; i++) {
                    File file = allFiles.get(i).toFile();
                    String currentName = file.getName();
                
                    // Update UI on the Event Dispatch Thread
                    int progress = (int) (((double) (i + 1) / total) * 100);
                    SwingUtilities.invokeLater(() -> {
                        fileLabel.setText("Deleting: " + currentName);
                        progressBar.setValue(progress);
                    });
                
                    // Small sleep so the user can actually see the files flash by
                    Thread.sleep(10);
                
                    file.delete();
                }
            
                // 4. Finalize
                SwingUtilities.invokeLater(() -> {
                    dialog.dispose();
                    DebugLogger.log("EditInstance: Successfully deleted " + folderName);
                    JOptionPane.showMessageDialog(null, "Instance '" + folderName + "' has been deleted.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                });
            
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    dialog.dispose();
                    DebugLogger.log("EditInstance Error: " + e.getMessage());
                    JOptionPane.showMessageDialog(null, "Error during deletion: " + e.getMessage());
                });
            }
        }).start();
    
        dialog.setVisible(true); // Blocks main thread until dialog.dispose() is called
    }

    public static void editMods(Path path) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")){
            String modsPath = System.getProperty("user.home") + "/Library/Application Support/Terraria/tModLoader-preview/Mods";
            File modsFolder = new File(modsPath);

            if (!modsFolder.exists() || !modsFolder.isDirectory()) {
                JOptionPane.showMessageDialog(null, "Mods folder not found at: " + modsPath);
                return;
            }

            File[] modFiles = modsFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".tmod"));

            if (modFiles == null || modFiles.length == 0) {
                JOptionPane.showMessageDialog(null, "No mods (.tmod files) found in the folder.");
                return;
            }

            String existingEnabled = "";
            File enabledFile = new File(path.toFile(), "enabled.json");
            if (enabledFile.exists()) {
                try {
                    existingEnabled = Files.readString(enabledFile.toPath());
                } catch (IOException e) {
                    DebugLogger.log("EditInstance: Could not read existing enabled.json");
                }
            }

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            List<JCheckBox> checkBoxes = new ArrayList<>();

            for (File file : modFiles) {
                // Internal name (e.g., "CalamityMod")
                String rawName = file.getName().substring(0, file.getName().length() - 5);
                // Display name (e.g., "Calamity Mod")
                String displayName = LauncherUtils.formatFolderName(rawName);

                JCheckBox checkBox = new JCheckBox(displayName);

                if (existingEnabled.contains("\"" + rawName + "\"")) {
                    checkBox.setSelected(true);
                }

                checkBoxes.add(checkBox);
                panel.add(checkBox);
            }

            JScrollPane scrollPane = new JScrollPane(panel);
            scrollPane.setPreferredSize(new Dimension(300, 400));

            int result = JOptionPane.showConfirmDialog(null, scrollPane,
                    "Select Mods to Enable", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                List<String> enabledMods = new ArrayList<>();
                for (JCheckBox cb : checkBoxes) {
                    if (cb.isSelected()) {
                        String modTitle = cb.getText();
                        String modInternal = modTitle.replaceAll("\\s+", "");
                        enabledMods.add(modInternal);
                    }
                }
                saveToEnabledJson(path.toFile(), enabledMods);

                JDialog loadingDialog = new JDialog((Frame)null, "Saving...", true);
                JProgressBar progressBar = new JProgressBar(0, 100);
                progressBar.setIndeterminate(true); // Makes the bar slide back and forth
                progressBar.setPreferredSize(new Dimension(300, 30));
                
                loadingDialog.setLayout(new FlowLayout());
                loadingDialog.add(new JLabel("Applying mod changes..."));
                loadingDialog.add(progressBar);
                loadingDialog.pack();
                loadingDialog.setLocationRelativeTo(null);

                // Timer to close loading and show "Done"
                Timer timer = new Timer(1000, e -> {
                    loadingDialog.dispose();
                    saveToEnabledJson(path.toFile(), enabledMods);
                    JOptionPane.showMessageDialog(null, "Mod settings updated successfully!",
                        "Done", JOptionPane.INFORMATION_MESSAGE);
                });
                
                timer.setRepeats(false);
                timer.start();
                
                loadingDialog.setVisible(true);
            }
        }
    }

    public static void saveToEnabledJson(File instanceFolder, List<String> modList) {
        File outputFile = new File(instanceFolder, "enabled.json");

        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write("[\n");

            for (int i = 0; i < modList.size(); i++) {
                writer.write("  \"" + modList.get(i) + "\"");
            
                // Only add a comma if it is NOT the last item
                if (i < modList.size() - 1) {
                    writer.write(",");
                }
                writer.write("\n");
            }
        
            writer.write("]");
            DebugLogger.log("ModEditor: Saved " + modList.size() + " mods to " + outputFile.getPath());
        } catch (IOException e) {
            DebugLogger.log("ModEditor Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}