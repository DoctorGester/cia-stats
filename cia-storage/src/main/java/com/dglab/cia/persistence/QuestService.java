package com.dglab.cia.persistence;

import com.dglab.cia.json.PassQuest;

import java.util.List;

/**
 * @author doc
 */
public interface QuestService {
    List<PassQuest> updatePlayerQuests(long steamId64);
    PassQuest rerollQuest(long quest);
    PassQuest updateQuestProgress(long questId, short progress);
    void resetRerolls();
}