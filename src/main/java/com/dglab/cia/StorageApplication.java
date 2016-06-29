package com.dglab.cia;

import com.dglab.cia.json.MatchInfo;
import com.dglab.cia.json.MatchWinner;
import com.dglab.cia.json.RoundInfo;
import com.dglab.cia.persistence.MatchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import spark.Request;

import static spark.Spark.*;

/**
 * @author doc
 */
public class StorageApplication {
	private AnnotationConfigApplicationContext context;
	private MatchService matchService;
	private ObjectMapper mapper;
	private JsonUtil jsonUtil;

	public StorageApplication() {
		port(5141);
		threadPool(16);

		context = new AnnotationConfigApplicationContext();
		context.getEnvironment().setActiveProfiles("readWrite");
		context.register(PersistenceConfig.class);
		context.refresh();

		matchService = context.getBean(MatchService.class);
		mapper = context.getBean(ObjectMapper.class);
		jsonUtil = context.getBean(JsonUtil.class);

		get("/match/:id", (request, response) -> {
			return matchService.getMatchDetails(Long.valueOf(request.params("id")));
		}, jsonUtil.json());

		post("/match/:id", (request, response) -> {
			long matchId = Long.valueOf(request.params("id"));
			MatchInfo matchInfo = requestObject(request, MatchInfo.class);
			matchInfo.setMatchId(matchId);

			matchService.putMatch(matchInfo);

			return "";
		});

		post("/match/:id/:round", (request, response) -> {
			long matchId = Long.valueOf(request.params("id"));
			short round = Short.valueOf(request.params("round"));

			RoundInfo roundInfo = requestObject(request, RoundInfo.class);
			roundInfo.setMatchId(matchId);
			roundInfo.setRoundNumber(round);

			matchService.putRound(roundInfo);

			return "";
		});

		post("/winner/:id", (request, response) -> {
			long matchId = Long.valueOf(request.params("id"));

			MatchWinner matchWinner = requestObject(request, MatchWinner.class);
			matchWinner.setMatchId(matchId);

			matchService.putWinner(matchWinner);

			return "";
		});

		exception(Exception.class, (exception, request, response) -> {
			exception.printStackTrace();
		});
	}

	private <T> T requestObject(Request request, Class<T> type) throws Exception {
		String data = request.raw().getParameter("data");
		return mapper.readValue(data, type);
	}

}
