package com.dglab.cia.persistence;

import com.dglab.cia.database.HeroWinRate;
import com.dglab.cia.database.HeroWinRateKey;
import com.dglab.cia.json.HeroWinRateAndGames;
import com.dglab.cia.json.RankRange;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static com.dglab.cia.database.QHeroWinRate.*;

/**
 * @author doc
 */
@Service
public class StatsServiceImpl implements StatsService {
    private static Logger log = LoggerFactory.getLogger(StatsServiceImpl.class);

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private HeroWinRateRepository winRateRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void incrementHeroStat(HeroWinRateKey key, boolean won) {
        HeroWinRate heroWinRate = winRateRepository.findOne(key);

        if (heroWinRate == null) {
            heroWinRate = new HeroWinRate();
            heroWinRate.setPk(key);
        }

        heroWinRate.setGames(heroWinRate.getGames() + 1);

        if (won) {
            heroWinRate.setWins(heroWinRate.getWins() + 1);
        }

        winRateRepository.save(heroWinRate);
    }

    private List<HeroWinRateAndGames> getHeroWinRates(RankRange rankRange) {
        BooleanExpression twoVersusTwo = heroWinRate.pk.mode.eq("2v2").and(heroWinRate.pk.players.eq((byte) 4));
        BooleanExpression threeVersusThree = heroWinRate.pk.mode.eq("3v3").and(heroWinRate.pk.players.eq((byte) 6));
        BooleanExpression dateAfter = heroWinRate.pk.date.after(LocalDate.now(Clock.systemUTC()).minusDays(7));

        List<Tuple> result = new JPAQuery<HeroWinRate>(entityManager)
                .select(heroWinRate.games.sum(), heroWinRate.wins.sum(), heroWinRate.pk.heroName)
                .from(heroWinRate)
                .where(dateAfter.and(twoVersusTwo.or(threeVersusThree)).and(heroWinRate.pk.rankRange.eq(rankRange)))
                .groupBy(heroWinRate.pk.heroName)
                .fetch();

        return result.stream().map(tuple -> {
            Integer games = tuple.get(heroWinRate.games);
            Integer wins = tuple.get(heroWinRate.wins);

            if (games == null || wins == null) {
                return null;
            }

            return new HeroWinRateAndGames(tuple.get(heroWinRate.pk.heroName), (double) games / wins, wins);
        }).collect(Collectors.toList());
    }

    @Override
	public List<HeroWinRateAndGames> getGeneralWinRates() {
		return getHeroWinRates(RankRange.ALL);
	}

    @Override
    public List<HeroWinRateAndGames> getRankOneWinRates() {
        return getHeroWinRates(RankRange.RANK_ONE);
    }
}
