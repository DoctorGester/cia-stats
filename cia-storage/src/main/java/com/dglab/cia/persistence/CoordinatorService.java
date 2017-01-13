package com.dglab.cia.persistence;

import com.dglab.cia.json.Achievements;
import com.dglab.cia.json.MatchInfo;
import com.dglab.cia.json.MatchResults;
import com.dglab.cia.json.PlayerList;

/**
 * @author doc
 */
public interface CoordinatorService {
    Achievements getAchievements(PlayerList players);
    MatchResults processMatch(MatchInfo matchInfo);
}
