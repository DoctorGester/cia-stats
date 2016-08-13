package com.dglab.cia.persistence;

import com.dglab.cia.database.*;
import com.dglab.cia.json.HeroWinRateAndGames;
import com.dglab.cia.json.MatchDateCount;
import com.dglab.cia.json.MatchMap;
import com.dglab.cia.json.RankRange;
import org.apache.commons.lang.NotImplementedException;
import org.hibernate.*;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.LocalDateType;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.EntityType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author doc
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public class StatsDao {
    private static final Logger log = LoggerFactory.getLogger(StatsDao.class);

	@PersistenceContext
	private EntityManager entityManager;

    private void increaseHeroGames(String hero, Match match, RankRange rankRange, Map<HeroWinRateKey, Integer> map) {
        HeroWinRateKey key = new HeroWinRateKey();
        key.setHeroName(hero);
        key.setMap(match.getMap());
        key.setMode(match.getMode());
        key.setPlayers(match.getPlayers());
        key.setRankRange(rankRange);

        Integer integer = map.get(key);

        if (integer == null) {
            integer = 0;
        }

        map.put(key, ++integer);
    }

    // Vendor-locked
    public void recalculateRankOneWinRates() {
        Session session = entityManager.unwrap(Session.class);
        Query query = session
                .createSQLQuery(
                    "select m.* from player_ranks as pr\n" +
                    "join player_match_data as pmd on pmd.steamId64 = pr.steamId64\n" +
                    "join matches as m " +
                            "on m.matchid = pmd.matchid " +
                            "and playerAmount > 1 " +
                            "and datetime >= current_date - cast('7 day' as INTERVAL)\n" +
                    "where pr.\"RANK\" = '1'\n" +
                    "group by m.matchid"
                ).addEntity(Match.class);

        query.setReadOnly(true);

        recalculateWinRates(session, query.scroll(ScrollMode.FORWARD_ONLY), RankRange.RANK_ONE);
    }

    public void recalculateAllWinRates() {
        Session session = entityManager.unwrap(Session.class);

        Criteria criteria = session
                .createCriteria(Match.class)
                .add(Restrictions.or(
                        Restrictions.eq("players", (byte) 2),
                        Restrictions.eq("players", (byte) 4),
                        Restrictions.eq("players", (byte) 6)
                ))
                .add(Restrictions.gt("dateTime", Instant.now().minus(7, ChronoUnit.DAYS)))
                .setReadOnly(true);

        recalculateWinRates(session, criteria.scroll(ScrollMode.FORWARD_ONLY), RankRange.ALL);
    }

    public void recalculateWinRates(Session session, ScrollableResults results, RankRange rankRange) {
        log.info("Started calculating win-rates");

        Map<HeroWinRateKey, Integer> heroWins = new HashMap<>();
        Map<HeroWinRateKey, Integer> heroLosses = new HashMap<>();

        int count = 0;

        while (results.next()) {
            Match match = (Match) results.get()[0];

            Hibernate.initialize(match.getMatchData());
            Hibernate.initialize(match.getRounds());

            Map<Long, Byte> playerTeams = match.getMatchData()
                    .stream()
                    .collect(Collectors.toMap(
                            p -> p.getPk().getSteamId64(), PlayerMatchData::getTeam
                    ));

            for (Round round : match.getRounds()) {
                if (round.getWinner() == null) {
                    continue;
                }

                Hibernate.initialize(round.getPlayerRoundData());

                for (PlayerRoundData roundData : round.getPlayerRoundData()) {
                    if (roundData.getHero() == null) {
                        continue;
                    }

                    Byte team = playerTeams.get(roundData.getPk().getSteamId64());
                    String hero = roundData.getHero();

                    if (Objects.equals(round.getWinner(), team)) {
                        increaseHeroGames(hero, match, rankRange, heroWins);
                    } else {
                        increaseHeroGames(hero, match, rankRange, heroLosses);
                    }

                    session.evict(roundData);
                }

                session.evict(round);
            }

            match.getMatchData().forEach(session::evict);
            session.evict(match);

            if (++count > 0 && count % 1000 == 0) {
                log.info("Processed {} matches", count);
            }
        }

        for (HeroWinRateKey key : heroWins.keySet()) {
            Integer wins = heroWins.get(key);
            Integer losses = heroLosses.get(key);

            if (losses == null) {
                losses = 0;
            }

            HeroWinRate winRate = new HeroWinRate();
            winRate.setPk(key);
            winRate.setWinRate(wins / (float) (wins + losses));
            winRate.setGames(wins + losses);

            entityManager.merge(winRate);
        }

        log.info("Finished calculating win-rates");
    }

    private <T, N> Predicate createRestriction(CriteriaBuilder b, Root<N> root, String property, Collection<T> values) {
        return b.or(
                values.stream().map(
                        mode -> b.equal(root.get("pk").get(property), mode)
                ).toArray(Predicate[]::new));
    }

    public List<HeroWinRateAndGames> getHeroWinRates(
            Collection<String> modes,
            Collection<Byte> playerAmounts,
            Collection<MatchMap> maps) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<HeroWinRateAndGames> query = builder.createQuery(HeroWinRateAndGames.class);
        EntityType<HeroWinRate> entity = entityManager.getMetamodel().entity(HeroWinRate.class);
        Root<HeroWinRate> root = query.from(entity);

        List<Predicate> restrictions = new ArrayList<>();

        if (modes != null) {
            restrictions.add(createRestriction(builder, root, "mode", modes));
        }

        if (playerAmounts != null) {
            restrictions.add(createRestriction(builder, root, "players", playerAmounts));
        }

        if (maps != null) {
            restrictions.add(createRestriction(builder, root, "map", playerAmounts));
        }

        return getHeroWinRates(builder, query, root, restrictions);
    }

    public List<HeroWinRateAndGames> getHeroWinRates(RankRange range) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<HeroWinRateAndGames> query = builder.createQuery(HeroWinRateAndGames.class);
        EntityType<HeroWinRate> entity = entityManager.getMetamodel().entity(HeroWinRate.class);
        Root<HeroWinRate> root = query.from(entity);

        return getHeroWinRates(builder, query, root, Arrays.asList(
                builder.equal(root.get("pk").get("rankRange"), range),
                builder.or(
                    builder.and(
                            builder.equal(root.get("pk").get("mode"), "2v2"),
                            builder.equal(root.get("pk").get("players"), 4)
                    ),
                    builder.and(
                            builder.equal(root.get("pk").get("mode"), "3v3"),
                            builder.equal(root.get("pk").get("players"), 6)
                    )
                )
        ));
    }

    private List<HeroWinRateAndGames> getHeroWinRates(
            CriteriaBuilder builder,
            CriteriaQuery<HeroWinRateAndGames> query,
            Root<HeroWinRate> root,
            Collection<Predicate> restrictions
    ) {
        query.multiselect(
                root.get("pk").get("heroName"),
                builder.avg(root.get("winRate")),
                builder.sum(root.get("games"))
        );

        if (restrictions != null && !restrictions.isEmpty()) {
            query.where(restrictions.toArray(new Predicate[0]));
        }

        query.groupBy(root.get("pk").get("heroName"));

        return entityManager.createQuery(query).getResultList();
    }

    public void recalculateTodayGameCounts() {
        LocalDate today = ZonedDateTime
                .now(ZoneOffset.UTC)
                .withMonth(7)
                .withDayOfMonth(16)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .toLocalDate();

        Map<MatchKey, Integer> matches = new HashMap<>();
        Session session = entityManager.unwrap(Session.class);

        // Vendor-locked (hibernate + postgres)
        ScrollableResults results = session
                .createCriteria(Match.class)
                .add(Restrictions.gt("players", (byte) 1))
                .add(Restrictions.sqlRestriction("dateTime::DATE = ?", today, LocalDateType.INSTANCE))
                .setReadOnly(true)
                .scroll(ScrollMode.FORWARD_ONLY);

        while (results.next()) {
            Match match = (Match) results.get()[0];

            MatchKey key = new MatchKey();
            key.setMap(match.getMap());
            key.setMode(match.getMode());
            key.setPlayers(match.getPlayers());

            Integer integer = matches.get(key);

            if (integer == null) {
                integer = 0;
            }

            matches.put(key, ++integer);
            session.evict(match);
        }

        matches.forEach((key, amount) -> {
            MatchCount matchCount = new MatchCount();
            matchCount.setPk(key);
            matchCount.setMatches(amount);

            entityManager.merge(matchCount);
        });
    }

    public List<MatchCount> getLastMatchCounts() {
        throw new NotImplementedException();
    }
}
