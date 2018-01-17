package com.dglab.cia.services;

import com.dglab.cia.ConnectionState;
import com.dglab.cia.json.RankedMode;
import com.dglab.cia.database.*;
import com.dglab.cia.json.*;
import com.dglab.cia.json.util.ExpiringObject;
import com.dglab.cia.persistence.MatchDao;
import com.dglab.cia.persistence.RankDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
import java.util.stream.IntStream;

/**
 * @author doc
 */
@Service
public class RankService {
	public static final short STARTING_ELO = 1000;
    private static final Logger log = LoggerFactory.getLogger(RankService.class);
	private static final ZonedDateTime FIRST_SEASON = ZonedDateTime.of(2016, 7, 1, 0, 0, 0, 0, ZoneOffset.UTC);

	@Autowired
	private RankDao rankDao;

	@Autowired
	private MatchDao matchDao;

    private byte previousSeason = -1;
    private Map<RankedMode, List<PlayerRank>> previousTopPlayers;

    private ExpiringObject<RankedInfo> cachedRankedInfo = new ExpiringObject<>(
            this::getRankedInfoInternal,
            ChronoUnit.MINUTES,
            10
    );

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

	public Map<Byte, Map<RankedMode, RankAndStars>> getPlayerRankHistory(long steamId64) {
		Map<Byte, Map<RankedMode, RankAndStars>> result = new HashMap<>();
		Collection<PlayerRank> ranks = rankDao.findPlayerRanks(steamId64);

		for (PlayerRank rank : ranks) {
			byte season = rank.getPk().getSeason();
			Map<RankedMode, RankAndStars> seasonRanks = result.computeIfAbsent(season, k -> new HashMap<>());

			seasonRanks.put(rank.getPk().getMode(), convertRank(rank));
		}

		return result;
	}

	public RankedMode getMatchRankedMode(String mode, int players, MatchMap map) {
		if (map == MatchMap.UNRANKED) {
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

	@Transactional(readOnly = true)
    public Map<Long, RankedAchievements> getRankedAchievements(PlayerList players) {
        Map<Long, RankedAchievements> result = new HashMap<>();

		for (Long steamId64 : players.getPlayers()) {
            Collection<Integer> seasons = rankDao.findPlayerRankOneSeasons(steamId64);

            // Game owner accounts have sets for all seasons
            if (steamId64 == 76561198192021671L || steamId64 == 76561198046920629L) {
                seasons = IntStream.range(0, getCurrentSeason() + 1).boxed().collect(Collectors.toList());
            }
			
            // Sozdatel' top 2017 player reward: ember spirit set
            if (steamId64 == 76561198267389327L) {
                 seasons.add(5);
            }

            RankedAchievements rankedAchievements = new RankedAchievements();
            rankedAchievements.setAchievedSeasons(seasons);
            rankedAchievements.setWasTopPlayer(getPreviousTopPlayers()
                    .values()
                    .stream()
                    .flatMap(Collection::stream)
                    .anyMatch(r -> r.getPk().getSteamId64() == steamId64)
            );

            result.put(steamId64, rankedAchievements);
        }

        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED)
	public Map<Long, RankAndStars> getMatchRanks(MatchInfo match) {
		RankedMode matchRankedMode = getMatchRankedMode(match.getMode(), match.getPlayers().size(), match.getMap());
		byte season = getCurrentSeason();

		if (matchRankedMode == null) {
			return null;
		}

		Map<Long, RankAndStars> result = new HashMap<>();

		for (PlayerInfo player : match.getPlayers()) {
			long steamId64 = player.getSteamId64();
			PlayerRank rank = rankDao.findPlayerRank(steamId64, season, matchRankedMode);

            result.put(steamId64, convertRank(rank));
		}

		return result;
	}

	public byte getCurrentSeason() {
		long between = ChronoUnit.MONTHS.between(FIRST_SEASON, ZonedDateTime.now(ZoneOffset.UTC));
		return (byte) between;
	}

    private int getPlayerElo(PlayerInfo player, byte season, RankedMode mode) {
        PlayerRank playerRank = rankDao.findPlayerRank(player.getSteamId64(), season, mode);
        EliteElo elo = playerRank.getElo();

        if (playerRank.getRank() == 1 && elo != null) {
            return elo.getElo();
        }

        return (30 - playerRank.getRank()) * 30;
    }

    @Transactional(propagation = Propagation.REQUIRED)
	public RankUpdateDetails processMatchResults(MatchInfo match) {
		RankedMode matchRankedMode = getMatchRankedMode(match.getMode(), match.getPlayers().size(), match.getMap());
		byte season = getCurrentSeason();

		if (matchRankedMode == null) {
			return null;
		}

        int rounds = match.getRounds().size();

        if (rounds <= 1) {
            log.info("Match not scored because only {} rounds were played. Match ID {}", rounds, match.getMatchId());
			return null;
        }

		if (checkRankedAbuse(match)) {
			log.info("Ranked abuse detected! Match ID {}", match.getMatchId());
			return null;
		}

		Map<Long, RankAndStars> previous = new HashMap<>();
		Map<Long, RankAndStars> updated = new HashMap<>();

		List<PlayerRank> toUpdate = new ArrayList<>();

        Map<Byte, Double> teamAverageElo = match.getPlayers().stream()
                .collect(
                        Collectors.groupingBy(PlayerInfo::getTeam)
                ).entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                value -> value.getValue().stream().mapToInt(
                                        player -> getPlayerElo(player, season, matchRankedMode)
                                ).average().orElse(STARTING_ELO)
                        )
                );

        for (PlayerInfo player : match.getPlayers()) {
			List<PlayerRoundInfo> playerData = match
					.getRounds()
					.stream()
					.flatMap(round -> round.getPlayers().stream())
					.filter(data -> data.getSteamId64() == player.getSteamId64())
					.collect(Collectors.toList());

			long notPlayed = playerData
					.stream()
					.filter(data -> data.getHero() == null)
					.count();

			boolean abandoned = playerData
					.stream()
					.anyMatch(data -> data.getConnectionState() == ConnectionState.ABANDONED.ordinal());

			long steamId64 = player.getSteamId64();
			PlayerRank playerRank = rankDao.findPlayerRank(steamId64, season, matchRankedMode);
			EliteElo elo = playerRank.getElo();

			int stars = playerRank.getStars();

			RankAndStars oldRank = convertRank(playerRank);
			previous.put(steamId64, oldRank);

            boolean won = player.getTeam() == match.getWinnerTeam() && !(abandoned || notPlayed > 2);

            if (playerRank.getRank() == 1) {
                Optional<Byte> enemyTeam
                        = teamAverageElo.keySet().stream().filter(team -> !Objects.equals(player.getTeam(), team)).findFirst();

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

        log.info("Updated ranks for match {}", match.getMatchId());

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

	private boolean checkRankedAbuse(MatchInfo match) {
		PlayerInfo firstPlayer = match.getPlayers().iterator().next();
		List<Match> lastMatches = matchDao.getPlayerMatchesInADay(firstPlayer.getSteamId64());

		int sameSetAmount = 0;

		Set<Long> initialSet = match
				.getPlayers()
				.stream()
				.map(PlayerInfo::getSteamId64)
				.collect(Collectors.toSet());

		for (Match lastMatch : lastMatches) {
			if (lastMatch.getMatchId() != match.getMatchId()) {

				if (getMatchRankedMode(lastMatch.getMode(), lastMatch.getPlayers(), lastMatch.getMap()) != null) {
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

	public Map<RankedMode, List<RankedPlayer>> getTopPlayers() {
        Map<RankedMode, List<PlayerRank>> topPlayers = rankDao.findTopPlayers(getCurrentSeason(), 5);
        return convertPlayers(topPlayers);
	}

    public RankedInfo getRankedInfo() {
        return cachedRankedInfo.get();
    }

    private RankedInfo getRankedInfoInternal() {
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

	public List<RankedPlayer> getTopPlayers(RankedMode mode) {
		return rankDao
				.findTopPlayers(getCurrentSeason(), mode, 50)
				.stream()
				.map(this::convertPlayer)
				.collect(Collectors.toList());
	}

	public void setRank(long steamId64, RankedMode mode, byte rank) {
		PlayerRank playerRank = rankDao.findPlayerRank(steamId64, getCurrentSeason(), mode);
		playerRank.setRank(rank);
		rankDao.save(playerRank);
	}

	public void setElo(long steamId64, RankedMode mode, short elo) {
		PlayerRank playerRank = rankDao.findPlayerRank(steamId64, getCurrentSeason(), mode);
		EliteElo playerElo = playerRank.getElo();

		if (playerElo != null) {
			playerElo.setElo(elo);
			rankDao.save(playerRank);
		}
	}
}
