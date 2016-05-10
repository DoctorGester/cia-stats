package com.dglab.cia.json;

/**
 * @author doc
 */
public class MatchWinner {
	private long matchId;
	private byte winnerTeam;

	public MatchWinner() {
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
