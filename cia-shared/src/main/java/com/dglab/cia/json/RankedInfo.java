package com.dglab.cia.json;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * @author doc
 */
public class RankedInfo {
    private Map<RankedMode, List<RankedPlayer>> topPlayers;
    private Map<RankedMode, List<RankedPlayer>> previousTopPlayers;
    private byte currentSeason;
    private Instant seasonEndTime;

    public Map<RankedMode, List<RankedPlayer>> getTopPlayers() {
        return topPlayers;
    }

    public void setTopPlayers(Map<RankedMode, List<RankedPlayer>> topPlayers) {
        this.topPlayers = topPlayers;
    }

    public Map<RankedMode, List<RankedPlayer>> getPreviousTopPlayers() {
        return previousTopPlayers;
    }

    public void setPreviousTopPlayers(Map<RankedMode, List<RankedPlayer>> previousTopPlayers) {
        this.previousTopPlayers = previousTopPlayers;
    }

    public byte getCurrentSeason() {
        return currentSeason;
    }

    public void setCurrentSeason(byte currentSeason) {
        this.currentSeason = currentSeason;
    }

    public Instant getSeasonEndTime() {
        return seasonEndTime;
    }

    public void setSeasonEndTime(Instant seasonEndTime) {
        this.seasonEndTime = seasonEndTime;
    }
}
