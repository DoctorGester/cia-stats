package com.dglab.cia.database;

import com.dglab.cia.RankedMode;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author doc
 */

@Entity
@Table(name = "player_ranks")
public class PlayerRank implements Serializable {
	private Pk pk;
	private byte rank;
	private byte stars;

	@Embeddable
	public static class Pk implements Serializable {
		private long steamId64;
		private byte season;
		private RankedMode mode;

		@Column(name = "steamId64", nullable = false, updatable = false)
		public long getSteamId64() {
			return steamId64;
		}

		@Column(name = "season", nullable = false, updatable = false)
		public byte getSeason() {
			return season;
		}

		@Column(name = "mode", nullable = false, updatable = false)
		@Enumerated(EnumType.ORDINAL)
		public RankedMode getMode() {
			return mode;
		}

		public void setSeason(byte season) {
			this.season = season;
		}

		public void setMode(RankedMode mode) {
			this.mode = mode;
		}

		public void setSteamId64(long steamId64) {
			this.steamId64 = steamId64;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Pk pk = (Pk) o;

			if (steamId64 != pk.steamId64) return false;
			if (season != pk.season) return false;
			return mode == pk.mode;

		}

		@Override
		public int hashCode() {
			int result = (int) (steamId64 ^ (steamId64 >>> 32));
			result = 31 * result + (int) season;
			result = 31 * result + (mode != null ? mode.hashCode() : 0);
			return result;
		}
	}


	@EmbeddedId
	public Pk getPk() {
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

	public void setRank(byte rank) {
		this.rank = rank;
	}

	public void setStars(byte stars) {
		this.stars = stars;
	}

	public void setPk(Pk pk) {
		this.pk = pk;
	}
}
