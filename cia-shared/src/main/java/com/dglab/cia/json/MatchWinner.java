package com.dglab.cia.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author doc
 */
public class MatchWinner {
	private long matchId;
	private byte winnerTeam;

	@JsonCreator()
	public MatchWinner(@JsonProperty(value = "winnerTeam", required = true) byte winnerTeam) {
		this.winnerTeam = winnerTeam;
	}

	public long getMatchId() {
		return matchId;
	}

	public void setMatchId(long matchId) {
		this.matchId = matchId;
	}

	public byte getWinnerTeam() {
		return winnerTeam;
	}

	public void setWinnerTeam(byte winnerTeam) {
		this.winnerTeam = winnerTeam;
	}
}
