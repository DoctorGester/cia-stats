package com.dglab.cia.persistence;

import com.dglab.cia.json.HeroWinRateAndGames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author doc
 */
@Service
public class StatsServiceImpl implements StatsService {
    private static Logger log = LoggerFactory.getLogger(StatsServiceImpl.class);
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

	@Autowired
	private StatsDao statsDao;

    @PostConstruct
    private void start() {
        long hours = computeNextDelay().toHours();
        if (hours > 2) {
            executorService.schedule(() -> {
                statsDao.recalculateStats();
                scheduleExecution();
            }, 1, TimeUnit.MINUTES);

            log.info("{} hours to next stats recalculation, scheduling in 1 minute", hours);
        } else {
            scheduleExecution();
        }
    }

    private void scheduleExecution() {
        Duration duration = computeNextDelay();
        long delay = duration.getSeconds();
        executorService.schedule(() -> {
            statsDao.recalculateStats();
            scheduleExecution();
        }, delay, TimeUnit.SECONDS);

        log.info("Stats recalculation scheduled in {} hours", duration.toHours());
    }

    private Duration computeNextDelay() {
        LocalDateTime localNow = LocalDateTime.now();
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, ZoneOffset.UTC);
        ZonedDateTime zonedNextTarget = zonedNow.withHour(0).withMinute(0).withSecond(0);

        if(zonedNow.compareTo(zonedNextTarget) > 0)
            zonedNextTarget = zonedNextTarget.plusDays(1);

        return Duration.between(zonedNow, zonedNextTarget);
    }

	@Override
	public List<HeroWinRateAndGames> getGeneralWinRates() {
		return statsDao.getHeroWinRates();
	}
}
