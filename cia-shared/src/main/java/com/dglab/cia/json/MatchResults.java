package com.dglab.cia.json;

import java.util.Map;

/**
 * @author doc
 */
public class MatchResults {
    private RankUpdateDetails rankDetails;
    private Map<Long, PlayerQuestResult> questResults;

    public RankUpdateDetails getRankDetails() {
        return rankDetails;
    }

    public void setRankDetails(RankUpdateDetails rankDetails) {
        this.rankDetails = rankDetails;
    }

    public Map<Long, PlayerQuestResult> getQuestResults() {
        return questResults;
    }

    public void setQuestResults(Map<Long, PlayerQuestResult> questResults) {
        this.questResults = questResults;
    }
}
