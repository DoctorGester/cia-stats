package com.dglab.cia.persistence;

import com.dglab.cia.database.*;
import com.dglab.cia.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
	private PlayerNameService playerNameService;

	@Override
	public MatchDetails getMatchDetails(long matchId) {
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

            RoundInfo roundInfo = new RoundInfo(playerRoundInfoCollection);
			roundInfo.setWinner(round.getWinner());
            roundInfo.setRoundNumber(round.getPk().getNumber());

            roundInfoCollection.add(roundInfo);
        }

		return new MatchDetails(info, roundInfoCollection);
	}

    @Override
    public List<MatchHeader> getRecentPlayerMatches(long steamId64) {
        List<Match> recentMatches = matchDao.getRecentPlayerMatches(steamId64, 40);

        return recentMatches.stream().map(match ->
            new MatchHeader(match.getMatchId(), match.getMode(), match.getDateTime())
        ).collect(Collectors.toList());
    }

    @Override
	public void putMatch(MatchInfo matchInfo) {
		Collection<PlayerMatchData> playerMatchData = new HashSet<>();

		Match match = matchDao.getMatch(matchInfo.getMatchId());

		if (match == null) {
			match = new Match();
		}

		match.setMatchId(matchInfo.getMatchId());
		match.setPlayers(matchInfo.getPlayerNumber());
		match.setMode(matchInfo.getMode());
		match.setVersion(matchInfo.getVersion());
		match.setMap(matchInfo.getMap());
		match.setDateTime(Instant.now(Clock.systemUTC()));

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

        matchDao.putMatch(match);
	}

	@Override
	public void putRound(RoundInfo roundInfo) {
		Match match = matchDao.getMatch(roundInfo.getMatchId());

		if (match == null) {
			throw new IllegalArgumentException();
		}

		Round round = new Round();

		Round.Pk roundKey = new Round.Pk();
		roundKey.setMatchId(match.getMatchId());
		roundKey.setNumber(roundInfo.getRoundNumber());

		round.setPk(roundKey);
		round.setMatch(match);

		if (roundInfo.getWinner() != null) {
			round.setWinner(roundInfo.getWinner());
		}

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

			playerRoundData.add(roundData);
		}

		round.setPlayerRoundData(playerRoundData);

		matchDao.putRound(round);
	}

	@Override
	public boolean putWinner(MatchWinner winner) {
		Match match = matchDao.getMatch(winner.getMatchId());

		if (match == null) {
			throw new IllegalArgumentException();
		}

		if (match.getWinnerTeam() == 0) {

			match.setWinnerTeam(winner.getWinnerTeam());

			matchDao.save(match);

			return true;
		}

		return false;
	}
}
