package com.kitsunecommand.data.entities;

/**
 * Represents a player's points balance and sign-in tracking.
 * Maps to the `points_info` table.
 */
public class PointsInfo {

    private String id;          // Player UUID
    private String createdAt;
    private String playerName;
    private int points;
    private String lastSignInAt;

    public PointsInfo() {}

    public PointsInfo(String id, String playerName) {
        this.id = id;
        this.playerName = playerName;
        this.points = 0;
    }

    // --- Getters & Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getLastSignInAt() { return lastSignInAt; }
    public void setLastSignInAt(String lastSignInAt) { this.lastSignInAt = lastSignInAt; }

    @Override
    public String toString() {
        return "PointsInfo{id='" + id + "', name='" + playerName + "', points=" + points + "}";
    }
}
