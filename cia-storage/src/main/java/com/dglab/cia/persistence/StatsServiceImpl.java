package com.dglab.cia.persistence;

import com.dglab.cia.json.HeroWinRateAndGames;
import com.dglab.cia.json.RankRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author doc
 */
@Service
public class StatsServiceImpl implements StatsService {
    private static Logger log = LoggerFactory.getLogger(StatsServiceImpl.class);

	@Autowired
	private StatsDao statsDao;

    // Every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    @Async
    public void runAllWinRatesRecalculation() {
        statsDao.recalculateAllWinRates();
    }

    // Every day at 10 minutes past midnight
    @Scheduled(cron = "0 10 0 * * *")
    @Async
    public void runRankOneWinRatesRecalculation() {
        statsDao.recalculateRankOneWinRates();
    }

	@Override
	public List<HeroWinRateAndGames> getGeneralWinRates() {
		return statsDao.getHeroWinRates(RankRange.ALL);
	}

    @Override
    public List<HeroWinRateAndGames> getRankOneWinRates() {
        return statsDao.getHeroWinRates(RankRange.RANK_ONE);
    }
}
