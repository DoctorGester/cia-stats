package com.dglab.cia.database;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author doc
 */
@Entity
@Table(name = "rounds")
public class Round {
	private Match match;
	private Pk pk;
	private Collection<PlayerRoundData> playerRoundData = new HashSet<>();

	@Embeddable
	public static class Pk implements Serializable {
		private long matchId;
		private short number;

		@Column(name = "matchId", nullable = false, updatable = false)
		public long getMatchId() {
			return matchId;
		}

		@Column(name = "number", nullable = false, updatable = false)
		public short getNumber() {
			return number;
		}

		public void setMatchId(long matchId) {
			this.matchId = matchId;
		}

		public void setNumber(short number) {
			this.number = number;
		}
	}

	@EmbeddedId
	public Pk getPk() {
		return pk;
	}

	public void setPk(Pk pk) {
		this.pk = pk;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "matchId", nullable = false, insertable = false, updatable = false)
	public Match getMatch() {
		return match;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "round")
	public Collection<PlayerRoundData> getPlayerRoundData() {
		return playerRoundData;
	}

	@Transient
	public short getNumber() {
		return pk.number;
	}

	public void setMatch(Match match) {
		this.match = match;
	}

	public void setPlayerRoundData(Collection<PlayerRoundData> playerRoundData) {
		this.playerRoundData = playerRoundData;
	}
}
