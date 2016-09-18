package com.dglab.cia.persistence;

import com.dglab.cia.json.PassQuest;
import com.dglab.cia.json.PlayerQuestResult;

import java.util.List;
import java.util.Map;

/**
 * @author doc
 */
public interface QuestService {
    List<PassQuest> updatePlayerQuests(long steamId64);
    PassQuest rerollQuest(long quest);
    Map<Long, PlayerQuestResult> updateQuestBatch(Map<Long, Integer> progress);
    void resetRerolls();
}