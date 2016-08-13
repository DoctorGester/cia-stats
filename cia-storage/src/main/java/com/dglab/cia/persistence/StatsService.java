package com.dglab.cia.persistence;

import com.dglab.cia.json.HeroWinRateAndGames;

import java.util.List;
import java.util.Map;

/**
 * @author doc
 */
public interface StatsService {
	List<HeroWinRateAndGames> getGeneralWinRates();
    List<HeroWinRateAndGames> getRankOneWinRates();
    void runAllWinRatesRecalculation();
    void runRankOneWinRatesRecalculation();
}
