package com.dglab.cia.database;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author doc
 */
@Entity
@Table(name = "player_round_data")
public class PlayerRoundData implements Serializable {
	private Round round;
	private Pk pk;
	private short damageDealt;
	private short projectilesFired;
	private short score;
	private String hero;

	@Embeddable
	public static class Pk implements Serializable {
		private long matchId;
		private short number;
		private long steamId64;

		@Column(name = "matchId", nullable = false, updatable = false)
		public long getMatchId() {
			return matchId;
		}

		@Column(name = "number", nullable = false, updatable = false)
		public short getNumber() {
			return number;
		}

		@Column(name = "steamId64", nullable = false, updatable = false)
		public long getSteamId64() {
			return steamId64;
		}

		public void setMatchId(long matchId) {
			this.matchId = matchId;
		}

		public void setNumber(short number) {
			this.number = number;
		}

		public void setSteamId64(long steamId64) {
			this.steamId64 = steamId64;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Pk pk = (Pk) o;

			return matchId == pk.matchId && number == pk.number && steamId64 == pk.steamId64;
		}

		@Override
		public int hashCode() {
			int result = (int) (matchId ^ (matchId >>> 32));
			result = 31 * result + (int) number;
			result = 31 * result + (int) (steamId64 ^ (steamId64 >>> 32));
			return result;
		}
	}

	@EmbeddedId
	public Pk getPk() {
		return pk;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({
			@JoinColumn(name = "number", nullable = false, insertable = false, updatable = false),
			@JoinColumn(name = "matchId", nullable = false, insertable = false, updatable = false)
	})
	public Round getRound() {
		return round;
	}

	@Column(name = "damageDealt", nullable = false)
	public short getDamageDealt() {
		return damageDealt;
	}

	@Column(name = "projectilesFired", nullable = false)
	public short getProjectilesFired() {
		return projectilesFired;
	}

	@Column(name = "score", nullable = false)
	public short getScore() {
		return score;
	}

	@Column(name = "hero")
	public String getHero() {
		return hero;
	}

	public void setPk(Pk pk) {
		this.pk = pk;
	}

	public void setRound(Round round) {
		this.round = round;
	}

	public void setDamageDealt(short damageDealt) {
		this.damageDealt = damageDealt;
	}

	public void setScore(short score) {
		this.score = score;
	}

	public void setHero(String hero) {
		this.hero = hero;
	}

	public void setProjectilesFired(short projectilesFired) {
		this.projectilesFired = projectilesFired;
	}
}
