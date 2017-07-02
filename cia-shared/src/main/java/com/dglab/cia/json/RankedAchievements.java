package com.dglab.cia.json;

import java.util.Collection;

/**
 * @author doc
 */
public class RankedAchievements {
    private Collection<Integer> achievedSeasons;
    private Collection<String> achievedMasteryHeroes;
    private boolean wasTopPlayer;

    public Collection<Integer> getAchievedSeasons() {
        return achievedSeasons;
    }

    public void setAchievedSeasons(Collection<Integer> achievedSeasons) {
        this.achievedSeasons = achievedSeasons;
    }

    public boolean wasTopPlayer() {
        return wasTopPlayer;
    }

    public void setWasTopPlayer(boolean wasTopPlayer) {
        this.wasTopPlayer = wasTopPlayer;
    }

    public Collection<String> getAchievedMasteryHeroes() {
        return achievedMasteryHeroes;
    }

    public void setAchievedMasteryHeroes(Collection<String> achievedMasteryHeroes) {
        this.achievedMasteryHeroes = achievedMasteryHeroes;
    }
}
