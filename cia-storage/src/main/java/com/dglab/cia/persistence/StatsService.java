package com.dglab.cia.persistence;

import com.dglab.cia.database.HeroWinRateKey;
import com.dglab.cia.json.HeroWinRateAndGames;

import java.util.List;

/**
 * @author doc
 */
public interface StatsService {
    void incrementHeroStat(HeroWinRateKey key, boolean won);
	List<HeroWinRateAndGames> getGeneralWinRates();
    List<HeroWinRateAndGames> getRankOneWinRates();
}
