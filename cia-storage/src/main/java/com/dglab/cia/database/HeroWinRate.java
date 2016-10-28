package com.dglab.cia.database;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author doc
 */
@Entity
@Table(name = "hero_win_rates")
public class HeroWinRate {
    private HeroWinRateKey pk;
    private int wins;
    private int games;

    @EmbeddedId
    public HeroWinRateKey getPk() {
        return pk;
    }

    @Column(name = "wins", nullable = false)
    public int getWins() {
        return wins;
    }

    @Column(name = "games", nullable = false)
    public int getGames() {
        return games;
    }

    public void setGames(int games) {
        this.games = games;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void setPk(HeroWinRateKey pk) {
        this.pk = pk;
    }
}
