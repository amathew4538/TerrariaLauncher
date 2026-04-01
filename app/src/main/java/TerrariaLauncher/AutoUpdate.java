package TerrariaLauncher;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.swing.*;
import java.awt.BorderLayout;

public class AutoUpdate {
    private static final String REPO_URL = "https://api.github.com/repos/amathew4538/TerrariaLauncher/releases/latest";

    public static void checkForUpdates(String currentVersion) {
        // Don't run update check if it's a vDev-Build
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

                // Parse latest tag version
                String latestTag = json.split("\"tag_name\":\"")[1].split("\"")[0];
                String cleanLatest = latestTag.replace("v", "");
                DebugLogger.log("Latest on GitHub: " + cleanLatest);

                if (!cleanLatest.equals(currentVersion)) {
                    // Find the macOS Asset URL specifically
                    String downloadUrl = "";
                    String[] assetBlocks = json.split("\\{\"url\":\"https://api.github.com/repos/amathew4538/TerrariaLauncher/releases/assets/");

                    for (String block : assetBlocks) {
                        if (block.contains("TerrariaLauncher-macOS.zip")) {
                            String marker = "\"browser_download_url\":\"";
                            int start = block.indexOf(marker);
                            if (start != -1) {
                                start += marker.length();
                                int end = block.indexOf("\"", start);
                                downloadUrl = block.substring(start, end);
                                break;
                            }
                        }
                    }

                    if (downloadUrl.isEmpty()) {
                        DebugLogger.log("ERROR: Could not find TerrariaLauncher-macOS.zip in assets.");
                        return;
                    }

                    final String finalUrl = downloadUrl;

                    // Show confirmation popup on the UI thread
                    SwingUtilities.invokeLater(() -> {
                        int choice = JOptionPane.showConfirmDialog(null,
                            "New version " + latestTag + " is available! Update now?",
                            "Update Found", JOptionPane.YES_NO_OPTION);
                        
                        if (choice == JOptionPane.YES_OPTION) {
                            new Thread(() -> downloadAndInstall(finalUrl)).start();
                        }
                    });
                } else {
                    DebugLogger.log("App is up to date.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void downloadAndInstall(String downloadUrl) {
        // Create Progress Window
        JDialog progressDialog = new JDialog((java.awt.Frame)null, "Updating Terraria Launcher", true);
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
                File tempZip = new File(System.getProperty("user.home") + "/Downloads/TerrariaUpdate.zip");
                URL url = new URL(downloadUrl);
                HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                long fileSize = httpConn.getContentLengthLong();

                try (InputStream in = httpConn.getInputStream();
                     FileOutputStream out = new FileOutputStream(tempZip)) {
                    
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
                handleHandoff(tempZip);

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Update failed: " + e.getMessage()));
                progressDialog.dispose();
            }
        }).start();

        progressDialog.setVisible(true);
    }

    private static void handleHandoff(File tempZip) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        String installDir = System.getProperty("user.home") + "/Applications/Terraria/";

        if (os.contains("mac")) {
            String stageDir = installDir + "update_stage/";

            // Deletes iTerm
            // Unzips to a temp directory
            // Finds the .apps inside that folder and moves them up
            // Fixes permissions and quarantine, then launches
            String script = String.format(
                "mkdir -p '%1$s' '%2$s' && sleep 2 && " +
                "rm -rf '%1$sTerrariaLauncher.app' '%1$siTerm.app' && " +
                "unzip -o '%3$s' -d '%2$s' > /dev/null && " +
                "find '%2$s' -name '*.app' -maxdepth 2 -exec cp -R {} '%1$s' \\; && " + 
                "chmod +x '%1$sTerrariaLauncher.app/Contents/MacOS/TerrariaLauncher' && " +
                "chmod +x '%1$siTerm.app/Contents/MacOS/iTerm2' 2>/dev/null || true && " +
                "xattr -rd com.apple.quarantine '%1$sTerrariaLauncher.app' 2>/dev/null || true && " +
                "rm -rf '%2$s' '%3$s' && " +
                "open '%1$sTerrariaLauncher.app'",
                installDir, stageDir, tempZip.getAbsolutePath()
            );

            String[] cmd = { "/bin/sh", "-c", "nohup " + script + " > /dev/null 2>&1 &" };
            new ProcessBuilder(cmd).start();
            System.exit(0);

        } else if (os.contains("win")) {
            File batch = new File("update.bat");
            try (PrintWriter writer = new PrintWriter(batch)) {
                writer.println("@echo off");
                writer.println("timeout /t 2 /nobreak");
                writer.println("powershell -Command \"Expand-Archive -Path '" + tempZip.getAbsolutePath() + "' -DestinationPath '" + installDir + "' -Force\"");
                writer.println("start \"\" \"" + installDir + "TerrariaLauncher.exe\"");
                writer.println("del \"%~f0\"");
            }
            Runtime.getRuntime().exec("cmd /c start update.bat");
            System.exit(0);
        }
    }
}