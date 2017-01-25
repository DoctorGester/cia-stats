package com.dglab.cia.persistence;

import com.dglab.cia.database.HeroWinRate;
import com.dglab.cia.database.HeroWinRateKey;
import com.dglab.cia.database.PlayerHeroWinRate;
import com.dglab.cia.database.PlayerHeroWinRateKey;
import com.dglab.cia.json.HeroWinRateAndGames;
import com.dglab.cia.json.MatchMap;
import com.dglab.cia.json.PlayerHeroWinRateAndGames;
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
import java.util.Map;
import java.util.stream.Collectors;

import static com.dglab.cia.database.QHeroWinRate.*;
import static com.dglab.cia.database.QPlayerHeroWinRate.*;
import static com.dglab.cia.database.QPlayerName.*;

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

    @Autowired
    private PlayerHeroWinRateRepository playerHeroWinRateRepository;

    @Autowired
    private PlayerNameRepository playerNameRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void incrementHeroStat(long steamId64, HeroWinRateKey key, boolean won) {
        PlayerHeroWinRateKey playerKey = new PlayerHeroWinRateKey();
        playerKey.setHeroName(key.getHeroName());
        playerKey.setMap(key.getMap());
        playerKey.setMode(key.getMode());
        playerKey.setPlayers(key.getPlayers());
        playerKey.setSteamId64(steamId64);

        HeroWinRate heroWinRate = winRateRepository.findOne(key);
        PlayerHeroWinRate playerHeroWinRate = playerHeroWinRateRepository.findOne(playerKey);

        if (heroWinRate == null) {
            heroWinRate = new HeroWinRate();
            heroWinRate.setPk(key);
        }

        if (playerHeroWinRate == null) {
            playerHeroWinRate = new PlayerHeroWinRate();
            playerHeroWinRate.setPk(playerKey);
        }

        heroWinRate.setGames(heroWinRate.getGames() + 1);
        playerHeroWinRate.setGames(playerHeroWinRate.getGames() + 1);

        if (won) {
            heroWinRate.setWins(heroWinRate.getWins() + 1);
            playerHeroWinRate.setWins(playerHeroWinRate.getWins() + 1);
        }

        playerHeroWinRate.setWinrate((double) playerHeroWinRate.getWins() / playerHeroWinRate.getGames());

        winRateRepository.save(heroWinRate);
        playerHeroWinRateRepository.save(playerHeroWinRate);
    }

    private BooleanExpression defaultMatchFilters(int days) {
        BooleanExpression twoVersusTwo = heroWinRate.pk.mode.eq("2v2").and(heroWinRate.pk.players.eq((byte) 4));
        BooleanExpression threeVersusThree = heroWinRate.pk.mode.eq("3v3").and(heroWinRate.pk.players.eq((byte) 6));
        BooleanExpression dateAfter = heroWinRate.pk.date.after(LocalDate.now(Clock.systemUTC()).minusDays(days));

        return dateAfter.and(twoVersusTwo.or(threeVersusThree));
    }

    private BooleanExpression duelMatchFilters(int days) {
        BooleanExpression oneVersusOne =
                heroWinRate.pk.mode.eq("ffa")
                .and(heroWinRate.pk.players.eq((byte) 2))
                .and(heroWinRate.pk.map.eq(MatchMap.RANKED_2));

        BooleanExpression dateAfter = heroWinRate.pk.date.after(LocalDate.now(Clock.systemUTC()).minusDays(days));

        return dateAfter.and(oneVersusOne);
    }

    private List<HeroWinRateAndGames> getHeroWinRates(RankRange rankRange, BooleanExpression filter) {
        List<Tuple> result = new JPAQuery<HeroWinRate>(entityManager)
                .select(heroWinRate.games.sum(), heroWinRate.wins.sum(), heroWinRate.pk.heroName)
                .from(heroWinRate)
                .where(filter.and(heroWinRate.pk.rankRange.eq(rankRange)))
                .groupBy(heroWinRate.pk.heroName)
                .fetch();

        return result.stream().map(tuple -> {
            Integer games = tuple.get(heroWinRate.games.sum());
            Integer wins = tuple.get(heroWinRate.wins.sum());

            if (games == null || wins == null) {
                return null;
            }

            return new HeroWinRateAndGames(tuple.get(heroWinRate.pk.heroName), (double) wins / games, wins);
        }).collect(Collectors.toList());
    }

    @Override
    public Map<LocalDate, HeroWinRateAndGames> getHeroWinRatePerDay(String hero) {
        if (hero == null) {
            return null;
        }

        List<Tuple> result = new JPAQuery<HeroWinRate>(entityManager)
                .select(heroWinRate.games.sum(), heroWinRate.wins.sum(), heroWinRate.pk.date)
                .from(heroWinRate)
                .where(defaultMatchFilters(30).and(heroWinRate.pk.heroName.eq("npc_dota_hero_" + hero)))
                .groupBy(heroWinRate.pk.date)
                .fetch();

        return result.stream().collect(Collectors.toMap(t -> t.get(heroWinRate.pk.date), t -> {
            Integer games = t.get(heroWinRate.games.sum());
            Integer wins = t.get(heroWinRate.wins.sum());

            if (games == null || wins == null) {
                return null;
            }

            return new HeroWinRateAndGames(hero, (double) wins / games, games);
        }));
    }

    @Override
    public List<PlayerHeroWinRateAndGames> getPlayerHeroWinRate(String hero) {
        if (hero == null) {
            return null;
        }

        BooleanExpression oneVersusOne = playerHeroWinRate.pk.mode.eq("ffa").and(playerHeroWinRate.pk.players.eq((byte) 2));
        BooleanExpression twoVersusTwo = playerHeroWinRate.pk.mode.eq("2v2").and(playerHeroWinRate.pk.players.eq((byte) 4));
        BooleanExpression threeVersusThree = playerHeroWinRate.pk.mode.eq("3v3").and(playerHeroWinRate.pk.players.eq((byte) 6));

        BooleanExpression relevantMatch = oneVersusOne.or(twoVersusTwo).or(threeVersusThree);

        List<Tuple> result = new JPAQuery<PlayerHeroWinRate>(entityManager)
                .select(
                        playerHeroWinRate.games.sum(),
                        playerHeroWinRate.wins.sum(),
                        playerHeroWinRate.winrate.avg(),
                        playerHeroWinRate.pk.steamId64,
                        playerName.name,
                        playerName.avatarUrl
                )
                .from(playerHeroWinRate)
                .innerJoin(playerHeroWinRate.name, playerName)
                .where(playerHeroWinRate.pk.heroName.eq("npc_dota_hero_" + hero)
                        .and(relevantMatch)
                        .and(playerHeroWinRate.games.gt(10))
                        .and(playerName.steamId64.eq(playerHeroWinRate.pk.steamId64))
                )
                .orderBy(playerHeroWinRate.games.sum().desc(), playerHeroWinRate.winrate.avg().desc())
                .groupBy(playerHeroWinRate.pk.steamId64, playerName.steamId64)
                .distinct()
                .limit(3)
                .fetch();

        return result.stream().map(t -> {
            Long steamId64 = t.get(playerHeroWinRate.pk.steamId64);
            Integer games = t.get(playerHeroWinRate.games.sum());
            Integer wins = t.get(playerHeroWinRate.wins.sum());

            if (games == null || wins == null || steamId64 == null) {
                return null;
            }

            HeroWinRateAndGames winRateAndGames = new HeroWinRateAndGames(hero, (double) wins / games, games);

            return new PlayerHeroWinRateAndGames(
                    steamId64,
                    t.get(playerName.name),
                    t.get(playerName.avatarUrl),
                    winRateAndGames
            );
        }).collect(Collectors.toList());
    }

    @Override
	public List<HeroWinRateAndGames> getGeneralWinRates() {
		return getHeroWinRates(RankRange.ALL, defaultMatchFilters(7));
	}

    @Override
    public List<HeroWinRateAndGames> getRankOneWinRates() {
        return getHeroWinRates(RankRange.RANK_ONE, defaultMatchFilters(7));
    }

    @Override
    public List<HeroWinRateAndGames> getDuelWinRates() {
        return getHeroWinRates(RankRange.ALL, duelMatchFilters(7));
    }
}
