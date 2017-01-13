package com.dglab.cia.persistence;

import com.dglab.cia.database.PassOwner;
import com.dglab.cia.json.MatchInfo;
import com.dglab.cia.json.PassPlayer;
import com.dglab.cia.json.PlayerQuestResult;
import com.dglab.cia.json.QuestProgressReport;

import java.util.List;
import java.util.Map;

/**
 * @author doc
 */
public interface PassService {
    double EXPERIENCE_PER_SECOND = 0.09;

    PassOwner get(long steamId64);
    PassOwner getOrCreate(long steamId64);
    List<PassPlayer> getTopPlayers();
    void awardExperience(long steamId64, int experience);
    Map<Long, PlayerQuestResult> processMatchUpdate(MatchInfo matchInfo);
}
