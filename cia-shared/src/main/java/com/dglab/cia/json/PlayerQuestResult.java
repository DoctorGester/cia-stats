package com.dglab.cia.json;

import java.util.List;

/**
 * @author doc
 */
public class PlayerQuestResult {
    private List<PassQuest> completedQuests;
    private int experience;
    private int earnedExperience;

    public List<PassQuest> getCompletedQuests() {
        return completedQuests;
    }

    public void setCompletedQuests(List<PassQuest> completedQuests) {
        this.completedQuests = completedQuests;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getEarnedExperience() {
        return earnedExperience;
    }

    public void setEarnedExperience(int earnedExperience) {
        this.earnedExperience = earnedExperience;
    }
}
