package com.dglab.cia.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author doc
 */
public class MatchResult {
	private long matchId;
	private byte winnerTeam;
    private final int gameLength;

    @JsonCreator()
	public MatchResult(
			@JsonProperty(value = "winnerTeam", required = true) byte winnerTeam,
            @JsonProperty(value = "gameLength", required = true) int gameLength
    ) {
		this.winnerTeam = winnerTeam;
        this.gameLength = gameLength;
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

    public int getGameLength() {
        return gameLength;
    }
}
