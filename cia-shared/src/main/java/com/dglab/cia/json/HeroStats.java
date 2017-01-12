package com.dglab.cia.json;

import java.time.LocalDate;
import java.util.Map;

/**
 * @author doc
 */
public class HeroStats {
    private Map<LocalDate, HeroWinRateAndGames> heroWinRatePerDate;
    private Map<Long, HeroWinRateAndGames> heroWinRatePerPlayer;

    public Map<LocalDate, HeroWinRateAndGames> getHeroWinRatePerDate() {
        return heroWinRatePerDate;
    }

    public void setHeroWinRatePerDate(Map<LocalDate, HeroWinRateAndGames> heroWinRatePerDate) {
        this.heroWinRatePerDate = heroWinRatePerDate;
    }

    public Map<Long, HeroWinRateAndGames> getHeroWinRatePerPlayer() {
        return heroWinRatePerPlayer;
    }

    public void setHeroWinRatePerPlayer(Map<Long, HeroWinRateAndGames> heroWinRatePerPlayer) {
        this.heroWinRatePerPlayer = heroWinRatePerPlayer;
    }
}
