package com.dglab.cia.json;

import java.util.List;

/**
 * @author doc
 */
public class AllStats {
    private List<HeroWinRateAndGames> generalWinrates;
    private List<HeroWinRateAndGames> rankOneWinrates;
    private List<HeroWinRateAndGames> duelWinrates;

    public List<HeroWinRateAndGames> getRankOneWinrates() {
        return rankOneWinrates;
    }

    public void setRankOneWinrates(List<HeroWinRateAndGames> rankOneWinrates) {
        this.rankOneWinrates = rankOneWinrates;
    }

    public List<HeroWinRateAndGames> getGeneralWinrates() {
        return generalWinrates;
    }

    public void setGeneralWinrates(List<HeroWinRateAndGames> generalWinrates) {
        this.generalWinrates = generalWinrates;
    }

    public List<HeroWinRateAndGames> getDuelWinrates() {
        return duelWinrates;
    }

    public void setDuelWinrates(List<HeroWinRateAndGames> duelWinrates) {
        this.duelWinrates = duelWinrates;
    }
}
