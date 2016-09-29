package com.dglab.cia.database;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author doc
 */

@Entity
@Table(name = "player_match_data", indexes = @Index(columnList = "steamId64", name = "id_index"))
public class PlayerMatchData implements Serializable {
	private Match match;
	private Pk pk;
	private byte team;
	private PlayerName name;

	@Embeddable
	public static class Pk implements Serializable {
		private long matchId;
		private long steamId64;

		@Column(name = "matchId", nullable = false, updatable = false)
		public long getMatchId() {
			return matchId;
		}

		@Column(name = "steamId64", nullable = false, updatable = false)
		public long getSteamId64() {
			return steamId64;
		}

		public void setMatchId(long matchId) {
			this.matchId = matchId;
		}

		public void setSteamId64(long steamId64) {
			this.steamId64 = steamId64;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Pk pk = (Pk) o;

			return matchId == pk.matchId && steamId64 == pk.steamId64;
		}

		@Override
		public int hashCode() {
			int result = (int) (matchId ^ (matchId >>> 32));
			result = 31 * result + (int) (steamId64 ^ (steamId64 >>> 32));
			return result;
		}
	}

	@EmbeddedId
	public Pk getPk() {
		return pk;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "matchId", nullable = false, insertable = false, updatable = false)
	public Match getMatch() {
		return match;
	}

	@Column(name = "team", nullable = false)
	public byte getTeam() {
		return team;
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

	public void setPk(Pk pk) {
		this.pk = pk;
	}

	public void setMatch(Match match) {
		this.match = match;
	}

	public void setName(PlayerName name) {
		this.name = name;
	}

	public void setTeam(byte team) {
		this.team = team;
	}
}
