package com.dglab.cia.persistence;

import com.dglab.cia.database.Match;
import com.dglab.cia.database.PlayerMatchData;
import com.dglab.cia.database.PlayerRoundData;
import com.dglab.cia.database.Round;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * @author doc
 */
@Repository
public class MatchDao {
	@Autowired
	private SessionFactory sessionFactory;

	public void putMatch(Match match) {
		sessionFactory.getCurrentSession().saveOrUpdate(match);

		for (PlayerMatchData playerMatchData : match.getMatchData()) {
			sessionFactory.getCurrentSession().saveOrUpdate(playerMatchData);
		}
	}

	public void putRound(Round round) {
		sessionFactory.getCurrentSession().saveOrUpdate(round);

		for (PlayerRoundData playerRoundData : round.getPlayerRoundData()) {
			sessionFactory.getCurrentSession().saveOrUpdate(playerRoundData);
		}
	}

	public Match getMatch(long id) {
		return sessionFactory.getCurrentSession().byId(Match.class).load(id);
	}

	public void save(Match match) {
		sessionFactory.getCurrentSession().save(match);
	}
}
