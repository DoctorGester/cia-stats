package com.dglab.cia.database;

import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author doc
 */

@Entity
@Table(name = "player_ranks")
public class PlayerRank implements Serializable {
	private RankPrimaryKey pk;
	private byte rank;
	private byte stars;
    private EliteStreak streak;

	@EmbeddedId
	public RankPrimaryKey getPk() {
		return pk;
	}

	@ColumnDefault("30")
	@Column(name = "rank", nullable = false)
	public byte getRank() {
		return rank;
	}

	@Column(name = "stars", nullable = false)
	public byte getStars() {
		return stars;
	}

    @OneToOne(mappedBy = "rank", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public EliteStreak getStreak() {
        return streak;
    }

	public void setRank(byte rank) {
		this.rank = rank;
	}

	public void setStars(byte stars) {
		this.stars = stars;
	}

	public void setPk(RankPrimaryKey pk) {
		this.pk = pk;
	}

    public void setStreak(EliteStreak streak) {
        this.streak = streak;
    }
}
