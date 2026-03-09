package com.kitsunecommand.core;

import com.google.inject.Inject;
import com.hypixel.hytale.server.core.event.EventRegistry;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.logging.HytaleLogger;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks currently online players.
 * Provides a thread-safe map of player UUID → join time.
 *
 * Used by features like playtime rewards to know who's online.
 */
public class LivePlayerManager {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    /**
     * Map of player UUID string → time they connected (epoch millis).
     */
    private final ConcurrentHashMap<String, Long> onlinePlayers = new ConcurrentHashMap<>();

    /**
     * Map of player UUID → display name for convenience.
     */
    private final ConcurrentHashMap<String, String> playerNames = new ConcurrentHashMap<>();

    @Inject
    public LivePlayerManager() {
    }

    /**
     * Register event listeners for player connect/disconnect.
     * Called during plugin setup.
     */
    public void register(EventRegistry eventRegistry) {
        eventRegistry.register(PlayerConnectEvent.class, this::onPlayerConnect);
        eventRegistry.register(PlayerDisconnectEvent.class, this::onPlayerDisconnect);
        LOGGER.info("LivePlayerManager registered — tracking online players");
    }

    private void onPlayerConnect(PlayerConnectEvent event) {
        String uuid = event.getPlayer().getUuid().toString();
        String name = event.getPlayer().getName();
        onlinePlayers.put(uuid, Instant.now().toEpochMilli());
        playerNames.put(uuid, name);
        LOGGER.debug("Player connected: {} ({})", name, uuid);
    }

    private void onPlayerDisconnect(PlayerDisconnectEvent event) {
        String uuid = event.getPlayer().getUuid().toString();
        String name = playerNames.getOrDefault(uuid, "unknown");
        onlinePlayers.remove(uuid);
        playerNames.remove(uuid);
        LOGGER.debug("Player disconnected: {} ({})", name, uuid);
    }

    // --- Public API ---

    /**
     * Get an unmodifiable view of online players (UUID → connect time).
     */
    public Map<String, Long> getOnlinePlayers() {
        return Collections.unmodifiableMap(onlinePlayers);
    }

    /**
     * Get the display name of an online player by UUID.
     */
    public String getPlayerName(String uuid) {
        return playerNames.get(uuid);
    }

    /**
     * Check if a player is currently online.
     */
    public boolean isOnline(String uuid) {
        return onlinePlayers.containsKey(uuid);
    }

    /**
     * Get the number of currently online players.
     */
    public int getOnlineCount() {
        return onlinePlayers.size();
    }

    /**
     * Get how long a player has been online in milliseconds, or -1 if not online.
     */
    public long getSessionDurationMs(String uuid) {
        Long joinTime = onlinePlayers.get(uuid);
        if (joinTime == null) return -1;
        return Instant.now().toEpochMilli() - joinTime;
    }
}
