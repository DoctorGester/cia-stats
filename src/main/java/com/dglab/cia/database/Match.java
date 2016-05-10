package com.dglab.cia.database;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author doc
 */
@Entity
@Table(name = "matches")
public class Match {
	private long matchId;
	private String mode;
	private String version;
	private LocalDateTime dateTime;
	private byte players;
	private byte winnerTeam;

	private Collection<PlayerMatchData> matchData = new HashSet<>();
	private Collection<Round> rounds = new HashSet<>();

	public Match(){}

	public void setRounds(Collection<Round> rounds) {
		this.rounds = rounds;
	}

	@Id
	@Column(name = "matchId", unique = true, nullable = false)
	public long getMatchId() {
		return matchId;
	}

	@Column(name = "mode", nullable = false)
	public String getMode() {
		return mode;
	}

	@Column(name = "playerAmount", nullable = false)
	public byte getPlayers() {
		return players;
	}

	@Column(name = "winner")
	public byte getWinnerTeam() {
		return winnerTeam;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "match")
	public Collection<PlayerMatchData> getMatchData() {
		return matchData;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "match")
	public Collection<Round> getRounds() {
		return rounds;
	}

	@Column(name = "dateTime", nullable = false)
	public LocalDateTime getDateTime() {
		return dateTime;
	}

	@Column(name = "version", nullable = false)
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}

	public void setMatchId(long matchId) {
		this.matchId = matchId;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public void setPlayers(byte players) {
		this.players = players;
	}

	public void setWinnerTeam(byte winnerTeam) {
		this.winnerTeam = winnerTeam;
	}

	public void setMatchData(Collection<PlayerMatchData> matchData) {
		this.matchData = matchData;
	}

	public void setRoundData(Collection<Round> rounds) {
		this.rounds = rounds;
	}
}
