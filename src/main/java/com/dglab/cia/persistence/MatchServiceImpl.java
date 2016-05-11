package com.dglab.cia.persistence;

import com.dglab.cia.database.Match;
import com.dglab.cia.database.PlayerMatchData;
import com.dglab.cia.database.PlayerRoundData;
import com.dglab.cia.database.Round;
import com.dglab.cia.json.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author doc
 */

public class MatchServiceImpl implements MatchService {
	@Autowired
	private MatchDao matchDao;

	@Autowired
	private PlayerNameService playerNameService;

	@Override
	@Transactional
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

		for (PlayerMatchData playerMatchData : match.getMatchData()) {
			PlayerInfo playerInfo = new PlayerInfo();
			playerInfo.setTeam(playerMatchData.getTeam());
			playerInfo.setSteamId64(playerMatchData.getPk().getSteamId64());
			playerInfo.setName(playerMatchData.getName().getName());

			info.getPlayers().add(playerInfo);
		}

		return new MatchDetails(info);
	}

	@Transactional
	public void doPutMatch(Match match) {
		matchDao.putMatch(match);
	}

	@Override
	@Transactional
	public void putMatch(MatchInfo matchInfo) {
		Collection<PlayerMatchData> playerMatchData = new HashSet<>();

		Match match = new Match();
		match.setMatchId(matchInfo.getMatchId());
		match.setPlayers(matchInfo.getPlayerNumber());
		match.setMode(matchInfo.getMode());
		match.setVersion(matchInfo.getVersion());
		match.setDateTime(Instant.now(Clock.systemUTC()));

		for (PlayerInfo playerInfo : matchInfo.getPlayers()) {
			PlayerMatchData.Pk pk = new PlayerMatchData.Pk();
			pk.setMatchId(match.getMatchId());
			pk.setSteamId64(playerInfo.getSteamId64());

			PlayerMatchData playerData = new PlayerMatchData();
			playerData.setPk(pk);
			playerData.setTeam(playerInfo.getTeam());

			playerMatchData.add(playerData);
		}

		match.setMatchData(playerMatchData);

		doPutMatch(match);

		for (PlayerInfo playerInfo : matchInfo.getPlayers()) {
			playerNameService.updatePlayerName(playerInfo.getSteamId64());
		}
	}

	@Override
	@Transactional
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

			roundData.setPk(playerKey);
			roundData.setHero(playerRoundInfo.getHero());
			roundData.setScore(playerRoundInfo.getScore());
			roundData.setDamageDealt(playerRoundInfo.getDamageDealt());
			roundData.setProjectilesFired(playerRoundInfo.getProjectilesFired());

			playerRoundData.add(roundData);
		}

		round.setPlayerRoundData(playerRoundData);

		matchDao.putRound(round);
	}

	@Override
	@Transactional
	public void putWinner(MatchWinner winner) {
		Match match = matchDao.getMatch(winner.getMatchId());

		if (match == null) {
			throw new IllegalArgumentException();
		}

		match.setWinnerTeam(winner.getWinnerTeam());

		matchDao.save(match);
	}
}
