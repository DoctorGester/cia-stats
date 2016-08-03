package com.dglab.cia.persistence;

import com.dglab.cia.database.*;
import com.dglab.cia.json.HeroWinRateAndGames;
import com.dglab.cia.json.MatchMap;
import org.hibernate.*;
import org.hibernate.criterion.Restrictions;
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
import java.time.temporal.ChronoUnit;
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

    private void increaseHeroGames(String hero, Match match, Map<HeroWinRateKey, Integer> map) {
        HeroWinRateKey key = new HeroWinRateKey();
        key.setHeroName(hero);
        key.setMap(match.getMap());
        key.setMode(match.getMode());
        key.setPlayers(match.getPlayers());

        Integer integer = map.get(key);

        if (integer == null) {
            integer = 0;
        }

        map.put(key, ++integer);
    }

    public void recalculateStats() {
        log.info("Started calculating stats");

        Map<HeroWinRateKey, Integer> heroWins = new HashMap<>();
        Map<HeroWinRateKey, Integer> heroLosses = new HashMap<>();

        Session session = entityManager.unwrap(Session.class);

        // Vendor-locked
        ScrollableResults results = session
                .createCriteria(Match.class)
                .add(Restrictions.or(
                        Restrictions.eq("players", (byte) 2),
                        Restrictions.eq("players", (byte) 4),
                        Restrictions.eq("players", (byte) 6)
                ))
                .add(Restrictions.gt("dateTime", Instant.now().minus(7, ChronoUnit.DAYS)))
                .setReadOnly(true)
                .scroll(ScrollMode.FORWARD_ONLY);

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
                        increaseHeroGames(hero, match, heroWins);
                    } else {
                        increaseHeroGames(hero, match, heroLosses);
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

        log.info("Finished calculating stats");
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

    public List<HeroWinRateAndGames> getHeroWinRates() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<HeroWinRateAndGames> query = builder.createQuery(HeroWinRateAndGames.class);
        EntityType<HeroWinRate> entity = entityManager.getMetamodel().entity(HeroWinRate.class);
        Root<HeroWinRate> root = query.from(entity);

        return getHeroWinRates(builder, query, root, Collections.singleton(
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
}
