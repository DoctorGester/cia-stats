package com.dglab.cia.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author doc
 */
public class MatchInfo {
	private long matchId;
	private String mode;
	private String version;
	private Collection<PlayerInfo> players = new HashSet<>();
	private Instant dateTime;

	@JsonCreator()
	public MatchInfo(
			@JsonProperty(value = "mode", required = true) String mode,
			@JsonProperty(value = "version", required = true) String version,
			@JsonProperty(value = "players", required = true) Collection<PlayerInfo> players) {
		this.mode = mode;
		this.version = version;
		this.players = players;
	}

	public MatchInfo() {
	}

	public long getMatchId() {
		return matchId;
	}

	public void setMatchId(long matchId) {
		this.matchId = matchId;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public byte getPlayerNumber() {
		return (byte) players.size();
	}

	public Collection<PlayerInfo> getPlayers() {
		return players;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Instant getDateTime() {
		return dateTime;
	}

	public void setDateTime(Instant dateTime) {
		this.dateTime = dateTime;
	}
}
