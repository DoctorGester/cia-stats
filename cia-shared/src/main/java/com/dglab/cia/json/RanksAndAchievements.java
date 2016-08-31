package com.dglab.cia.json;

import java.util.Map;

/**
 * @author doc
 */
public class RanksAndAchievements {
    private byte currentSeason;
    private Map<Long, RankAndStars> ranks;
    private Map<Long, RankedAchievements> achievements;

    public byte getCurrentSeason() {
        return currentSeason;
    }

    public void setCurrentSeason(byte currentSeason) {
        this.currentSeason = currentSeason;
    }

    public Map<Long, RankAndStars> getRanks() {
        return ranks;
    }

    public void setRanks(Map<Long, RankAndStars> ranks) {
        this.ranks = ranks;
    }

    public Map<Long, RankedAchievements> getAchievements() {
        return achievements;
    }

    public void setAchievements(Map<Long, RankedAchievements> achievements) {
        this.achievements = achievements;
    }
}
