package com.kitsunecommand.features.economy;

import com.kitsunecommand.data.repositories.SettingsRepository;

/**
 * Configuration for the Points feature.
 * All values stored in the settings table with "points." prefix.
 * Same pattern as the C# version where settings live in the database.
 */
public class PointsSettings {

    private int killReward;
    private int deathPenalty;
    private int signInBonus;
    private int playtimeReward;
    private int playtimeIntervalMinutes;
    private boolean killTrackingEnabled;
    private boolean playtimeTrackingEnabled;
    private boolean signInEnabled;

    /**
     * Load settings from the database with sensible defaults.
     */
    public static PointsSettings load(SettingsRepository settings) {
        PointsSettings s = new PointsSettings();
        s.killReward = settings.getInt("points.killReward", 10);
        s.deathPenalty = settings.getInt("points.deathPenalty", 0);
        s.signInBonus = settings.getInt("points.signInBonus", 50);
        s.playtimeReward = settings.getInt("points.playtimeReward", 5);
        s.playtimeIntervalMinutes = settings.getInt("points.playtimeIntervalMinutes", 30);
        s.killTrackingEnabled = settings.getBool("points.killTrackingEnabled", true);
        s.playtimeTrackingEnabled = settings.getBool("points.playtimeTrackingEnabled", true);
        s.signInEnabled = settings.getBool("points.signInEnabled", true);
        return s;
    }

    // --- Getters ---

    public int getKillReward() { return killReward; }
    public int getDeathPenalty() { return deathPenalty; }
    public int getSignInBonus() { return signInBonus; }
    public int getPlaytimeReward() { return playtimeReward; }
    public int getPlaytimeIntervalMinutes() { return playtimeIntervalMinutes; }
    public boolean isKillTrackingEnabled() { return killTrackingEnabled; }
    public boolean isPlaytimeTrackingEnabled() { return playtimeTrackingEnabled; }
    public boolean isSignInEnabled() { return signInEnabled; }
}
