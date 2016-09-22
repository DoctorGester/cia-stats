package com.dglab.cia.persistence;

import com.dglab.cia.json.*;

import java.util.List;
import java.util.Map;

/**
 * @author doc
 */
public interface MatchService {
	MatchDetails getMatchDetails(long matchId);
    List<MatchHeader> getRecentPlayerMatches(long steamId64);
	Map<Long, Long> getMatchesPlayed(MatchInfo info);
	Map<Long, Integer> getPassExperience(MatchInfo info);
	void putMatch(MatchInfo matchInfo);
	void putRound(RoundInfo roundInfo);
	boolean putWinner(MatchResult winner);
}
