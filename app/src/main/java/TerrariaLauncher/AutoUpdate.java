package TerrariaLauncher;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Frame;

public class AutoUpdate {
    private static final String REPO_URL = "https://api.github.com/repos/amathew4538/TerrariaLauncher/releases/latest";

    /**
     * Checks version and starts updates
     * @param currentVersion the version the app is
     */
    public static void checkForUpdates(String currentVersion) {
        if ("Dev-Build".equals(currentVersion)) {
            DebugLogger.log("Development build detected. Skipping update check.");
            return;
        }

        DebugLogger.log("Checking for updates... Local version is: " + currentVersion);
        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(REPO_URL).openConnection();
                conn.setRequestProperty("User-Agent", "Terraria-Launcher-Updater");
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() != 200) {
                    DebugLogger.log("GitHub API Error: " + conn.getResponseCode());
                    return;
                }

                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) result.append(line);
                rd.close();

                String json = result.toString();
                String latestTag = json.split("\"tag_name\":\"")[1].split("\"")[0];
                String cleanLatest = latestTag.replace("v", "");

                if (!cleanLatest.equals(currentVersion)) {
                    String os = System.getProperty("os.name").toLowerCase();
                    String targetAsset = os.contains("mac") ? "TerrariaLauncher-macOS.dmg" : "TerrariaLauncher-Windows.zip";
                    String downloadUrl = "";

                    // Parsing for the specific asset
                    String[] assets = json.split("\\{\"url\":\"https://api.github.com/repos/amathew4538/TerrariaLauncher/releases/assets/");
                    for (String asset : assets) {
                        if (asset.contains(targetAsset)) {
                            String marker = "\"browser_download_url\":\"";
                            int start = asset.indexOf(marker) + marker.length();
                            int end = asset.indexOf("\"", start);
                            downloadUrl = asset.substring(start, end);
                            break;
                        }
                    }

                    if (downloadUrl.isEmpty()) {
                        DebugLogger.log("ERROR: Could not find " + targetAsset + " in assets.");
                        return;
                    }

                    final String finalUrl = downloadUrl;
                    final String extension = targetAsset.endsWith(".dmg") ? ".dmg" : ".zip";

                    SwingUtilities.invokeLater(() -> {
                        int choice = JOptionPane.showConfirmDialog(null,
                            "New version " + latestTag + " is available! Update now?",
                            "Update Found", JOptionPane.YES_NO_OPTION);
                        
                        if (choice == JOptionPane.YES_OPTION) {
                            new Thread(() -> downloadAndInstall(finalUrl, extension)).start();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Download and install the new app
     * @param downloadUrl url to the dmg/zip
     * @param extension dmg or zip
     */
    public static void downloadAndInstall(String downloadUrl, String extension) {
        JDialog progressDialog = new JDialog((Frame)null, "Updating Terraria Launcher", true);
        JProgressBar progressBar = new JProgressBar(0, 100);
        JLabel statusLabel = new JLabel("Connecting to GitHub...");

        progressDialog.setLayout(new BorderLayout(10, 10));
        progressDialog.add(statusLabel, BorderLayout.NORTH);
        progressDialog.add(progressBar, BorderLayout.CENTER);
        progressDialog.setSize(400, 120);
        progressDialog.setLocationRelativeTo(null);
        progressBar.setStringPainted(true);

        new Thread(() -> {
            try {
                // Save to temp file with correct extension
                Path tempPath;
                if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
                    tempPath = Files.createTempFile(
                            "TerrariaUpdate",
                            extension,
                            PosixFilePermissions.asFileAttribute(
                                    EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)
                            )
                    );
                } else {
                    tempPath = Files.createTempFile("TerrariaUpdate", extension);
                    File temp = tempPath.toFile();
                    if (!temp.setReadable(false, false)) {
                        DebugLogger.log("Failed to clear read permissions on temp file: " + temp.getAbsolutePath());
                        throw new IOException("Failed to clear read permissions on temp file: " + temp.getAbsolutePath());
                    }
                    if (!temp.setWritable(false, false)) {
                        DebugLogger.log("Failed to clear write permissions on temp file: " + temp.getAbsolutePath());
                        throw new IOException("Failed to clear write permissions on temp file: " + temp.getAbsolutePath());
                    }
                    if (!temp.setExecutable(false, false)) {
                        DebugLogger.log("Failed to clear execute permissions on temp file: " + temp.getAbsolutePath());
                        throw new IOException("Failed to clear execute permissions on temp file: " + temp.getAbsolutePath());
                    }
                    if (!temp.setReadable(true, true)) {
                        DebugLogger.log("Failed to set owner read permission on temp file: " + temp.getAbsolutePath());
                        throw new IOException("Failed to set owner read permission on temp file: " + temp.getAbsolutePath());
                    }
                    if (!temp.setWritable(true, true)) {
                        DebugLogger.log("Failed to set owner write permission on temp file: " + temp.getAbsolutePath());
                        throw new IOException("Failed to set owner write permission on temp file: " + temp.getAbsolutePath());
                    }
                }

                File tempFile = tempPath.toFile();

                URL url = new URL(downloadUrl);
                HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                long fileSize = httpConn.getContentLengthLong();

                try (InputStream in = httpConn.getInputStream();
                     FileOutputStream out = new FileOutputStream(tempFile)) {
                    
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

                SwingUtilities.invokeLater(() -> statusLabel.setText("Installing... App will restart shortly."));
                Thread.sleep(1000);
                handleHandoff(tempFile);

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Update failed: " + e.getMessage()));
                progressDialog.dispose();
            }
        }).start();

        progressDialog.setVisible(true);
    }

    /**
     * Handoff to seperate app to finish updates
     * @param updateFile the app to update with
     * @throws IOException
     */
    private static void handleHandoff(File updateFile) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        if (os.contains("mac")) {
            // Path: /Users/username/Applications/Terraria/
            String installDir = userHome + "/Applications/Terraria/";

            String script = String.format(
                "sleep 2 && " +
                "mkdir -p '%2$s' && " +
                "MOUNT_DIR=$(hdiutil attach '%1$s' -nobrowse | awk '/\\/Volumes/ {print $3}') && " +
                "if [ -n \"$MOUNT_DIR\" ]; then " +
                "  rm -rf '%2$sTerrariaLauncher.app' '%2$siTerm.app' && " +
                "  rsync -a \"$MOUNT_DIR/\"*.app '%2$s' && " +
                "  hdiutil detach \"$MOUNT_DIR\"; " +
                "fi && " +
                "xattr -rd com.apple.quarantine '%2$s' 2>/dev/null || true && " +
                "rm -f '%1$s' && " +
                "open '%2$sTerrariaLauncher.app'",
                updateFile.getAbsolutePath(), installDir
            );

            String[] cmd = { "/bin/sh", "-c", "nohup " + script + " > /dev/null 2>&1 &" };
            new ProcessBuilder(cmd).start();
            System.exit(0);

        } else if (os.contains("win")) {
            // Path: C:\Users\\username\Documents\Terraria\
            String installDir = userHome + "\\Documents\\Terraria\\";
            File batch = new File(System.getProperty("java.io.tmpdir"), "update.bat");
            
            try (PrintWriter writer = new PrintWriter(batch)) {
                writer.println("@echo off");
                writer.println("timeout /t 2 /nobreak > nul");
                writer.println("if not exist \"" + installDir + "\" mkdir \"" + installDir + "\"");
                // Remove old folders to ensure a clean slate for 'app' and 'runtime'
                writer.println("if exist \"" + installDir + "app\" rd /s /q \"" + installDir + "app\"");
                writer.println("if exist \"" + installDir + "runtime\" rd /s /q \"" + installDir + "runtime\"");
                // Extract zip and restart
                writer.println("powershell -Command \"Expand-Archive -Path '" + updateFile.getAbsolutePath() + "' -DestinationPath '" + installDir + "' -Force\"");
                writer.println("start \"\" \"" + installDir + "TerrariaLauncher.exe\"");
                writer.println("del \"" + updateFile.getAbsolutePath() + "\"");
                writer.println("del \"%~f0\"");
            }
            
            String systemRootStr = System.getenv("SystemRoot");
            if (systemRootStr == null || systemRootStr.isEmpty()) {
                systemRootStr = "C:\\Windows";
            }
            
            File systemRoot = new File(systemRootStr);
            File system32 = new File(systemRoot, "System32");
            File cmdExe = new File(system32, "cmd.exe");

            // Pass the absolute path directly to the ProcessBuilder (fixes CWE-78/88????).
            ProcessBuilder pb = new ProcessBuilder(
                cmdExe.getAbsolutePath(), 
                "/c",
                "start", 
                "/min", 
                "", 
                batch.getAbsolutePath()
            );

            pb.start();
            System.exit(0);
        }
    }
}