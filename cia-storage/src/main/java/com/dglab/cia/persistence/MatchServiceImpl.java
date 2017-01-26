package com.dglab.cia.persistence;

import com.dglab.cia.database.*;
import com.dglab.cia.json.*;
import com.dglab.cia.util.MatchAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author doc
 */

@Service
public class MatchServiceImpl implements MatchService {
    private static final Logger log = LoggerFactory.getLogger(MatchServiceImpl.class);

	@Autowired
	private MatchDao matchDao;

    @Autowired
    private StatsService statsService;

    @Autowired
    private RankService rankService;

	@Autowired
	private PlayerNameService playerNameService;

    @Autowired
    private PassService passService;

	@Override
    @Transactional(readOnly = true)
	public Map<Long, Long> getMatchesPlayed(PlayerList players) {
        return players.getPlayers()
                .stream()
                .collect(Collectors.toMap(
						Function.identity(),
                        player -> matchDao.getMatchCount(player)
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Integer> getPassExperience(PlayerList players) {
        return players.getPlayers()
                .stream()
                .map(passService::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        PassOwner::getSteamId64,
                        PassOwner::getExperience
                ));
    }

    @Override
    @Transactional(readOnly = true)
	public MatchInfo getMatchDetails(long matchId) {
		Match match = matchDao.getMatch(matchId);

		if (match == null) {
			return null;
		}

		MatchInfo info = new MatchInfo();

		info.setMatchId(match.getMatchId());
		info.setMode(match.getMode());
		info.setVersion(match.getVersion());
		info.setDateTime(match.getDateTime());
        info.setWinnerTeam(match.getWinnerTeam());
		info.setMap(match.getMap());
        info.setGameLength(match.getGameLength());

		for (PlayerMatchData playerMatchData : match.getMatchData()) {
			PlayerInfo playerInfo = new PlayerInfo();
			playerInfo.setTeam(playerMatchData.getTeam());
			playerInfo.setSteamId64(playerMatchData.getPk().getSteamId64());

            PlayerName name = playerMatchData.getName();

            if (name != null) {
                playerInfo.setName(name.getName());
            }

			info.getPlayers().add(playerInfo);
		}

        Collection<RoundInfo> roundInfoCollection = new ArrayList<>();

        for (Round round : match.getRounds()) {
            Collection<PlayerRoundData> playerRoundData = round.getPlayerRoundData();
            Collection<PlayerRoundInfo> playerRoundInfoCollection = new ArrayList<>();

            for (PlayerRoundData roundData : playerRoundData) {
                PlayerRoundInfo playerRoundInfo = new PlayerRoundInfo();

                playerRoundInfo.setSteamId64(roundData.getPk().getSteamId64());
                playerRoundInfo.setDamageDealt(roundData.getDamageDealt());
                playerRoundInfo.setProjectilesFired(roundData.getProjectilesFired());
                playerRoundInfo.setScore(roundData.getScore());
                playerRoundInfo.setHero(roundData.getHero());
				playerRoundInfo.setConnectionState(roundData.getConnectionState());

                playerRoundInfoCollection.add(playerRoundInfo);
            }

            RoundInfo roundInfo = new RoundInfo(playerRoundInfoCollection, round.getNumber());
			roundInfo.setWinner(round.getWinner());
            roundInfo.setRoundNumber(round.getPk().getNumber());

            roundInfoCollection.add(roundInfo);
        }

        info.setRounds(roundInfoCollection);

		return info;
	}

    @Override
    public List<MatchHeader> getRecentPlayerMatches(long steamId64) {
        List<Match> recentMatches = matchDao.getRecentPlayerMatches(steamId64, 40);

        return recentMatches.stream().map(match ->
            new MatchHeader(match.getMatchId(), match.getMode(), match.getDateTime())
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
	public void putMatch(MatchInfo matchInfo) throws MatchAlreadyExistsException {
		Collection<PlayerMatchData> playerMatchData = new HashSet<>();

		if (matchDao.getMatch(matchInfo.getMatchId()) != null) {
			throw new MatchAlreadyExistsException(matchInfo.getMatchId());
		}

        Match match = new Match();

		match.setMatchId(matchInfo.getMatchId());
		match.setPlayers(matchInfo.getPlayerNumber());
		match.setMode(matchInfo.getMode());
		match.setVersion(matchInfo.getVersion());
		match.setMap(matchInfo.getMap());
		match.setDateTime(Instant.now(Clock.systemUTC()));
        match.setGameLength(matchInfo.getGameLength());
        match.setWinnerTeam(matchInfo.getWinnerTeam());

		for (PlayerInfo info : matchInfo.getPlayers()) {
			PlayerMatchData matchData = match
					.getMatchData()
					.stream()
					.filter(data -> data.getPk().getSteamId64() == info.getSteamId64())
					.findAny()
					.orElseGet(() -> {
						PlayerMatchData.Pk pk = new PlayerMatchData.Pk();
						pk.setMatchId(matchInfo.getMatchId());
						pk.setSteamId64(info.getSteamId64());

						PlayerMatchData playerData = new PlayerMatchData();
						playerData.setPk(pk);
						return playerData;
					});

			if (info.getTeam() != 0) {
				matchData.setTeam(info.getTeam());
			}

			playerMatchData.add(matchData);
		}

		playerNameService.updatePlayerNames(
				matchInfo.getPlayers().stream().map(PlayerInfo::getSteamId64).collect(Collectors.toList())
		);

		match.setMatchData(playerMatchData);

        Map<Long, RankAndStars> matchRanks = rankService.getMatchRanks(matchInfo);

		match.setRoundData(
		        matchInfo.getRounds().stream().map(
		                info -> createRound(match, info, matchRanks)
                ).collect(Collectors.toList())
        );

        matchDao.putMatch(match);
	}

    @Transactional(propagation = Propagation.REQUIRED)
	private Round createRound(Match match, RoundInfo roundInfo, Map<Long, RankAndStars> matchRanks) {
		Round round = new Round();

		Round.Pk roundKey = new Round.Pk();
		roundKey.setMatchId(match.getMatchId());
		roundKey.setNumber(roundInfo.getRoundNumber());

		round.setPk(roundKey);
		round.setMatch(match);

		if (roundInfo.getWinner() != null) {
			round.setWinner(roundInfo.getWinner());
		}

        Map<Long, Byte> playerTeams = match.getMatchData().stream().collect(
                Collectors.toMap(data -> data.getPk().getSteamId64(), PlayerMatchData::getTeam)
        );

        Collection<PlayerRoundData> playerRoundData = new HashSet<>();

        for (PlayerRoundInfo playerRoundInfo : roundInfo.getPlayers()) {
			PlayerRoundData roundData = new PlayerRoundData();

			PlayerRoundData.Pk playerKey = new PlayerRoundData.Pk();

			playerKey.setMatchId(roundKey.getMatchId());
			playerKey.setNumber(roundKey.getNumber());
			playerKey.setSteamId64(playerRoundInfo.getSteamId64());

			roundData.setRound(round);
			roundData.setPk(playerKey);
			roundData.setHero(playerRoundInfo.getHero());
			roundData.setScore(playerRoundInfo.getScore());
			roundData.setDamageDealt(playerRoundInfo.getDamageDealt());
			roundData.setProjectilesFired(playerRoundInfo.getProjectilesFired());
			roundData.setConnectionState(playerRoundInfo.getConnectionState());

            if (playerRoundInfo.getHero() != null) {
                HeroWinRateKey winRateKey = new HeroWinRateKey();
                winRateKey.setDate(match.getDateTime().atZone(ZoneId.systemDefault()).toLocalDate());
                winRateKey.setHeroName(playerRoundInfo.getHero());
                winRateKey.setMap(match.getMap());
                winRateKey.setMode(match.getMode());
                winRateKey.setPlayers(match.getPlayers());
                winRateKey.setRankRange(RankRange.ALL);

                boolean isWinner = Objects.equals(round.getWinner(), playerTeams.get(playerRoundInfo.getSteamId64()));

                if (matchRanks != null) {
                    RankAndStars rankAndStars = matchRanks.get(playerRoundInfo.getSteamId64());

                    if (rankAndStars != null && rankAndStars.getRank() == 1) {
                        winRateKey.setRankRange(RankRange.RANK_ONE);
                    }
                }

                statsService.incrementHeroStat(playerRoundInfo.getSteamId64(), winRateKey, isWinner);
            }

			playerRoundData.add(roundData);
		}

		round.setPlayerRoundData(playerRoundData);

        return round;
	}
}
