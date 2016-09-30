package com.dglab.cia.json;

import java.util.List;
import java.util.Map;

/**
 * @author doc
 */
public class QuestProgressReport {
    private Map<Long, Integer> questProgress;
    private List<Long> passPlayers;
    private int gameLength;

    public Map<Long, Integer> getQuestProgress() {
        return questProgress;
    }

    public List<Long> getPassPlayers() {
        return passPlayers;
    }

    public int getGameLength() {
        return gameLength;
    }
}
