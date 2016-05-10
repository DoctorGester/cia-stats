package com.dglab.cia.persistence;

import com.github.koraktor.steamcondenser.steam.community.SteamId;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author doc
 */
public class PlayerNameServiceImpl implements PlayerNameService {
	private ExecutorService executorService = Executors.newFixedThreadPool(16);

	@Override
	public Future<String> getPlayerName(long steamId64) {

		return executorService.submit(() -> {
			SteamId id = SteamId.create(steamId64);
			return id.getNickname();
		});
	}
}
