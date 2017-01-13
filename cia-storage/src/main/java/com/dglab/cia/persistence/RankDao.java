package com.dglab.cia.persistence;

import com.dglab.cia.json.RankedMode;
import com.dglab.cia.database.EliteElo;
import com.dglab.cia.database.PlayerMatchData;
import com.dglab.cia.database.PlayerRank;
import com.dglab.cia.database.RankPrimaryKey;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.EntityType;
import java.util.*;
import java.util.stream.Collectors;

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

		for (RankedMode rankedMode : RankedMode.realValues()) {
			PlayerRank rank = findPlayerRank(steamId64, season, rankedMode);

			if (rank != null) {
				result.add(rank);
			}
		}

		return result;
	}

	public PlayerRank findPlayerRank(long steamId64, byte season, RankedMode mode) {
		RankPrimaryKey pk = new RankPrimaryKey();
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

		if (checkRankElo(rank)) {
			save(rank);
		}

		return rank;
	}

	@Transactional(readOnly = true)
	public Collection<Integer> findPlayerRankOneSeasons(long steamId64) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PlayerRank> query = builder.createQuery(PlayerRank.class);
        EntityType<PlayerRank> entity = entityManager.getMetamodel().entity(PlayerRank.class);

        Root<PlayerRank> root = query.from(entity);
        query.select(root);
        query.where(
                builder.and(
                        builder.equal(root.get("pk").get("steamId64"), steamId64),
                        builder.equal(root.get("rank"), 1)
                )
        );

        return entityManager
                .createQuery(query)
                .getResultList()
                .stream()
                .map(rank -> (int) rank.getPk().getSeason()).collect(Collectors.toSet());
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

		Join<PlayerRank, EliteElo> join = root.join(entity.getSingularAttribute("elo", EliteElo.class), JoinType.LEFT);

		query.orderBy(builder.asc(root.get("rank")), builder.desc(join.get("elo")));

		List<PlayerRank> result = entityManager.createQuery(query).setMaxResults(amount).getResultList();
		result.stream().filter(this::checkRankElo).forEach(this::save);

		return result;
	}

    public Map<RankedMode, List<PlayerRank>> findTopPlayers(byte season, int amount) {
        Map<RankedMode, List<PlayerRank>> result = new HashMap<>();

        for (RankedMode rankedMode : RankedMode.realValues()) {
            List<PlayerRank> topPlayers = findTopPlayers(season, rankedMode, amount);

            if (!topPlayers.isEmpty()) {
                result.put(rankedMode, topPlayers);
            }
        }

        return result;
    }

	public void save(PlayerRank rank) {
		checkRankElo(rank);

		entityManager.merge(rank);
	}

	public void save(Collection<PlayerRank> ranks) {
		ranks.forEach(this::save);
	}

	private boolean checkRankElo(PlayerRank rank) {
		if (rank.getRank() == 1) {
			EliteElo elo = rank.getElo();

			if (elo == null) {
				elo = new EliteElo();
				elo.setElo(RankService.STARTING_ELO);
				elo.setPk(rank.getPk());

				rank.setElo(elo);

				return true;
			}
		}

		return false;
	}
}
