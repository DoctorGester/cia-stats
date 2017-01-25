package com.dglab.cia.persistence;

import com.dglab.cia.database.HeroWinRateKey;
import com.dglab.cia.json.HeroWinRateAndGames;
import com.dglab.cia.json.PlayerHeroWinRateAndGames;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author doc
 */
public interface StatsService {
    void incrementHeroStat(long steamId64, HeroWinRateKey key, boolean won);
    Map<LocalDate, HeroWinRateAndGames> getHeroWinRatePerDay(String hero);
    List<PlayerHeroWinRateAndGames> getPlayerHeroWinRate(String hero);
    List<HeroWinRateAndGames> getGeneralWinRates();
    List<HeroWinRateAndGames> getRankOneWinRates();
    List<HeroWinRateAndGames> getDuelWinRates();
}
