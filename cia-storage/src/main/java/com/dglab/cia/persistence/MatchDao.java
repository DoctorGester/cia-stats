package com.dglab.cia.persistence;

import com.dglab.cia.database.Match;
import com.dglab.cia.database.PlayerMatchData;
import com.dglab.cia.database.Round;
import org.hibernate.*;
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
import java.util.List;

/**
 * @author doc
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public class MatchDao {
    private static final Logger log = LoggerFactory.getLogger(MatchDao.class);

	@PersistenceContext
	private EntityManager entityManager;

	public void putMatch(Match match) {
		entityManager.merge(match);
	}

	public void putRound(Round round) {
		entityManager.merge(round);
	}

	public Match getMatch(long id) {
        return entityManager.find(Match.class, id);
	}

	public long getMatchCount(long steamId64) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);

        Root<PlayerMatchData> root = query.from(PlayerMatchData.class);
        query.select(builder.count(root));
		query.where(builder.equal(root.get("pk").get("steamId64"), steamId64));

        return entityManager.createQuery(query).getSingleResult();
	}

    public List<Match> getRecentPlayerMatches(long steamId64, int amount) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Match> query = builder.createQuery(Match.class);
        EntityType<Match> entity = entityManager.getMetamodel().entity(Match.class);

        Root<Match> root = query.from(entity);
        query.select(root);
        Join<Match, PlayerMatchData> join = root.join(entity.getCollection("matchData", PlayerMatchData.class));
        query.where(builder.equal(join.get("pk").get("steamId64"), steamId64));
        query.orderBy(builder.desc(root.get("dateTime")));

        return entityManager.createQuery(query).setMaxResults(amount).getResultList();
    }

	public List<Match> getPlayerMatchesInADay(long steamId64) {
		Instant lastDay = Instant.now().minus(1, ChronoUnit.DAYS);

		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Match> query = builder.createQuery(Match.class);
		EntityType<Match> entity = entityManager.getMetamodel().entity(Match.class);
		Root<Match> root = query.from(entity);
		query.select(root);
		Join<Match, PlayerMatchData> join = root.join(entity.getCollection("matchData", PlayerMatchData.class));
		query.where(
				builder.and(
						builder.equal(join.get("pk").get("steamId64"), steamId64),
						builder.greaterThan(root.get("dateTime"), lastDay)
				)
		);
		query.orderBy(builder.desc(root.get("dateTime")));

		List<Match> resultList = entityManager.createQuery(query).getResultList();

		resultList.stream().map(Match::getMatchData).forEach(Hibernate::initialize);

		return resultList;
	}

	public void save(Match match) {
		entityManager.merge(match);
	}
}
