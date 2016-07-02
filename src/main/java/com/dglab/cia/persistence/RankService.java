package com.dglab.cia.persistence;

import com.dglab.cia.RankedMode;
import com.dglab.cia.database.Match;
import com.dglab.cia.json.RankAndStars;
import com.dglab.cia.json.RankUpdateDetails;

import java.util.Map;

/**
 * @author doc
 */
public interface RankService {
	byte getCurrentSeason();
	Map<RankedMode, RankAndStars> getPlayerRanks(long steamId64);
	RankedMode getMatchRankedMode(Match match);
	Map<Long, RankAndStars> getMatchRanks(long matchId);
	RankUpdateDetails processMatchResults(long matchId);
}
