package com.dglab.cia.persistence;

import com.dglab.cia.json.MatchDetails;
import com.dglab.cia.json.MatchInfo;
import com.dglab.cia.json.MatchWinner;
import com.dglab.cia.json.RoundInfo;

/**
 * @author doc
 */
public interface MatchService {
	MatchDetails getMatchDetails(long matchId);
	void putMatch(MatchInfo matchInfo);
	void putRound(RoundInfo roundInfo);
	void putWinner(MatchWinner winner);
}
