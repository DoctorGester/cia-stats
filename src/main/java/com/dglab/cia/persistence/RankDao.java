package com.dglab.cia.persistence;

import com.dglab.cia.RankedMode;
import com.dglab.cia.database.PlayerMatchData;
import com.dglab.cia.database.PlayerRank;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author doc
 */
@Repository
public class RankDao {
	@Autowired
	private SessionFactory sessionFactory;

	public Collection<PlayerRank> findPlayerRanks(long steamId64, byte season) {
		Collection<PlayerRank> result = new ArrayList<>();

		for (RankedMode rankedMode : RankedMode.values()) {
			PlayerRank rank = findPlayerRank(steamId64, season, rankedMode);

			if (rank != null) {
				result.add(rank);
			}
		}

		return result;
	}

	public PlayerRank findPlayerRank(long steamId64, byte season, RankedMode mode) {
		List playerEntries = sessionFactory
				.getCurrentSession()
				.createCriteria(PlayerMatchData.class)
				.add(Restrictions.eq("pk.steamId64", steamId64))
				.list();

		if (playerEntries.isEmpty()) {
			return null;
		}

		PlayerRank.Pk pk = new PlayerRank.Pk();
		pk.setSteamId64(steamId64);
		pk.setSeason(season);
		pk.setMode(mode);

		PlayerRank rank = sessionFactory.getCurrentSession().byId(PlayerRank.class).load(pk);

		if (rank == null) {
			rank = new PlayerRank();
			rank.setPk(pk);
			rank.setStars(mode.getStars());
			rank.setRank((byte) 30);

			sessionFactory.getCurrentSession().save(rank);
		}

		return rank;
	}

	public void save(PlayerRank rank) {
		sessionFactory.getCurrentSession().save(rank);
	}
}
