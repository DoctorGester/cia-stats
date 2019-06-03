package com.dglab.cia.database;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;

/**
 * @author doc
 */
@Entity
@Table(name = "player_hero_win_rates", indexes = {
        @Index(name = "player_hero_win_rates_index", columnList = "steamId64"),
        @Index(name = "player_hero_win_rates_by_hero", columnList = "hero"),
        @Index(name = "player_hero_win_rates_by_games", columnList = "games")
})
public class PlayerHeroWinRate {
    private PlayerHeroWinRateKey pk;
    private int wins;
    private int games;
    private double winrate;
    private PlayerName name;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(
            name = "steamId64",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    public PlayerName getName() {
        return name;
    }

    public void setName(PlayerName name) {
        this.name = name;
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
