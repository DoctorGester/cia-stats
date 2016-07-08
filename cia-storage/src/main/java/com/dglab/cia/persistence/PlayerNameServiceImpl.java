package com.dglab.cia.persistence;

import com.dglab.cia.database.PlayerName;
import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;
import com.github.koraktor.steamcondenser.steam.community.SteamId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author doc
 */
@Service
public class PlayerNameServiceImpl implements PlayerNameService {
    private Logger log = LoggerFactory.getLogger(PlayerNameService.class);

	@Autowired
	private PlayerNameDao playerNameDao;

	private ExecutorService executorService = Executors.newFixedThreadPool(16);

	private void setPlayerName(long steamId64, String name) {
		PlayerName playerName = new PlayerName();
		playerName.setSteamId64(steamId64);
		playerName.setName(name);

		playerNameDao.update(playerName);
	}

	@Override
	public Future<String> updatePlayerName(long steamId64) {
		return executorService.submit(() -> {
            String name = null;

            try {
                SteamId id = SteamId.create(steamId64);
                name = id.getNickname();
            } catch (SteamCondenserException e) {
                log.warn("Couldn't get name for profile %d", steamId64);
                return null;
            }

            setPlayerName(steamId64, name);

			return name;
		});
	}
}
