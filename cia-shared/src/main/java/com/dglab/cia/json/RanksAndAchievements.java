package com.dglab.cia.json;

import java.util.Map;

/**
 * @author doc
 */
public class RanksAndAchievements {
    private byte currentSeason;
    private Map<Long, RankedAchievements> achievements;
    private Map<Long, Long> gamesPlayed;
    private Map<Long, Integer> passExperience;

    public byte getCurrentSeason() {
        return currentSeason;
    }

    public void setCurrentSeason(byte currentSeason) {
        this.currentSeason = currentSeason;
    }

    public Map<Long, RankedAchievements> getAchievements() {
        return achievements;
    }

    public void setAchievements(Map<Long, RankedAchievements> achievements) {
        this.achievements = achievements;
    }

    public Map<Long, Long> getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(Map<Long, Long> gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public Map<Long, Integer> getPassExperience() {
        return passExperience;
    }

    public void setPassExperience(Map<Long, Integer> passExperience) {
        this.passExperience = passExperience;
    }
}
