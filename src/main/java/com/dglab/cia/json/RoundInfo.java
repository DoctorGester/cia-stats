package com.dglab.cia.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author doc
 */
public class RoundInfo {
	private long matchId;
	private short roundNumber;
	private Byte winner;
	private Collection<PlayerRoundInfo> players = new HashSet<>();

	@JsonCreator()
	public RoundInfo(
			@JsonProperty(value = "players", required = true) Collection<PlayerRoundInfo> players,
			@JsonProperty(value = "winner") Byte winner) {
		this.players = players;
		this.winner = winner;
	}

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

	public Byte getWinner() {
		return winner;
	}

	public void setWinner(Byte winner) {
		this.winner = winner;
	}
}
