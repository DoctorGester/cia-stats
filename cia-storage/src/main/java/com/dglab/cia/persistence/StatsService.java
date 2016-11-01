package com.dglab.cia.persistence;

import com.dglab.cia.database.HeroWinRateKey;
import com.dglab.cia.json.HeroWinRateAndGames;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author doc
 */
public interface StatsService {
    void incrementHeroStat(HeroWinRateKey key, boolean won);
    Map<LocalDate, HeroWinRateAndGames> getHeroWinRatePerDay(String hero);
    List<HeroWinRateAndGames> getGeneralWinRates();
    List<HeroWinRateAndGames> getRankOneWinRates();
}
