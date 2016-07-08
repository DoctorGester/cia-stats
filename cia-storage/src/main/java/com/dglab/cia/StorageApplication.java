package com.dglab.cia;

import com.dglab.cia.json.*;
import com.dglab.cia.persistence.MatchService;
import com.dglab.cia.persistence.RankService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import spark.Request;


import static spark.Spark.*;

/**
 * @author doc
 */
public class StorageApplication {
    static {
        System.setProperty("org.jboss.logging.provider", "slf4j");
        System.setProperty(SimpleLogger.SHOW_DATE_TIME_KEY, "true");
        System.setProperty(SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "true");
        System.setProperty(SimpleLogger.DATE_TIME_FORMAT_KEY, "[yyyy/MM/dd HH:mm:ss]");
    }

    private static Logger log = LoggerFactory.getLogger(StorageApplication.class);

    private AnnotationConfigApplicationContext context;
	private MatchService matchService;
	private RankService rankService;
	private ObjectMapper mapper;
	private JsonUtil jsonUtil;

	public StorageApplication() {
		port(5141);
		threadPool(4);

		context = new AnnotationConfigApplicationContext();
		context.getEnvironment().setActiveProfiles("readWrite");
		context.register(PersistenceConfig.class);
		context.refresh();

		matchService = context.getBean(MatchService.class);
		rankService = context.getBean(RankService.class);
		mapper = context.getBean(ObjectMapper.class);
		jsonUtil = context.getBean(JsonUtil.class);

		get("/match/:id", (request, response) -> {
			return matchService.getMatchDetails(requestLong(request, "id"));
		}, jsonUtil.json());

        get("/matches/:id", (request, response) -> {
            return matchService.getRecentPlayerMatches(requestLong(request, "id"));
        }, jsonUtil.json());

        get("/ranks/player/:id", (request, response) -> {
			return rankService.getPlayerRanks(requestLong(request, "id"));
		}, jsonUtil.json());

		get("/ranks/history/:id", (request, response) -> {
			return rankService.getPlayerRankHistory(requestLong(request, "id"));
		}, jsonUtil.json());

		get("/ranks/top/:mode", (request, response) -> {
			String mode = request.params("mode");

			for (RankedMode rankedMode : RankedMode.values()) {
				if (rankedMode.getUrl().equalsIgnoreCase(mode)) {
					return rankService.getTopPlayers(rankedMode);
				}
			}

			return "";
		}, jsonUtil.json());

		get("/ranks/top", (request, response) -> {
			return rankService.getTopPlayers();
		}, jsonUtil.json());

		post("/match/:id", (request, response) -> {
			long matchId = requestLong(request, "id");
			MatchInfo matchInfo = requestObject(request, MatchInfo.class);
			matchInfo.setMatchId(matchId);

			log.info("Match started {}", matchId);

            for (PlayerInfo player : matchInfo.getPlayers()) {
                log.info("Match {} player with {} in team {}", matchId, player.getSteamId64(), player.getTeam());
            }

			matchService.putMatch(matchInfo);

			return rankService.getMatchRanks(matchId);
		}, jsonUtil.json());

		post("/match/:id/:round", (request, response) -> {
			long matchId = requestLong(request, "id");
			short round = requestLong(request, "round").shortValue();

			RoundInfo roundInfo = requestObject(request, RoundInfo.class);
			roundInfo.setMatchId(matchId);
			roundInfo.setRoundNumber(round);

			matchService.putRound(roundInfo);

			return "";
		});

		post("/winner/:id", (request, response) -> {
			long matchId = requestLong(request, "id");

			MatchWinner matchWinner = requestObject(request, MatchWinner.class);
			matchWinner.setMatchId(matchId);

			log.info("Winner set {}", matchId);

			matchService.putWinner(matchWinner);

			return rankService.processMatchResults(matchId);
		}, jsonUtil.json());

		exception(Exception.class, (exception, request, response) -> {
			log.error("Error processing request: {} at {}", exception, exception.getStackTrace()[0]);
            response.status(500);
		});
	}

    private Long requestLong(Request request, String name) {
        try {
            return Long.valueOf(request.params(name));
        } catch (NumberFormatException e) {
            halt(400);
        }

        return 0L;
    }

	private <T> T requestObject(Request request, Class<T> type) throws Exception {
		String data = request.raw().getParameter("data");
		return mapper.readValue(data, type);
	}

}
