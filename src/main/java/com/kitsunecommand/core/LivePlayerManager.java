package com.kitsunecommand.core;

import com.google.inject.Inject;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

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
        LOGGER.at(Level.INFO).log("LivePlayerManager registered — tracking online players");
    }

    private void onPlayerConnect(PlayerConnectEvent event) {
        try {
            var playerRef = event.getPlayerRef();
            if (playerRef == null) return;
            String uuid = playerRef.getUuid().toString();
            String name = playerRef.getUsername();
            onlinePlayers.put(uuid, Instant.now().toEpochMilli());
            playerNames.put(uuid, name);
            LOGGER.at(Level.FINE).log("Player connected: %s (%s)", name, uuid);
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("Failed to track player connect");
        }
    }

    private void onPlayerDisconnect(PlayerDisconnectEvent event) {
        try {
            var playerRef = event.getPlayerRef();
            if (playerRef == null) return;
            String uuid = playerRef.getUuid().toString();
            String name = playerNames.getOrDefault(uuid, "unknown");
            onlinePlayers.remove(uuid);
            playerNames.remove(uuid);
            LOGGER.at(Level.FINE).log("Player disconnected: %s (%s)", name, uuid);
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("Failed to track player disconnect");
        }
    }

    // --- Public API ---

    public Map<String, Long> getOnlinePlayers() {
        return Collections.unmodifiableMap(onlinePlayers);
    }

    public String getPlayerName(String uuid) {
        return playerNames.get(uuid);
    }

    public boolean isOnline(String uuid) {
        return onlinePlayers.containsKey(uuid);
    }

    public int getOnlineCount() {
        return onlinePlayers.size();
    }

    public long getSessionDurationMs(String uuid) {
        Long joinTime = onlinePlayers.get(uuid);
        if (joinTime == null) return -1;
        return Instant.now().toEpochMilli() - joinTime;
    }
}
