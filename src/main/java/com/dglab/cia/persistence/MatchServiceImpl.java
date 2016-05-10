package com.dglab.cia.persistence;

import com.dglab.cia.database.Match;
import com.dglab.cia.database.PlayerMatchData;
import com.dglab.cia.database.PlayerRoundData;
import com.dglab.cia.json.*;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

/**
 * @author doc
 */

public class MatchServiceImpl implements MatchService {
	@Autowired
	private SessionFactory sessionFactory;

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

		for (PlayerMatchData playerMatchData : match.getMatchData()) {
			PlayerInfo playerInfo = new PlayerInfo();
			playerInfo.setTeam(playerMatchData.getTeam());
			playerInfo.setSteamId64(playerMatchData.getPk().getSteamId64());
			playerInfo.setName(playerMatchData.getName());

			info.getPlayers().add(playerInfo);
		}

		return new MatchDetails(info);
	}

	@Override
	@Transactional
	public void putMatch(MatchInfo matchInfo) {
		try {
			Collection<PlayerMatchData> playerMatchData = new HashSet<>();

			Match match = new Match();
			match.setMatchId(matchInfo.getMatchId());
			match.setPlayers(matchInfo.getPlayerNumber());
			match.setMode(matchInfo.getMode());
			match.setVersion(matchInfo.getVersion());
			match.setDateTime(LocalDateTime.now(Clock.systemUTC()));

			for (PlayerInfo playerInfo : matchInfo.getPlayers()) {
				PlayerMatchData.Pk pk = new PlayerMatchData.Pk();
				pk.setMatchId(match.getMatchId());
				pk.setSteamId64(playerInfo.getSteamId64());

				PlayerMatchData playerData = new PlayerMatchData();
				playerData.setPk(pk);
				playerData.setTeam(playerInfo.getTeam());
				playerData.setName(playerNameService.getPlayerName(playerInfo.getSteamId64()).get());

				playerMatchData.add(playerData);
			}

			match.setMatchData(playerMatchData);

			matchDao.putMatch(match);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Override
	@Transactional
	public void putRound(RoundInfo roundInfo) {
		Match match = matchDao.getMatch(roundInfo.getMatchId());

		if (match == null) {
			throw new IllegalArgumentException();
		}

		Collection<PlayerRoundData> playerRoundData = new HashSet<>();

		for (PlayerRoundInfo playerRoundInfo : roundInfo.getPlayers()) {
			PlayerRoundData roundData = new PlayerRoundData();

			PlayerRoundData.Pk pk = new PlayerRoundData.Pk();

			pk.setMatchId(match.getMatchId());
			pk.setNumber(roundInfo.getRoundNumber());
			pk.setSteamId64(playerRoundInfo.getSteamId64());

			roundData.setPk(pk);
			roundData.setHero(playerRoundInfo.getHero());
			roundData.setScore(playerRoundInfo.getScore());
			roundData.setDamageDealt(playerRoundInfo.getDamageDealt());
			roundData.setProjectilesFired(playerRoundInfo.getProjectilesFired());
		}

		matchDao.putRound(playerRoundData);
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
