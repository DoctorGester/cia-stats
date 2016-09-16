package com.dglab.cia.persistence;

import com.dglab.cia.json.PassQuest;

import java.util.List;

/**
 * @author doc
 */
public interface QuestService {
    List<PassQuest> getPlayerQuests(long steamId64);
    PassQuest rerollQuest(long quest);
    void updateQuestProgress(long quest, short progress);
}