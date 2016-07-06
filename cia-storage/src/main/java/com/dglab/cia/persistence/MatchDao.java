package com.dglab.cia.persistence;

import com.dglab.cia.database.Match;
import com.dglab.cia.database.PlayerMatchData;
import com.dglab.cia.database.Round;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

/**
 * @author doc
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public class MatchDao {
	@PersistenceContext
	private EntityManager entityManager;

	public void putMatch(Match match) {
		entityManager.merge(match);
	}

	public void putRound(Round round) {
		entityManager.merge(round);
	}

	public Match getMatch(long id) {
		Match match = entityManager.find(Match.class, id);

		if (match != null) {
			Hibernate.initialize(match.getMatchData());
			Hibernate.initialize(match.getRounds());

			for (Round round : match.getRounds()) {
				Hibernate.initialize(round.getPlayerRoundData());
			}
		}

		return match;
	}

    public List<Match> getRecentPlayerMatches(long steamId64, int amount) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Match> query = builder.createQuery(Match.class);
        EntityType<Match> entity = entityManager.getMetamodel().entity(Match.class);
        entity.getCollection("matchData");

        Root<Match> root = query.from(entity);

        query.select(root);
        Join<Match, PlayerMatchData> join = root.join(entity.getCollection("matchData", PlayerMatchData.class));
        query.where(builder.equal(join.get("pk").get("steamId64"), steamId64));
        query.orderBy(builder.desc(root.get("dateTime")));

        return entityManager.createQuery(query).setMaxResults(amount).getResultList();
    }

	public void save(Match match) {
		entityManager.merge(match);
	}
}
