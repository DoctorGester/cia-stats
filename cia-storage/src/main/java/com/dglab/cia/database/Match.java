package com.dglab.cia.database;

import com.dglab.cia.json.MatchMap;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.time.Instant;
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
	private Instant dateTime;
	private byte players;
	private byte winnerTeam;
	private MatchMap map;
	private Integer gameLength;

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

	@Column(name = "\"MODE\"", nullable = false)
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

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "match", cascade = {
	    CascadeType.PERSIST, CascadeType.MERGE
	})
	public Collection<PlayerMatchData> getMatchData() {
		return matchData;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "match", cascade = {
	    CascadeType.PERSIST, CascadeType.MERGE
	})
	public Collection<Round> getRounds() {
		return rounds;
	}

	@Column(name = "dateTime", nullable = false)
	public Instant getDateTime() {
		return dateTime;
	}

	@Column(name = "\"VERSION\"", nullable = false)
	public String getVersion() {
		return version;
	}

	@ColumnDefault("0")
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "\"MAP\"", nullable = false)
	public MatchMap getMap() { return map; }

    @Column(name = "gameLength")
    public Integer getGameLength() {
        return gameLength;
    }

    public void setGameLength(Integer gameLength) {
        this.gameLength = gameLength;
    }

    public void setVersion(String version) {
		this.version = version;
	}

	public void setDateTime(Instant dateTime) {
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

	public void setMap(MatchMap map) {
		this.map = map;
	}
}
