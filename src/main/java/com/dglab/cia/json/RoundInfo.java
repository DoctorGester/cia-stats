package com.dglab.cia.json;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author doc
 */
public class RoundInfo {
	private long matchId;
	private short roundNumber;
	private Collection<PlayerRoundInfo> players = new HashSet<>();

	public long getMatchId() {
		return matchId;
	}

	public void setMatchId(long matchId) {
		this.matchId = matchId;
	}

	public short getRoundNumber() {
		return roundNumber;
	}

	public void setRoundNumber(short roundNumber) {
		this.roundNumber = roundNumber;
	}

	public Collection<PlayerRoundInfo> getPlayers() {
		return players;
	}

	public void setPlayers(Collection<PlayerRoundInfo> players) {
		this.players = players;
	}
}
