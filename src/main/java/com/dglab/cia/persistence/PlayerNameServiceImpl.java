package com.dglab.cia.persistence;

import com.dglab.cia.database.PlayerName;
import com.github.koraktor.steamcondenser.steam.community.SteamId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author doc
 */
public class PlayerNameServiceImpl implements PlayerNameService {
	@Autowired
	private PlayerNameDao playerNameDao;

	private ExecutorService executorService = Executors.newFixedThreadPool(16);

	@Transactional
	private void setPlayerName(long steamId64, String name) {
		PlayerName playerName = new PlayerName();
		playerName.setSteamId64(steamId64);
		playerName.setName(name);

		playerNameDao.update(playerName);
	}

	@Override
	public Future<String> updatePlayerName(long steamId64) {
		return executorService.submit(() -> {
			SteamId id = SteamId.create(steamId64);
			String name = id.getNickname();

			setPlayerName(steamId64, name);

			return name;
		});
	}
}
