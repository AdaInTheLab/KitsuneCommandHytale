package com.kitsunecommand.data.repositories;

import com.google.inject.Inject;
import com.hypixel.hytale.logger.HytaleLogger;
import com.kitsunecommand.data.entities.PointsInfo;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Data access for the points_info table.
 * Equivalent to PointsInfoRepository.cs in the C# version (Dapper → JDBI).
 */
public class PointsRepository {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final Jdbi jdbi;

    @Inject
    public PointsRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    /**
     * Get a player's points record by their UUID.
     */
    public Optional<PointsInfo> findById(String playerId) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT id, created_at, player_name, points, last_sign_in_at FROM points_info WHERE id = :id")
                .bind("id", playerId)
                .map((rs, ctx) -> {
                    PointsInfo info = new PointsInfo();
                    info.setId(rs.getString("id"));
                    info.setCreatedAt(rs.getString("created_at"));
                    info.setPlayerName(rs.getString("player_name"));
                    info.setPoints(rs.getInt("points"));
                    info.setLastSignInAt(rs.getString("last_sign_in_at"));
                    return info;
                })
                .findOne()
        );
    }

    /**
     * Create a new points record for a player.
     */
    public void create(String playerId, String playerName) {
        jdbi.useHandle(handle ->
            handle.execute(
                "INSERT OR IGNORE INTO points_info (id, player_name, points) VALUES (?, ?, 0)",
                playerId, playerName
            )
        );
    }

    /**
     * Add points to a player's balance.
     */
    public void addPoints(String playerId, int amount) {
        jdbi.useHandle(handle ->
            handle.execute(
                "UPDATE points_info SET points = points + ? WHERE id = ?",
                amount, playerId
            )
        );
    }

    /**
     * Deduct points from a player's balance. Returns true if successful.
     */
    public boolean deductPoints(String playerId, int amount) {
        int updated = jdbi.withHandle(handle ->
            handle.execute(
                "UPDATE points_info SET points = points - ? WHERE id = ? AND points >= ?",
                amount, playerId, amount
            )
        );
        return updated > 0;
    }

    /**
     * Update the last sign-in timestamp and add sign-in bonus points.
     */
    public void recordSignIn(String playerId, int bonusPoints) {
        jdbi.useHandle(handle ->
            handle.execute(
                "UPDATE points_info SET points = points + ?, last_sign_in_at = datetime('now') WHERE id = ?",
                bonusPoints, playerId
            )
        );
    }

    /**
     * Update the player's display name (in case they changed it).
     */
    public void updatePlayerName(String playerId, String playerName) {
        jdbi.useHandle(handle ->
            handle.execute(
                "UPDATE points_info SET player_name = ? WHERE id = ?",
                playerName, playerId
            )
        );
    }

    /**
     * Get the top N players by points.
     */
    public List<PointsInfo> getLeaderboard(int limit) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT id, player_name, points FROM points_info ORDER BY points DESC LIMIT :limit")
                .bind("limit", limit)
                .map((rs, ctx) -> {
                    PointsInfo info = new PointsInfo();
                    info.setId(rs.getString("id"));
                    info.setPlayerName(rs.getString("player_name"));
                    info.setPoints(rs.getInt("points"));
                    return info;
                })
                .list()
        );
    }

    /**
     * Get total number of players with points records.
     */
    public int getPlayerCount() {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT COUNT(*) FROM points_info")
                .mapTo(Integer.class)
                .one()
        );
    }
}
