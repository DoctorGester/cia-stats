package com.dglab.cia.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * @author doc
 */
public class MatchResult {
	private long matchId;
	private byte winnerTeam;
    private final int gameLength;
    private final Map<Long, Integer> questProgress;
    private final List<Long> passPlayers;

    @JsonCreator()
	public MatchResult(
			@JsonProperty(value = "winnerTeam", required = true) byte winnerTeam,
            @JsonProperty(value = "gameLength", required = true) int gameLength,
            @JsonProperty(value = "questProgress") Map<Long, Integer> questProgress,
            @JsonProperty(value = "passPlayers") List<Long> passPlayers
    ) {
		this.winnerTeam = winnerTeam;
        this.gameLength = gameLength;
        this.questProgress = questProgress;
        this.passPlayers = passPlayers;
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

    public Map<Long, Integer> getQuestProgress() {
        return questProgress;
    }

    public List<Long> getPassPlayers() {
        return passPlayers;
    }
}