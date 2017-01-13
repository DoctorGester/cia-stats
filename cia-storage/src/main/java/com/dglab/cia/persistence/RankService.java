package com.dglab.cia.persistence;

import com.dglab.cia.json.*;
import com.dglab.cia.database.Match;

import java.util.List;
import java.util.Map;

/**
 * @author doc
 */
public interface RankService {
	short STARTING_ELO = 1000;

	byte getCurrentSeason();
	Map<RankedMode, RankAndStars> getPlayerRanks(long steamId64);
	Map<Byte, Map<RankedMode, RankAndStars>> getPlayerRankHistory(long steamId64);
	RankedMode getMatchRankedMode(String mode, int players, MatchMap map);
	Map<Long, RankAndStars> getMatchRanks(MatchInfo matchInfo);
	Map<Long, RankedAchievements> getRankedAchievements(PlayerList matchInfo);
	RankUpdateDetails processMatchResults(MatchInfo matchInfo);
	Map<RankedMode, List<RankedPlayer>> getTopPlayers();
    RankedInfo getRankedInfo();
	List<RankedPlayer> getTopPlayers(RankedMode mode);
	void setRank(long steamId64, RankedMode mode, byte rank);
	void setElo(long steamId64, RankedMode mode, short elo);
}
