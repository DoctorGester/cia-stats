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
	private Byte winner;
	private Collection<PlayerRoundData> playerRoundData = new HashSet<>();

	@Embeddable
	public static class Pk implements Serializable {
		private long matchId;
		private short number;

		@Column(name = "matchId", nullable = false, updatable = false)
		public long getMatchId() {
			return matchId;
		}

		@Column(name = "\"NUMBER\"", nullable = false, updatable = false)
		public short getNumber() {
			return number;
		}

		public void setMatchId(long matchId) {
			this.matchId = matchId;
		}

		public void setNumber(short number) {
			this.number = number;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Pk pk = (Pk) o;

			return matchId == pk.matchId && number == pk.number;
		}

		@Override
		public int hashCode() {
			int result = (int) (matchId ^ (matchId >>> 32));
			result = 31 * result + (int) number;
			return result;
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

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "round", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	public Collection<PlayerRoundData> getPlayerRoundData() {
		return playerRoundData;
	}

	@Column(name = "winner")
	public Byte getWinner() {
		return winner;
	}

	public void setWinner(Byte winner) {
		this.winner = winner;
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
