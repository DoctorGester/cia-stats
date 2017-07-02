package com.dglab.cia.json;

import java.util.List;
import java.util.Map;

/**
 * @author doc
 */
public class PlayerProfileInfo {
    private long steamId64;
    private String name;
    private String avatarUrl;
    private Map<Byte, Map<RankedMode, RankAndStars>> rankHistory;
    private List<HeroWinRateAndGames> winrates;

    public PlayerProfileInfo() {}

    public PlayerProfileInfo(
            long steamId64,
            String name,
            String avatarUrl,
            Map<Byte, Map<RankedMode, RankAndStars>> rankHistory,
            List<HeroWinRateAndGames> winrates
    ) {
        this.steamId64 = steamId64;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.rankHistory = rankHistory;
        this.winrates = winrates;
    }

    public Map<Byte, Map<RankedMode, RankAndStars>> getRankHistory() {
        return rankHistory;
    }

    public void setRankHistory(Map<Byte, Map<RankedMode, RankAndStars>> rankHistory) {
        this.rankHistory = rankHistory;
    }

    public long getSteamId64() {
        return steamId64;
    }

    public void setSteamId64(long steamId64) {
        this.steamId64 = steamId64;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public List<HeroWinRateAndGames> getWinrates() {
        return winrates;
    }

    public void setWinrates(List<HeroWinRateAndGames> winrates) {
        this.winrates = winrates;
    }
}
