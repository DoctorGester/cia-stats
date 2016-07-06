package com.dglab.cia.json;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author doc
 */
public class RankedPlayer {
	// Thanks you cuck javascript
	@JsonSerialize(using = SteamIdSerializer.class, as = String.class)
	private long steamId64;
	private byte rank;

	public RankedPlayer(long steamId64, byte rank) {
		this.steamId64 = steamId64;
		this.rank = rank;
	}

	public long getSteamId64() {
		return steamId64;
	}

	public void setSteamId64(long steamId64) {
		this.steamId64 = steamId64;
	}

	public byte getRank() {
		return rank;
	}

	public void setRank(byte rank) {
		this.rank = rank;
	}
}
