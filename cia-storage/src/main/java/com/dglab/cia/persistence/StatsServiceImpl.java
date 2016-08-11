package com.dglab.cia.persistence;

import com.dglab.cia.json.HeroWinRateAndGames;
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
    private void winRatesTask() {
        statsDao.recalculateWinRates();
    }

    // Every 10 minutes
    @Scheduled(cron = "0 */10 * * * *")
    private void gameCountsTask() {
        statsDao.recalculateTodayGameCounts();
    }

	@Override
	public List<HeroWinRateAndGames> getGeneralWinRates() {
		return statsDao.getHeroWinRates();
	}
}
