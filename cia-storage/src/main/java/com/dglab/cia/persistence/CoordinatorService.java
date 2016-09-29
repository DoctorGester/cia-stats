package com.dglab.cia.persistence;

import com.dglab.cia.json.Achievements;
import com.dglab.cia.json.MatchInfo;

/**
 * @author doc
 */
public interface CoordinatorService {
    Achievements getAchievements(MatchInfo matchInfo);
}
