package com.dglab.cia.json;

import java.util.List;

/**
 * @author doc
 */
public class AllStats {
    private List<HeroWinRateAndGames> generalWinrates;

    public List<HeroWinRateAndGames> getGeneralWinrates() {
        return generalWinrates;
    }

    public void setGeneralWinrates(List<HeroWinRateAndGames> generalWinrates) {
        this.generalWinrates = generalWinrates;
    }
}
