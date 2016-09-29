package com.dglab.cia.persistence;

import com.dglab.cia.ConnectionState;
import com.dglab.cia.json.RankedMode;
import com.dglab.cia.database.*;
import com.dglab.cia.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author doc
 */
@Service
public class RankServiceImpl implements RankService {
    private static final Logger log = LoggerFactory.getLogger(RankServiceImpl.class);
	private static final ZonedDateTime FIRST_SEASON = ZonedDateTime.of(2016, 7, 1, 0, 0, 0, 0, ZoneOffset.UTC);

	@Autowired
	private RankDao rankDao;

	@Autowired
	private MatchDao matchDao;

    private byte previousSeason = -1;
    private Map<RankedMode, List<PlayerRank>> previousTopPlayers;

	private RankAndStars convertRank(PlayerRank rank) {
		RankAndStars rankAndStars = new RankAndStars(rank.getRank(), rank.getStars());

		EliteElo elo = rank.getElo();

		if (elo != null) {
			rankAndStars.setElo(elo.getElo());
		}

		return rankAndStars;
	}

	private RankedPlayer convertPlayer(PlayerRank rank) {
		RankAndStars rankAndStars = convertRank(rank);
		RankedPlayer player = new RankedPlayer(rank.getPk().getSteamId64(), rankAndStars.getRank());
		player.setElo(rankAndStars.getElo());

        if (rank.getName() != null) {
            player.setName(rank.getName().getName());
            player.setAvatarUrl(rank.getName().getAvatarUrl());
        }

		return player;
	}

    private Map<RankedMode, List<RankedPlayer>> convertPlayers(Map<RankedMode, List<PlayerRank>> players) {
        return players.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry ->
                entry.getValue().stream().map(this::convertPlayer).collect(Collectors.toList())
        ));
    }

    private synchronized Map<RankedMode, List<PlayerRank>> getPreviousTopPlayers() {
        byte previousSeason = (byte) (getCurrentSeason() - 1);

        if (previousSeason != this.previousSeason) {
            previousTopPlayers = rankDao.findTopPlayers(previousSeason, 3);

            this.previousSeason = previousSeason;
        }

        return previousTopPlayers;
    }

	@Override
	public Map<RankedMode, RankAndStars> getPlayerRanks(long steamId64) {
		Collection<PlayerRank> playerRanks = rankDao.findPlayerRanks(steamId64, getCurrentSeason());

		return playerRanks
				.stream()
				.collect(
						Collectors.toMap(
								rank -> rank.getPk().getMode(),
								this::convertRank
						)
				);
	}

	@Override
	public Map<Byte, Map<RankedMode, RankAndStars>> getPlayerRankHistory(long steamId64) {
		Map<Byte, Map<RankedMode, RankAndStars>> result = new HashMap<>();
		Collection<PlayerRank> ranks = rankDao.findPlayerRanks(steamId64);

		for (PlayerRank rank : ranks) {
			byte season = rank.getPk().getSeason();
			Map<RankedMode, RankAndStars> seasonRanks = result.get(season);

			if (seasonRanks == null) {
				seasonRanks = new HashMap<>();
				result.put(season, seasonRanks);
			}

			seasonRanks.put(rank.getPk().getMode(), new RankAndStars(rank.getRank(), rank.getStars()));
		}

		return result;
	}

	@Override
	public RankedMode getMatchRankedMode(Match match) {
		String mode = match.getMode();
		byte players = match.getPlayers();

		if (match.getMap() == MatchMap.UNRANKED) {
			return null;
		}

		if ("2v2".equals(mode) && players == 4) {
			return RankedMode.TWO_TEAMS;
		}

		if ("3v3".equals(mode) && players == 6) {
			return RankedMode.TWO_TEAMS;
		}

		if ("ffa".equals(mode) && players == 2) {
			return RankedMode.DUEL;
		}

		return null;
	}

    @Override
	@Transactional(readOnly = true)
    public Map<Long, RankedAchievements> getRankedAchievements(MatchInfo matchInfo) {
        Map<Long, RankedAchievements> result = new HashMap<>();

        for (PlayerInfo player : matchInfo.getPlayers()) {
            long steamId64 = player.getSteamId64();

            Collection<Integer> seasons = rankDao.findPlayerRankOneSeasons(steamId64);

            RankedAchievements rankedAchievements = new RankedAchievements();
            rankedAchievements.setAchievedSeasons(seasons);
            rankedAchievements.setWasTopPlayer(getPreviousTopPlayers()
                    .values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(r -> r.getPk().getSteamId64() == steamId64)
                    .findAny()
                    .isPresent()
            );

            result.put(steamId64, rankedAchievements);
        }

        return result;
    }

    @Override
	public Map<Long, RankAndStars> getMatchRanks(long matchId) {
		Match match = matchDao.getMatch(matchId);

		if (match == null) {
			return null;
		}

		RankedMode matchRankedMode = getMatchRankedMode(match);
		byte season = getCurrentSeason();

		if (matchRankedMode == null) {
			return null;
		}

		Map<Long, RankAndStars> result = new HashMap<>();

		for (PlayerMatchData player : match.getMatchData()) {
			long steamId64 = player.getPk().getSteamId64();
			PlayerRank rank = rankDao.findPlayerRank(steamId64, season, matchRankedMode);

            result.put(steamId64, convertRank(rank));
		}

		return result;
	}

	@Override
	public byte getCurrentSeason() {
		long between = ChronoUnit.MONTHS.between(FIRST_SEASON, ZonedDateTime.now(ZoneOffset.UTC));
		return (byte) between;
	}

    private int getPlayerElo(PlayerMatchData player, byte season, RankedMode mode) {
        PlayerRank playerRank = rankDao.findPlayerRank(player.getPk().getSteamId64(), season, mode);
        EliteElo elo = playerRank.getElo();

        if (playerRank.getRank() == 1 && elo != null) {
            return elo.getElo();
        }

        return (30 - playerRank.getRank()) * 30;
    }

	@Override
	public RankUpdateDetails processMatchResults(long matchId) {
		Match match = matchDao.getMatch(matchId);

		if (match == null) {
			return null;
		}

		RankedMode matchRankedMode = getMatchRankedMode(match);
		byte season = getCurrentSeason();

		if (matchRankedMode == null) {
			return null;
		}

        int rounds = match.getRounds().size();

        if (rounds <= 2) {
            log.info("Match not scored because only {} rounds were played. Match ID {}", rounds, matchId);
			return null;
        }

		if (checkRankedAbuse(match)) {
			log.info("Ranked abuse detected! Match ID {}", matchId);
			return null;
		}

		Map<Long, RankAndStars> previous = new HashMap<>();
		Map<Long, RankAndStars> updated = new HashMap<>();

		List<PlayerRank> toUpdate = new ArrayList<>();

        Map<Byte, Double> teamAverageElo = match.getMatchData().stream()
                .collect(
                        Collectors.groupingBy(PlayerMatchData::getTeam)
                ).entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                value -> value.getValue().stream().mapToInt(
                                        player -> getPlayerElo(player, season, matchRankedMode)
                                ).average().orElse(STARTING_ELO)
                        )
                );

        for (PlayerMatchData player : match.getMatchData()) {
			List<PlayerRoundData> playerData = match
					.getRounds()
					.stream()
					.flatMap(round -> round.getPlayerRoundData().stream())
					.filter(data -> data.getPk().getSteamId64() == player.getPk().getSteamId64())
					.collect(Collectors.toList());

			long notPlayed = playerData
					.stream()
					.filter(data -> data.getHero() == null)
					.count();

			boolean abandoned = playerData
					.stream()
					.anyMatch(data -> data.getConnectionState() == ConnectionState.ABANDONED.ordinal());

			long steamId64 = player.getPk().getSteamId64();
			PlayerRank playerRank = rankDao.findPlayerRank(steamId64, season, matchRankedMode);
			EliteElo elo = playerRank.getElo();

			int stars = playerRank.getStars();

			RankAndStars oldRank = convertRank(playerRank);
			previous.put(steamId64, oldRank);

            boolean won = player.getTeam() == match.getWinnerTeam() && !(abandoned || notPlayed > 2);

            if (playerRank.getRank() == 1) {
                Optional<Byte> enemyTeam
                        = teamAverageElo.keySet().stream().filter(team -> player.getTeam() != team).findFirst();

                if (enemyTeam.isPresent()) {
                    double playerAverageElo = teamAverageElo.get(player.getTeam());
                    double enemyAverageElo = teamAverageElo.get(enemyTeam.get());

                    updateElo(elo::getElo, elo::setElo, won, playerAverageElo, enemyAverageElo);
                }
            } else {
                stars = stars + (won ? 1 : -1);
            }

			if (stars > matchRankedMode.getStars()) {
				stars = matchRankedMode.getStars();

				int newRank = Math.max(1, playerRank.getRank() - 1);
				playerRank.setRank((byte) newRank);
				playerRank.setStars((byte) stars);
			} else if (stars <= 0) {
				stars = matchRankedMode.getStars();

				int newRank = Math.min(30, playerRank.getRank() + 1);
				playerRank.setRank((byte) newRank);
				playerRank.setStars((byte) stars);
			} else {
				playerRank.setStars((byte) stars);
			}

			boolean rankUpdated = oldRank.getRank() != playerRank.getRank() || oldRank.getStars() != playerRank.getStars();
			boolean eloUpdated = false;

			if (oldRank.getElo() != null) {
				eloUpdated = oldRank.getElo() != elo.getElo();
			}

			if (rankUpdated || eloUpdated) {
                updated.put(steamId64, convertRank(playerRank));
                toUpdate.add(playerRank);
			}
		}

		rankDao.save(toUpdate);

        log.info("Updated ranks for match {}", matchId);

        // Adding starting ELO values to all players who just reached rank 1
        updated.entrySet().stream()
                .filter(e -> e.getValue().getRank() == 1)
                .filter(e -> previous.containsKey(e.getKey()) && previous.get(e.getKey()).getRank() > 1)
                .forEach(e -> e.getValue().setElo(STARTING_ELO));

		RankUpdateDetails details = new RankUpdateDetails();
		details.setPrevious(previous);
		details.setUpdated(updated);

		return details;
	}

	private boolean checkRankedAbuse(Match match) {
		PlayerMatchData firstPlayer = match.getMatchData().iterator().next();
		List<Match> lastMatches = matchDao.getPlayerMatchesInADay(firstPlayer.getPk().getSteamId64());

		int sameSetAmount = 0;

		Set<Long> initialSet = match
				.getMatchData()
				.stream()
				.map(data -> data.getPk().getSteamId64())
				.collect(Collectors.toSet());

		for (Match lastMatch : lastMatches) {
			if (lastMatch.getMatchId() != match.getMatchId()) {

				if (getMatchRankedMode(lastMatch) != null) {
					Set<Long> playerSet = lastMatch
							.getMatchData()
							.stream()
							.map(data -> data.getPk().getSteamId64())
							.collect(Collectors.toSet());

					if (playerSet.equals(initialSet)) {
						sameSetAmount++;
					}
				}
			}
		}

		return sameSetAmount > 2;
	}

	private void updateElo(Supplier<Short> elo, Consumer<Short> consumer, boolean won, double playerAverage, double enemyAverage) {
        Short previousElo = elo.get();

        int gameResult = won ? 1 : 0;
        double chanceToWin = 1 / (1 + Math.pow(10, (enemyAverage - playerAverage) / 900));
        long eloDelta = Math.round(50 * (gameResult - chanceToWin));

        if (won) {
            eloDelta = Math.max(eloDelta, 5);
        } else {
            eloDelta = Math.min(eloDelta, -5);
        }

        consumer.accept((short) Math.max(previousElo + eloDelta, 0));
	}

	@Override
	public Map<RankedMode, List<RankedPlayer>> getTopPlayers() {
        Map<RankedMode, List<PlayerRank>> topPlayers = rankDao.findTopPlayers(getCurrentSeason(), 5);
        return convertPlayers(topPlayers);
	}

    @Override
    public RankedInfo getRankedInfo() {
        Instant seasonEnd = ZonedDateTime
                .now(ZoneOffset.UTC)
                .with(TemporalAdjusters.firstDayOfNextMonth())
                .toLocalDate()
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        RankedInfo info = new RankedInfo();

        info.setCurrentSeason(getCurrentSeason());
        info.setPreviousTopPlayers(convertPlayers(getPreviousTopPlayers()));
        info.setSeasonEndTime(seasonEnd);
        info.setTopPlayers(getTopPlayers());

        return info;
    }

    @Override
	public List<RankedPlayer> getTopPlayers(RankedMode mode) {
		return rankDao
				.findTopPlayers(getCurrentSeason(), mode, 50)
				.stream()
				.map(this::convertPlayer)
				.collect(Collectors.toList());
	}

	@Override
	public void setRank(long steamId64, RankedMode mode, byte rank) {
		PlayerRank playerRank = rankDao.findPlayerRank(steamId64, getCurrentSeason(), mode);
		playerRank.setRank(rank);
		rankDao.save(playerRank);
	}

	@Override
	public void setElo(long steamId64, RankedMode mode, short elo) {
		PlayerRank playerRank = rankDao.findPlayerRank(steamId64, getCurrentSeason(), mode);
		EliteElo playerElo = playerRank.getElo();

		if (playerElo != null) {
			playerElo.setElo(elo);
			rankDao.save(playerRank);
		}
	}
}
