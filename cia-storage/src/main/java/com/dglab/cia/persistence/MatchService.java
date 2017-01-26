package com.dglab.cia.persistence;

import com.dglab.cia.json.*;
import com.dglab.cia.util.MatchAlreadyExistsException;

import java.util.List;
import java.util.Map;

/**
 * @author doc
 */
public interface MatchService {
	MatchInfo getMatchDetails(long matchId);
    List<MatchHeader> getRecentPlayerMatches(long steamId64);
	Map<Long, Long> getMatchesPlayed(PlayerList info);
	Map<Long, Integer> getPassExperience(PlayerList info);
	void putMatch(MatchInfo matchInfo) throws MatchAlreadyExistsException;
}
