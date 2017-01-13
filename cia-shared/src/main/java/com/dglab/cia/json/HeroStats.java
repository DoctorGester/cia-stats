package com.dglab.cia.json;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author doc
 */
public class HeroStats {
    private Map<LocalDate, HeroWinRateAndGames> heroWinRatePerDate;
    private List<PlayerHeroWinRateAndGames> heroWinRatePerPlayer;

    public Map<LocalDate, HeroWinRateAndGames> getHeroWinRatePerDate() {
        return heroWinRatePerDate;
    }

    public void setHeroWinRatePerDate(Map<LocalDate, HeroWinRateAndGames> heroWinRatePerDate) {
        this.heroWinRatePerDate = heroWinRatePerDate;
    }

    public List<PlayerHeroWinRateAndGames> getHeroWinRatePerPlayer() {
        return heroWinRatePerPlayer;
    }

    public void setHeroWinRatePerPlayer(List<PlayerHeroWinRateAndGames> heroWinRatePerPlayer) {
        this.heroWinRatePerPlayer = heroWinRatePerPlayer;
    }
}
