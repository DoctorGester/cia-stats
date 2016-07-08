package com.dglab.cia.persistence;

import com.dglab.cia.database.PlayerName;
import com.lukaspradel.steamapi.core.exception.SteamApiException;
import com.lukaspradel.steamapi.data.json.playersummaries.GetPlayerSummaries;
import com.lukaspradel.steamapi.data.json.playersummaries.Player;
import com.lukaspradel.steamapi.data.json.playersummaries.Response;
import com.lukaspradel.steamapi.webapi.client.SteamWebApiClient;
import com.lukaspradel.steamapi.webapi.request.GetPlayerSummariesRequest;
import com.lukaspradel.steamapi.webapi.request.builders.SteamWebApiRequestFactory;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author doc
 */
@Service
public class PlayerNameServiceImpl implements PlayerNameService {
    private Logger log = LoggerFactory.getLogger(PlayerNameService.class);

	@Autowired
	private PlayerNameDao playerNameDao;

	private ExecutorService executorService = Executors.newFixedThreadPool(16);
    private SteamWebApiClient api;

    public PlayerNameServiceImpl() {
        try {
            String key = FileUtils.readFileToString(new File("private.key"));
            api = new SteamWebApiClient.SteamWebApiClientBuilder(key).build();
        } catch (Exception e) {
            log.warn("Error initializing Steam API file: {}", e.toString());
        }
    }

    @Override
    public Future<?> updatePlayerNames(Collection<Long> steamIds64) {
        return executorService.submit(() -> {
            try {
                if (api == null) {
                    log.warn("Steam API client was not initialized");
                    return null;
                }

                GetPlayerSummariesRequest request = SteamWebApiRequestFactory
                        .createGetPlayerSummariesRequest(
                                steamIds64.stream().map(Object::toString).collect(Collectors.toList())
                        );

                GetPlayerSummaries summaries = api.<GetPlayerSummaries>processRequest(request);
                Response response = summaries.getResponse();

                for (Player player : response.getPlayers()) {
                    PlayerName playerName = new PlayerName();
                    playerName.setSteamId64(Long.valueOf(player.getSteamid()));
                    playerName.setName(player.getPersonaname());
                    playerName.setAvatarUrl(player.getAvatarmedium());

                    playerNameDao.update(playerName);
                }
            } catch (SteamApiException e) {
                log.warn("Steam API exception {}", e.toString());
                return null;
            }

            return null;
        });
    }
}
