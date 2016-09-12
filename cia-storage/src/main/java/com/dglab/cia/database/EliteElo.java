package com.dglab.cia.database;

import javax.persistence.*;

/**
 * User: kartemov
 * Date: 05.07.2016
 * Time: 20:37
 */
@Entity
@Table(name = "elite_elo")
public class EliteElo {
    private RankPrimaryKey pk;
    private short elo;
    private PlayerRank rank;

    @EmbeddedId
    public RankPrimaryKey getPk() {
        return pk;
    }

    @Column(nullable = false)
    public short getElo() {
        return elo;
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

    public void setElo(short elo) {
        this.elo = elo;
    }
}
