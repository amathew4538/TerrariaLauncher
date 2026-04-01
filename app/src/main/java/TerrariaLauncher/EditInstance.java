package TerrariaLauncher;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

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
                List<Path> allFiles = Files.walk(path).sorted(Comparator.reverseOrder()).toList();
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

                // Wait until finished then confirm deletion
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
                    DebugLogger.log("Edit Instance: Could not read existing enabled.json");
                }
            }

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            List<JCheckBox> checkBoxes = new ArrayList<>();

            for (File file : modFiles) {
                // Raw Folder name e.g. "calamityMod"
                String rawName = file.getName().substring(0, file.getName().length() - 5);
                // Display name e.g. "Calamity Mod"
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

    public static void createInstance(String instanceName, File selectedIcon, File rootDir, JPanel container) {
        JDialog progressDialog = new JDialog((java.awt.Frame)null, "Creating Instance: " + instanceName, true);
        JProgressBar progressBar = new JProgressBar(0, 100);
        JLabel statusLabel = new JLabel("Fetching latest tModLoader...");

        progressDialog.setLayout(new BorderLayout(10, 10));
        progressDialog.add(statusLabel, BorderLayout.NORTH);
        progressDialog.add(progressBar, BorderLayout.CENTER);
        progressDialog.setSize(450, 140); // Slightly wider to fit long filenames
        progressDialog.setLocationRelativeTo(null);
        progressBar.setStringPainted(true);
    
        new Thread(() -> {
            try {
                // Get Download URL
                String TMOD_API = "https://api.github.com/repos/tModLoader/tModLoader/releases/latest";
                HttpURLConnection conn = (HttpURLConnection) new URL(TMOD_API).openConnection();
                conn.setRequestProperty("User-Agent", "Terraria-Launcher");
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) result.append(line);
                rd.close();
            
                String downloadUrl = "";
                String[] assets = result.toString().split("\"browser_download_url\":\"");
                for (String asset : assets) {
                    String urlCandidate = asset.split("\"")[0];
                    if (urlCandidate.endsWith(".zip") && urlCandidate.contains("tModLoader") && !urlCandidate.contains("ExampleMod")) {
                        downloadUrl = urlCandidate;
                        break;
                    }
                }
                if (downloadUrl.isEmpty()) throw new Exception("Could not find tModLoader.zip");
            
                // Download the ZIP
                File tempZip = new File(System.getProperty("java.io.tmpdir"), "tModLoader.zip");
                URL url = new URL(downloadUrl);
                HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                long fileSize = httpConn.getContentLengthLong();
            
                try (InputStream in = httpConn.getInputStream(); FileOutputStream out = new FileOutputStream(tempZip)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    long totalBytesRead = 0;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        int percent = (int) ((totalBytesRead * 100) / fileSize);
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("Downloading: " + percent + "%");
                            progressBar.setValue(percent);
                        });
                    }
                }
            
                // Extract Files with Progress Tracking
                File newFolder = new File(rootDir, instanceName);
                if (!newFolder.exists()) newFolder.mkdirs();
            
                // Count entries first to get an accurate progress bar
                int totalEntries = 0;
                try (ZipInputStream countStream = new ZipInputStream(new FileInputStream(tempZip))) {
                    while (countStream.getNextEntry() != null) totalEntries++;
                }
            
                try (ZipInputStream zis = new ZipInputStream(new FileInputStream(tempZip))) {
                    ZipEntry entry;
                    int processedEntries = 0;
                    byte[] buffer = new byte[8192];
                
                    while ((entry = zis.getNextEntry()) != null) {
                        File newFile = new File(newFolder, entry.getName());
                        processedEntries++;
                    
                        // Update UI with filename
                        final String fileName = entry.getName();
                        final int progress = (int) (((double) processedEntries / totalEntries) * 100);
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("Extracting: " + fileName);
                            progressBar.setValue(progress);
                        });
                    
                        if (entry.isDirectory()) {
                            newFile.mkdirs();
                        } else {
                            newFile.getParentFile().mkdirs();
                            try (FileOutputStream fos = new FileOutputStream(newFile)) {
                                int len;
                                while ((len = zis.read(buffer)) > 0) {
                                    fos.write(buffer, 0, len);
                                }
                            }
                        }
                        zis.closeEntry();
                    }
                }
            
                // Post-Extraction
                File scriptFile = new File(newFolder, "start-tmodloader.sh");
                if (scriptFile.exists()) scriptFile.setExecutable(true);
            
                if (selectedIcon != null && selectedIcon.exists()) {
                    File destIcon = new File(newFolder, "icon.png");
                    Files.copy(selectedIcon.toPath(), destIcon.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            
                tempZip.delete();
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    LauncherUtils.scanAndPopulate(container, rootDir);
                    JOptionPane.showMessageDialog(null, "Instance created successfully!");
                });
            
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
                });
            }
        }).start();
    
        progressDialog.setVisible(true);
    }
}