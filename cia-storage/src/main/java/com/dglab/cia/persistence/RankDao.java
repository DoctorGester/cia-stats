package com.dglab.cia.persistence;

import com.dglab.cia.RankedMode;
import com.dglab.cia.database.PlayerMatchData;
import com.dglab.cia.database.PlayerRank;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import java.util.*;

/**
 * @author doc
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public class RankDao {
	@PersistenceContext
	private EntityManager entityManager;

	public Collection<PlayerRank> findPlayerRanks(long steamId64) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<PlayerRank> query = builder.createQuery(PlayerRank.class);
		EntityType<PlayerRank> entity = entityManager.getMetamodel().entity(PlayerRank.class);

		Root<PlayerRank> root = query.from(entity);
		query.select(root);
		query.where(builder.equal(root.get("pk").get("steamId64"), steamId64));

		return entityManager.createQuery(query).getResultList();
	}

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
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<PlayerMatchData> query = builder.createQuery(PlayerMatchData.class);

		Root<PlayerMatchData> matchDataRoot = query.from(PlayerMatchData.class);
		query.select(matchDataRoot);
		query.where(builder.equal(matchDataRoot.get("pk").get("steamId64"), steamId64));

		if (entityManager.createQuery(query).getResultList().isEmpty()) {
			return null;
		}

		PlayerRank.Pk pk = new PlayerRank.Pk();
		pk.setSteamId64(steamId64);
		pk.setSeason(season);
		pk.setMode(mode);

		PlayerRank rank = entityManager.find(PlayerRank.class, pk);

		if (rank == null) {
			rank = new PlayerRank();
			rank.setPk(pk);
			rank.setStars(mode.getStars());
			rank.setRank((byte) 30);

			save(rank);
		}

		return rank;
	}

	public List<PlayerRank> findTopPlayers(byte season, RankedMode mode, int amount) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<PlayerRank> query = builder.createQuery(PlayerRank.class);
		EntityType<PlayerRank> entity = entityManager.getMetamodel().entity(PlayerRank.class);

		Root<PlayerRank> root = query.from(entity);
		query.select(root);
        query.where(
                builder.and(
                        builder.equal(root.get("pk").get("season"), season),
                        builder.equal(root.get("pk").get("mode"), mode)
                )
        );

		query.orderBy(builder.asc(root.get("rank")));

		return entityManager.createQuery(query).setMaxResults(amount).getResultList();
	}

    public Map<RankedMode, List<PlayerRank>> findTopPlayers(byte season, int amount) {
        Map<RankedMode, List<PlayerRank>> result = new HashMap<>();

        for (RankedMode rankedMode : RankedMode.values()) {
            List<PlayerRank> topPlayers = findTopPlayers(season, rankedMode, amount);

            if (!topPlayers.isEmpty()) {
                result.put(rankedMode, topPlayers);
            }
        }

        return result;
    }

	public void save(PlayerRank rank) {
		entityManager.merge(rank);
	}
}
