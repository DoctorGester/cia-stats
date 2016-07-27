package com.dglab.cia.persistence;

import com.dglab.cia.json.*;

import java.util.List;

/**
 * @author doc
 */
public interface MatchService {
	MatchDetails getMatchDetails(long matchId);
    List<MatchHeader> getRecentPlayerMatches(long steamId64);
	void putMatch(MatchInfo matchInfo);
	void putRound(RoundInfo roundInfo);
	boolean putWinner(MatchWinner winner);
}
