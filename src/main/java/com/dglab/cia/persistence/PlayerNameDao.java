package com.dglab.cia.persistence;

import com.dglab.cia.database.*;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author doc
 */
@Repository
public class PlayerNameDao {
	@Autowired
	private SessionFactory sessionFactory;

	public void update(PlayerName playerName) {
		sessionFactory.getCurrentSession().saveOrUpdate(playerName);
	}
}
