package com.dglab.cia.persistence;

import com.dglab.cia.database.Match;
import com.dglab.cia.database.PlayerMatchData;
import com.dglab.cia.database.PlayerRoundData;
import com.dglab.cia.database.Round;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import java.util.Collection;

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

		for (PlayerMatchData playerMatchData : match.getMatchData()) {
			entityManager.merge(playerMatchData);
		}
	}

	public void putRound(Round round) {
		entityManager.merge(round);

		for (PlayerRoundData playerRoundData : round.getPlayerRoundData()) {
			entityManager.merge(playerRoundData);
		}
	}

	public Match getMatch(long id) {
		Match match = entityManager.find(Match.class, id);
		Hibernate.initialize(match.getMatchData());
		Hibernate.initialize(match.getRounds());

		return match;
	}

	public void save(Match match) {
		entityManager.merge(match);
	}
}
