package com.dglab.cia.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author doc
 */
public class HeroWinRateAndGames {
    private String hero;
    private double winRate;
    private long games;

    @JsonCreator()
    public HeroWinRateAndGames(
            @JsonProperty(value = "hero", required = true) String hero,
            @JsonProperty(value = "winRate", required = true) double winRate,
            @JsonProperty(value = "games", required = true) long games) {
        this.hero = hero;
        this.winRate = winRate;
        this.games = games;
    }

    public String getHero() {
        return hero;
    }

    public void setHero(String hero) {
        this.hero = hero;
    }

    public double getWinRate() {
        return winRate;
    }

    public void setWinRate(double winRate) {
        this.winRate = winRate;
    }

    public long getGames() {
        return games;
    }

    public void setGames(long games) {
        this.games = games;
    }
}
