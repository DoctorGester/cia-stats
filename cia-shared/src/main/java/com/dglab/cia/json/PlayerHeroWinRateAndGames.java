package com.dglab.cia.json;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * @author doc
 */
public class PlayerHeroWinRateAndGames {
    @JsonSerialize(using = ToStringSerializer.class)
    private long steamId64;
    private String name;
    private String avatarUrl;
    private HeroWinRateAndGames winRateAndGames;

    public PlayerHeroWinRateAndGames() {}

    public PlayerHeroWinRateAndGames(long steamId64, String name, String avatarUrl, HeroWinRateAndGames winRateAndGames) {
        this.steamId64 = steamId64;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.winRateAndGames = winRateAndGames;
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

    public HeroWinRateAndGames getWinRateAndGames() {
        return winRateAndGames;
    }

    public void setWinRateAndGames(HeroWinRateAndGames winRateAndGames) {
        this.winRateAndGames = winRateAndGames;
    }
}
