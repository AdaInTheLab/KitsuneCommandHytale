package com.kitsunecommand.features.economy;

import com.google.inject.Inject;
import com.hypixel.hytale.server.core.logging.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.damage.event.KillFeedEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.kitsunecommand.KitsunePlugin;
import com.kitsunecommand.core.AbstractFeature;
import com.kitsunecommand.core.LivePlayerManager;
import com.kitsunecommand.data.repositories.PointsRepository;
import com.kitsunecommand.data.repositories.SettingsRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Points economy feature.
 * Awards points for kills, playtime, and daily sign-in.
 *
 * Equivalent to the Points feature in the C# version, but using Hytale's
 * native KillFeedEvent instead of Harmony-patched EntityKilled.
 */
public class PointsFeature extends AbstractFeature {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final KitsunePlugin plugin;
    private final PointsRepository pointsRepo;
    private final SettingsRepository settingsRepo;
    private final LivePlayerManager livePlayerManager;

    private PointsSettings settings;

    @Inject
    public PointsFeature(
            KitsunePlugin plugin,
            PointsRepository pointsRepo,
            SettingsRepository settingsRepo,
            LivePlayerManager livePlayerManager) {
        super("points", "Points Economy");
        this.plugin = plugin;
        this.pointsRepo = pointsRepo;
        this.settingsRepo = settingsRepo;
        this.livePlayerManager = livePlayerManager;
    }

    @Override
    public void onEnable() {
        // Load settings from database
        this.settings = PointsSettings.load(settingsRepo);

        // Register kill tracking event
        if (settings.isKillTrackingEnabled()) {
            plugin.getEventRegistry().register(
                KillFeedEvent.KillerMessage.class,
                this::onKill
            );
            LOGGER.info("  Kill tracking enabled (reward: {} points)", settings.getKillReward());
        }

        // Register player connect for auto-creation of points records
        plugin.getEventRegistry().register(
            PlayerConnectEvent.class,
            this::onPlayerConnect
        );

        LOGGER.info("Points feature enabled — signIn={}, playtime={}, kills={}",
            settings.isSignInEnabled(), settings.isPlaytimeTrackingEnabled(), settings.isKillTrackingEnabled());
    }

    @Override
    public void onStart() {
        // Schedule playtime rewards using Hytale's task registry
        if (settings.isPlaytimeTrackingEnabled()) {
            int intervalTicks = settings.getPlaytimeIntervalMinutes() * 60 * 20; // minutes → ticks
            plugin.getTaskRegistry().scheduleRepeating(
                this::awardPlaytimePoints,
                intervalTicks,
                intervalTicks
            );
            LOGGER.info("Playtime rewards scheduled — {} points every {} minutes",
                settings.getPlaytimeReward(), settings.getPlaytimeIntervalMinutes());
        }
    }

    // --- Event Handlers ---

    /**
     * When a player connects, ensure they have a points record.
     */
    private void onPlayerConnect(PlayerConnectEvent event) {
        try {
            String playerId = event.getPlayer().getUuid().toString();
            String playerName = event.getPlayer().getName();

            // Create record if it doesn't exist
            pointsRepo.create(playerId, playerName);

            // Update name in case it changed
            pointsRepo.updatePlayerName(playerId, playerName);
        } catch (Exception e) {
            LOGGER.error("Failed to handle player connect for points: {}", e.getMessage());
        }
    }

    /**
     * Award points when a player kills an entity.
     * Uses KillFeedEvent.KillerMessage to identify the killer.
     */
    private void onKill(KillFeedEvent.KillerMessage event) {
        try {
            var damage = event.getDamage();
            // The KillerMessage event fires on the killer's entity
            // We need to check if the attacker is a player
            // TODO: Verify Hytale API — damage.getAttacker() or ECS player check
            // For now, this is the hook point; exact API depends on Damage class structure

            // Placeholder: award points to killer if they're tracked in LivePlayerManager
            // The actual implementation will resolve the player UUID from the Damage/ECS system
        } catch (Exception e) {
            LOGGER.error("Failed to award kill points: {}", e.getMessage());
        }
    }

    /**
     * Award playtime points to all online players.
     * Called on a repeating schedule via Hytale's TaskRegistry.
     */
    private void awardPlaytimePoints() {
        try {
            int reward = settings.getPlaytimeReward();
            int count = 0;

            for (var entry : livePlayerManager.getOnlinePlayers().entrySet()) {
                pointsRepo.addPoints(entry.getKey(), reward);
                count++;
            }

            if (count > 0) {
                LOGGER.debug("Awarded {} playtime points to {} players", reward, count);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to award playtime points: {}", e.getMessage());
        }
    }

    // --- Public API ---

    /**
     * Handle a daily sign-in request from a player.
     * Returns the bonus points awarded, or -1 if already signed in today.
     */
    public int processSignIn(String playerId) {
        if (!settings.isSignInEnabled()) return -1;

        var infoOpt = pointsRepo.findById(playerId);
        if (infoOpt.isEmpty()) return -1;

        var info = infoOpt.get();

        // Check if already signed in today
        if (info.getLastSignInAt() != null) {
            try {
                LocalDate lastSignIn = LocalDateTime
                    .parse(info.getLastSignInAt(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .toLocalDate();
                if (lastSignIn.equals(LocalDate.now())) {
                    return -1; // Already signed in today
                }
            } catch (Exception e) {
                // If parsing fails, allow sign-in
                LOGGER.debug("Could not parse last sign-in date for {}: {}", playerId, e.getMessage());
            }
        }

        int bonus = settings.getSignInBonus();
        pointsRepo.recordSignIn(playerId, bonus);
        return bonus;
    }

    /**
     * Get a player's current points balance.
     */
    public int getBalance(String playerId) {
        return pointsRepo.findById(playerId)
            .map(info -> info.getPoints())
            .orElse(0);
    }

    public PointsSettings getSettings() {
        return settings;
    }
}
