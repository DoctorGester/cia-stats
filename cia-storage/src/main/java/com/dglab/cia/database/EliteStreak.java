package com.dglab.cia.database;

import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

/**
 * User: kartemov
 * Date: 05.07.2016
 * Time: 20:37
 */
@Entity
@Table(name = "elite_streaks")
public class EliteStreak {
    private RankPrimaryKey pk;
    private short currentStreak;
    private short maxStreak;
    private PlayerRank rank;

    @EmbeddedId
    public RankPrimaryKey getPk() {
        return pk;
    }

    @ColumnDefault("0")
    @Column(nullable = false)
    public short getCurrentStreak() {
        return currentStreak;
    }

    @ColumnDefault("0")
    @Column(nullable = false)
    public short getMaxStreak() {
        return maxStreak;
    }

    @OneToOne
    @JoinColumns({
            @JoinColumn(
                    name = "steamId64",
                    referencedColumnName = "steamId64",
                    nullable = false,
                    insertable = false,
                    updatable = false
            ),
            @JoinColumn(
                    name = "season",
                    referencedColumnName = "season",
                    nullable = false,
                    insertable = false,
                    updatable = false
            ),
            @JoinColumn(
                    name = "\"MODE\"",
                    referencedColumnName = "\"MODE\"",
                    nullable = false,
                    insertable = false,
                    updatable = false
            )
    })
    public PlayerRank getRank() {
        return rank;
    }

    public void setRank(PlayerRank rank) {
        this.rank = rank;
    }

    public void setPk(RankPrimaryKey pk) {
        this.pk = pk;
    }

    public void setCurrentStreak(short currentStreak) {
        this.currentStreak = currentStreak;
    }

    public void setMaxStreak(short maxStreak) {
        this.maxStreak = maxStreak;
    }
}
