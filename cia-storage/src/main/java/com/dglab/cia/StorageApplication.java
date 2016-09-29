package com.dglab.cia;

import com.dglab.cia.json.*;
import com.dglab.cia.persistence.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import spark.Request;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private CoordinatorService coordinatorService;
    private StatsService statsService;
    private MatchService matchService;
    private QuestService questService;
    private RankService rankService;
    private PassService passService;
    private ObjectMapper mapper;
	private JsonUtil jsonUtil;

	public StorageApplication() {
		port(5141);
		threadPool(16);

		context = new AnnotationConfigApplicationContext();
		context.getEnvironment().setActiveProfiles("readWrite");
		context.register(PersistenceConfig.class);
		context.refresh();

        coordinatorService = context.getBean(CoordinatorService.class);
        statsService = context.getBean(StatsService.class);
		matchService = context.getBean(MatchService.class);
        questService = context.getBean(QuestService.class);
		rankService = context.getBean(RankService.class);
        passService = context.getBean(PassService.class);
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
			RankedMode mode = paramToMode(request, "mode");

			if (mode != null) {
				return rankService.getTopPlayers(mode);
			}

			return "";
		}, jsonUtil.json());

		get("/ranks/top", (request, response) -> {
			return rankService.getTopPlayers();
		}, jsonUtil.json());

        get("/ranks/info", (request, response) -> {
            return rankService.getRankedInfo();
        }, jsonUtil.json());

        get("/", (request, response) -> {
            AllStats stats = new AllStats();
            stats.setGeneralWinrates(statsService.getGeneralWinRates());
            stats.setRankOneWinrates(statsService.getRankOneWinRates());

            return stats;
        }, jsonUtil.json());

        post("/match/info/:id", (request, response) -> {
            long matchId = requestLong(request, "id");
            MatchInfo matchInfo = requestObject(request, MatchInfo.class);
            matchInfo.setMatchId(matchId);

            long startTime = System.currentTimeMillis();
            Achievements achievements = coordinatorService.getAchievements(matchInfo);

            log.info("Match/Info request took {} ms", System.currentTimeMillis() - startTime);

            return achievements;
        }, jsonUtil.json());

		post("/match/:id", (request, response) -> {
			long matchId = requestLong(request, "id");
			MatchInfo matchInfo = requestObject(request, MatchInfo.class);
			matchInfo.setMatchId(matchId);

            long startTime = System.currentTimeMillis();

            if (matchInfo.getPlayers().stream().mapToInt(PlayerInfo::getTeam).sum() == 0) {
                log.info("Initial match registration {}", matchId);
            } else {
                log.info("Match started {}", matchId);
            }

			matchService.putMatch(matchInfo);

            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("Match registration request took {} ms", elapsedTime);

			return "";
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

			MatchResult matchResult = requestObject(request, MatchResult.class);
			matchResult.setMatchId(matchId);

			if (matchService.putMatchResult(matchResult)) {
                log.info("Winner set {}", matchId);

                MatchResults matchResults = new MatchResults();
                RankUpdateDetails details = rankService.processMatchResults(matchId);
                Map<Long, Integer> questProgress = matchResult.getQuestProgress();

                if (questProgress != null) {
                    matchResults.setQuestResults(passService.processMatchUpdate(
                            matchResult.getPassPlayers(),
                            questProgress,
                            matchResult.getGameLength()
                    ));
                }

                matchResults.setRankDetails(details);

                return matchResults;
            }

			return "";
		}, jsonUtil.json());

        post("/quests/update", (request, response) -> {
            PlayerList players = requestObject(request, PlayerList.class);

            Map<Long, List<PassQuest>> quests = players.getPlayers().stream().collect(
                    Collectors.toMap(id -> id, id -> questService.updatePlayerQuests(id))
            );

            return quests;
        }, jsonUtil.json());

        post("/quests/reroll/:id", (request, response) -> {
            long questId = requestLong(request, "id");

            return questService.rerollQuest(questId);
        });

        get("/pass/top", (request, response) -> {
            return passService.getTopPlayers();
        }, jsonUtil.json());

		before("/admin/*", (request, response) -> {
			if (!"127.0.0.1".equals(request.ip())) {
				throw new IllegalAccessException();
			}
		});

		get("/admin/ranks/set/:id/:mode/:rank", (request, response) -> {
			RankedMode mode = paramToMode(request, "mode");

			if (mode != null) {
				rankService.setRank(requestLong(request, "id"), mode, requestLong(request, "rank").byteValue());
			}

			return "";
		});

		get("/admin/elo/set/:id/:mode/:elo", (request, response) -> {
			RankedMode mode = paramToMode(request, "mode");

			if (mode != null) {
				rankService.setElo(
						requestLong(request, "id"),
						mode,
						requestLong(request, "elo").shortValue()
				);
			}

			return "";
		});

        get("/admin/stats/recalculate/:stat", (request, response) -> {
            String stat = request.params("stat");

            switch (stat) {
                case "allWinRates":
                    statsService.runAllWinRatesRecalculation();
                    break;

                case "rankOneWinRates":
                    statsService.runRankOneWinRatesRecalculation();
                    break;
            }

            return "";
        });

		exception(Exception.class, (exception, request, response) -> {
			log.error("Error processing request: {} at {}", exception, exception.getStackTrace()[0]);
            response.status(500);
		});
	}

	private RankedMode paramToMode(Request request, String paramName) {
		String mode = request.params(paramName);

		for (RankedMode rankedMode : RankedMode.realValues()) {
			if (rankedMode.getUrl().equalsIgnoreCase(mode)) {
				return rankedMode;
			}
		}

		return null;
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
