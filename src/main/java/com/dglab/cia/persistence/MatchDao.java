package com.dglab.cia.persistence;

import com.dglab.cia.database.Match;
import com.dglab.cia.database.PlayerMatchData;
import com.dglab.cia.database.PlayerRoundData;
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

	public void putRound(Collection<PlayerRoundData> roundData) {
		for (PlayerRoundData playerRoundData : roundData) {
			sessionFactory.getCurrentSession().saveOrUpdate(roundData);
		}
	}

	public Match getMatch(long id) {
		return sessionFactory.getCurrentSession().byId(Match.class).load(id);
	}
}
