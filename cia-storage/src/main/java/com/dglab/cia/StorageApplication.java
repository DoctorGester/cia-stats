package com.dglab.cia;

import com.dglab.cia.json.*;
import com.dglab.cia.persistence.*;
import com.dglab.cia.util.JsonLogger;
import com.dglab.cia.util.MatchAlreadyExistsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import spark.Request;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
    private TournamentService tournamentService;
    private StatsService statsService;
    private MatchService matchService;
    private QuestService questService;
    private RankService rankService;
    private PassService passService;
    private ObjectMapper mapper;
	private JsonUtil jsonUtil;
    private JsonLogger jsonLogger;

    private Map<HttpServletRequest, Long> requestTimeMap = ExpiringMap.builder().expiration(1, TimeUnit.MINUTES).build();
    private Map<String, Queue<Long>> urlRequestTimes = new ConcurrentHashMap<>();

	public StorageApplication() {
		port(5141);
		threadPool(32);

		context = new AnnotationConfigApplicationContext();
		context.register(PersistenceConfig.class);
		context.refresh();

        coordinatorService = context.getBean(CoordinatorService.class);
        tournamentService = context.getBean(TournamentService.class);
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
                Queue<Long> times = urlRequestTimes.computeIfAbsent(url, k -> new CircularFifoQueue<>(1024));

                times.add(resultTime);
            }
        });

		get("/match/:id", (request, response) ->
			matchService.getMatchDetails(requestLong(request, "id"))
		, jsonUtil.json());

        get("/matches/:id", (request, response) ->
            matchService.getRecentPlayerMatches(requestLong(request, "id"))
        , jsonUtil.json());

        get("/ranks/player/:id", (request, response) ->
			rankService.getPlayerRanks(requestLong(request, "id"))
		, jsonUtil.json());

		get("/ranks/history/:id", (request, response) ->
			rankService.getPlayerRankHistory(requestLong(request, "id"))
		, jsonUtil.json());

		get("/ranks/top/:mode", (request, response) -> {
			RankedMode mode = paramToMode(request, "mode");

			if (mode != null) {
				return rankService.getTopPlayers(mode);
			}

			return "";
		}, jsonUtil.json());

		get("/ranks/top", (request, response) ->
			rankService.getTopPlayers()
		, jsonUtil.json());

        get("/ranks/info", (request, response) ->
            rankService.getRankedInfo()
        , jsonUtil.json());

        get("/requests", (request, response) ->
            urlRequestTimes.entrySet().stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Math.floor(e.getValue().stream().mapToLong(Long::longValue).average().orElse(-1))
                )
            )
        , jsonUtil.json());

        get("/", (request, response) -> {
            AllStats stats = new AllStats();
            stats.setGeneralWinrates(statsService.getGeneralWinRates());
            stats.setRankOneWinrates(statsService.getRankOneWinRates());
            stats.setDuelWinrates(statsService.getDuelWinRates());

            return stats;
        }, jsonUtil.json());

        get("/stats/:hero", (request, response) -> {
            String hero = request.params("hero");

            Map<LocalDate, HeroWinRateAndGames> winRatePerDay = statsService.getHeroWinRatePerDay(hero);
            List<PlayerHeroWinRateAndGames> playerHeroWinRate = statsService.getPlayerHeroWinRate(hero);

            HeroStats heroStats = new HeroStats();

            heroStats.setHeroWinRatePerDate(winRatePerDay);
            heroStats.setHeroWinRatePerPlayer(playerHeroWinRate);

            return heroStats;
        }, jsonUtil.json());

        post("/match/achievements", (request, response) -> {
            PlayerList players = requestObject(request, PlayerList.class);

            long startTime = System.currentTimeMillis();
            Achievements achievements = coordinatorService.getAchievements(players);

            log.info("Match/Info request took {} ms", System.currentTimeMillis() - startTime);

            return achievements;
        }, jsonUtil.json());

		post("/match/:id", (request, response) -> {
            jsonLogger.log(request);

			long matchId = requestLong(request, "id");
			MatchInfo matchInfo = requestObject(request, MatchInfo.class);
			matchInfo.setMatchId(matchId);

            long startTime = System.currentTimeMillis();

            log.info("Match received {}", matchId);

            MatchResults matchResults;

            try {
                matchResults = coordinatorService.processMatch(matchInfo);
            } catch (MatchAlreadyExistsException e) {
                log.info("Tried to report an existing match {}", e.getMatchId());
                return "";
            }

            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("Match process request took {} ms", elapsedTime);

			return matchResults;
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

        get("/pass/top", (request, response) ->
            passService.getTopPlayers()
        , jsonUtil.json());

        before("/auth/*", (request, response) -> {
            if (!"127.0.0.1".equals(request.ip())) {
                throw new IllegalAccessException();
            }
        });

        get("/auth/tournament/register/:id", (request, response) -> {
            long steamId64 = requestLong(request, "id");

            return tournamentService.register(steamId64);
        }, jsonUtil.json());

        get("/tournament/time", (request, response) -> tournamentService.getTimeUntilRegistration(), jsonUtil.json());
        get("/tournament/participants", (request, response) -> tournamentService.getParticipants(), jsonUtil.json());
        get("/tournament/eligibility/:id", (request, response) -> tournamentService.canRegister(requestLong(request, "id")), jsonUtil.json());

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
