package com.dglab.cia.persistence;

import com.dglab.cia.database.Match;
import com.dglab.cia.database.Round;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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

	public void save(Match match) {
		entityManager.merge(match);
	}
}
