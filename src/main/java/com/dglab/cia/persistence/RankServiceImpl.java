package com.dglab.cia.persistence;

import com.dglab.cia.ConnectionState;
import com.dglab.cia.RankedMode;
import com.dglab.cia.database.Match;
import com.dglab.cia.database.PlayerMatchData;
import com.dglab.cia.database.PlayerRank;
import com.dglab.cia.database.PlayerRoundData;
import com.dglab.cia.json.RankAndStars;
import com.dglab.cia.json.RankUpdateDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author doc
 */
@Service
public class RankServiceImpl implements RankService {
	private static Logger logger = Logger.getLogger(RankService.class.getName());
	private static final ZonedDateTime FIRST_SEASON = ZonedDateTime.of(2016, 7, 1, 0, 0, 0, 0, ZoneOffset.UTC);

	@Autowired
	private RankDao rankDao;

	@Autowired
	private MatchDao matchDao;

	@Override
	public Map<RankedMode, RankAndStars> getPlayerRanks(long steamId64) {
		Collection<PlayerRank> playerRanks = rankDao.findPlayerRanks(steamId64, getCurrentSeason());

		return playerRanks
				.stream()
				.collect(
						Collectors.toMap(
								rank -> rank.getPk().getMode(),
								rank -> new RankAndStars(rank.getRank(), rank.getStars())
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
	public Map<Long, RankAndStars> getMatchRanks(long matchId) {
		Match match = matchDao.getMatch(matchId);

		logger.info("GetMatchRanks " + match + " " + matchId);

		if (match == null) {
			return null;
		}

		RankedMode matchRankedMode = getMatchRankedMode(match);
		byte season = getCurrentSeason();

		logger.info("Mode and season: " + matchRankedMode + " " + season);

		if (matchRankedMode == null) {
			return null;
		}

		Map<Long, RankAndStars> result = new HashMap<>();

		for (PlayerMatchData player : match.getMatchData()) {
			long steamId64 = player.getPk().getSteamId64();

			PlayerRank rank = rankDao.findPlayerRank(player.getPk().getSteamId64(), season, matchRankedMode);

			result.put(steamId64, new RankAndStars(rank.getRank(), rank.getStars()));
		}

		return result;
	}

	@Override
	public byte getCurrentSeason() {
		long between = ChronoUnit.MONTHS.between(FIRST_SEASON, ZonedDateTime.now(ZoneOffset.UTC));
		return (byte) between;
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

		Map<Long, RankAndStars> previous = new HashMap<>();
		Map<Long, RankAndStars> updated = new HashMap<>();

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

			if (playerRank.getRank() == 1) {
				continue;
			}

			int stars = playerRank.getStars();

			previous.put(steamId64, new RankAndStars(playerRank.getRank(), playerRank.getStars()));

			if (abandoned || notPlayed >= 2) {
				stars--;
			} else if (player.getTeam() == match.getWinnerTeam()) {
				stars++;
			} else {
				stars--;
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

			updated.put(steamId64, new RankAndStars(playerRank.getRank(), playerRank.getStars()));

			rankDao.save(playerRank);
		}

		RankUpdateDetails details = new RankUpdateDetails();
		details.setPrevious(previous);
		details.setUpdated(updated);

		return details;
	}
}
