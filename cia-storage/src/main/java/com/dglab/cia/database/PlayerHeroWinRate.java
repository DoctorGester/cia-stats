package com.dglab.cia.database;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author doc
 */
@Entity
@Table(name = "player_hero_win_rates")
public class PlayerHeroWinRate {
    private PlayerHeroWinRateKey pk;
    private int wins;
    private int games;
    private double winrate;

    @EmbeddedId
    public PlayerHeroWinRateKey getPk() {
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

    @Column(name = "winrate", nullable = false)
    public double getWinrate() {
        return winrate;
    }

    public void setGames(int games) {
        this.games = games;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void setWinrate(double winrate) {
        this.winrate = winrate;
    }

    public void setPk(PlayerHeroWinRateKey pk) {
        this.pk = pk;
    }
}
