package com.dglab.cia;

import com.dglab.cia.json.*;
import com.dglab.cia.persistence.*;
import com.dglab.cia.util.JsonLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import spark.Request;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static spark.Spark.*;

/**
 * @author doc
 */
public class StorageApplication {
    static {
        System.setProperty("org.jboss.logging.provider", "slf4j");
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
    private JsonLogger jsonLogger;

    private Map<HttpServletRequest, Long> requestTimeMap = Collections.synchronizedMap(
            new PassiveExpiringMap<>(new PassiveExpiringMap.ConstantTimeToLiveExpirationPolicy<>(1, TimeUnit.MINUTES))
    );

    private Map<String, Queue<Long>> urlRequestTimes = new ConcurrentHashMap<>();

	public StorageApplication() {
		port(5141);
		threadPool(32);

		context = new AnnotationConfigApplicationContext();
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
        jsonLogger = context.getBean(JsonLogger.class);

        before((request, response) -> requestTimeMap.put(request.raw(), System.currentTimeMillis()));

        after((request, response) -> {
            Long time = requestTimeMap.get(request.raw());

            if (time != null) {
                long resultTime = System.currentTimeMillis() - time;

                String url = request.requestMethod() + request.uri().replaceAll("/\\d+", "/#");
                Queue<Long> times = urlRequestTimes.get(url);

                if (times == null) {
                    times = new CircularFifoQueue<>(1024);
                    urlRequestTimes.put(url, times);
                }

                times.add(resultTime);
            }
        });

        before((request, response) -> {
            //request.requestMethod() + request.uri();
        });

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

        get("/requests", (request, response) -> {
            return urlRequestTimes.entrySet().stream().collect(
                    Collectors.toMap(
                            Map.Entry::getKey,
                            e -> Math.floor(e.getValue().stream().mapToLong(Long::longValue).average().orElse(-1))
                    )
            );
        }, jsonUtil.json());

        get("/", (request, response) -> {
            AllStats stats = new AllStats();
            stats.setGeneralWinrates(statsService.getGeneralWinRates());
            stats.setRankOneWinrates(statsService.getRankOneWinRates());

            return stats;
        }, jsonUtil.json());

        get("/stats/:hero", (request, response) -> {
            return statsService.getHeroWinRatePerDay(request.params("hero"));
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
            jsonLogger.log(request);

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
            jsonLogger.log(request);

			long matchId = requestLong(request, "id");
			short round = requestLong(request, "round").shortValue();

			RoundInfo roundInfo = requestObject(request, RoundInfo.class);
			roundInfo.setMatchId(matchId);
			roundInfo.setRoundNumber(round);

			matchService.putRound(roundInfo);

			return "";
		});

		post("/winner/:id", (request, response) -> {
            jsonLogger.log(request);

			long matchId = requestLong(request, "id");

			MatchResult matchResult = requestObject(request, MatchResult.class);
			matchResult.setMatchId(matchId);

            if (matchResult.getWinnerTeam() == 0) {
                log.warn("Incorrect match winner received. Aborting");
                log.warn(request.raw().getParameter("data"));
                return "";
            }

			if (matchService.putMatchResult(matchResult)) {
                log.info("Winner set {}", matchId);

                MatchResults matchResults = new MatchResults();
                RankUpdateDetails details = rankService.processMatchResults(matchId);
                matchResults.setRankDetails(details);

                return matchResults;
            }

			return "";
		}, jsonUtil.json());

        post("/quests/report/:id", (request, response) -> {
            jsonLogger.log(request);

            long matchId = requestLong(request, "id");
            QuestProgressReport progress = requestObject(request, QuestProgressReport.class);

            if (progress != null) {
                Map<Long, PlayerQuestResult> result = passService.processMatchUpdate(matchId, progress);

                if (result != null) {
                    return result;
                }
            }

            return "";
        }, jsonUtil.json());

        post("/quests/update", (request, response) -> {
            jsonLogger.log(request);

            PlayerList players = requestObject(request, PlayerList.class);

            log.info("Quests/Update {}", players.getPlayers());

            return players.getPlayers().stream().collect(
                    Collectors.toMap(id -> id, id -> questService.updatePlayerQuests(id))
            );
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

        get("/admin/matches/cleanup", (request, response) -> {
            matchService.deleteOldMatches();

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
