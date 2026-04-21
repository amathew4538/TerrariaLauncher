package TerrariaLauncher;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.Result;
import de.jcm.discordgamesdk.activity.Activity;

import java.time.Instant;

public class DiscordRPC {
    private boolean isRunning = true;
    private Core core;
    private Activity activity;
    private static final DiscordRPC DISCORD_RPC = new DiscordRPC();

    public static DiscordRPC getDiscordRPC() {
        return DISCORD_RPC;
    }

    /**
     * Initializes the Discord Rich Presence
     */
    public void init() {
        new Thread(() -> {
            try (CreateParams params = new CreateParams()) {
                params.setClientID(1495837937353621744L);
                params.setFlags(CreateParams.getDefaultFlags());

                try (Core core = new Core(params)) {
                    this.core = core;
                    DebugLogger.log("Discord RPC: Core Created");
                    try (Activity activity = new Activity()) {
                        this.activity = activity;
                        DebugLogger.log("Discord RPC: Activity Created");
                        // Initial Presence Setup
                        activity.setDetails("Launching a Terraria Instance");
                        activity.setState("Just Chilling");
                        activity.timestamps().setStart(Instant.now());

                        // Apply the activity
                        core.activityManager().updateActivity(activity);
                        DebugLogger.log("Discord RPC: Activity Applied");

                        // Keep the connection
                        while (isRunning) {
                            core.runCallbacks();
                            try {
                                Thread.sleep(16); // 60fps refresh rate
                            } catch (InterruptedException e) {
                                DebugLogger.log(e.getMessage());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                DebugLogger.log("Discord RPC failed to initialize: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Changes the status of the RPC
     * @param details what to set the details to
     * @param state the state
     */
    public void updateStatus(String details, String state) {
        if (core != null && activity != null) {
            activity.setDetails(details);
            activity.setState(state);
            core.activityManager().updateActivity(activity, result -> {
                // Compares to the result setup that DGSDK4 uses
                if (result == Result.OK) {
                    DebugLogger.log("Discord RPC: Status Updated Successfully");
                } else {
                    DebugLogger.log("Discord RPC: Update Failed with Result: " + result.name());
                }
            });
        } else {
            DebugLogger.log("Discord RPC: Cannot update - Core or Activity is NULL. (Did init run?)");
        }
    }

    /**
     * Stops RPC
     */
    public void stop() {
        this.isRunning = false;
    }
}