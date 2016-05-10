package com.dglab.cia;

import com.dglab.cia.json.MatchInfo;
import com.dglab.cia.json.RoundInfo;
import com.dglab.cia.persistence.MatchService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

import static com.dglab.cia.JsonUtil.json;
import static spark.Spark.*;

/**
 * @author doc
 */
public class Application {
	private ApplicationContext context;
	private MatchService matchService;
	private ObjectMapper mapper = new ObjectMapper();

	public Application() {
		port(5141);
		threadPool(16);

		mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);

		context = new AnnotationConfigApplicationContext(PersistenceConfig.class);
		matchService = context.getBean(MatchService.class);

		get("/match/:id", (request, response) -> {
			return matchService.getMatchDetails(Long.valueOf(request.params("id")));
		}, json());

		post("/match/:id", (request, response) -> {
			long matchId = Long.valueOf(request.params("id"));
			String data = request.raw().getParameter("data");
			MatchInfo matchInfo = mapper.readValue(data, MatchInfo.class);
			matchInfo.setMatchId(matchId);

			matchService.putMatch(matchInfo);

			return null;
		});

		post("/match/:id/:round", (request, response) -> {
			long matchId = Long.valueOf(request.params("id"));
			short round = Short.valueOf(request.params("round"));

			String data = request.raw().getParameter("data");
			RoundInfo roundInfo = mapper.readValue(data, RoundInfo.class);
			roundInfo.setMatchId(matchId);
			roundInfo.setRoundNumber(round);

			matchService.putRound(roundInfo);

			return null;
		});

		exception(Exception.class, (exception, request, response) -> {
			exception.printStackTrace();
		});

		try {
			String data = "{\"mode\":\"ffa\",\"players\":[{\"steamId64\":\"76561198046920629\",\"team\":2}, {\"steamId64\":\"76561198046920630\",\"team\":3}]}";
			MatchInfo matchInfo = mapper.readValue(data, MatchInfo.class);
			matchInfo.setMatchId(0);

			matchService.putMatch(matchInfo);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
