package com.dglab.cia;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import spark.utils.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static spark.Spark.*;

/**
 * @author doc
 */
public class ReverseProxy {
	private static Logger logger = Logger.getLogger(ReverseProxy.class.getName());

	private static final String PROXY_TARGET = "http://127.0.0.1:5141";
	private static final String IP_POOL = "https://raw.githubusercontent.com/SteamDatabase/GameTracking/master/dota/game/dota/pak01_dir/scripts/regions.txt";

	private ScheduledExecutorService service = Executors.newScheduledThreadPool(16);
	private Collection<String> whiteList = new HashSet<>();
	private Lock lock = new ReentrantLock();

	private class ServiceRequest {
		int triesLeft = 5;
		private Map<String, String> headers;
		byte[] body;
		String url;

		public ServiceRequest(String url, Map<String, String> headers, byte[] body) {
			this.url = url;
			this.headers = headers;
			this.body = body;
		}

		public HttpResponse<InputStream> retry() {
			try {
				HttpResponse<InputStream> answer = Unirest
						.post(PROXY_TARGET + url)
						.headers(headers)
						.body(body)
						.asBinary();

				if (answer.getStatus() != 200) {
					throw new UnirestException("Service error: " + IOUtils.toString(answer.getBody()));
				}

				return answer;
			} catch (UnirestException | IOException e) {
				logger.log(Level.SEVERE, e.getMessage());

				if (--triesLeft > 0) {
					service.schedule(this::retry, 30, TimeUnit.SECONDS);
				} else {
					e.printStackTrace();
				}
			}

			return null;
		}
	}

	private void downloadAndParseWhiteList() {
		try {
			HttpResponse<String> response = Unirest.get(IP_POOL).asString();

			if (response == null) {
				throw new UnirestException("No response");
			}

			if (response.getStatus() != 200) {
				throw new UnirestException("Server answered with code " + response.getStatus());
			}

			String body = response.getBody();
			BufferedReader reader = new BufferedReader(new StringReader(body));
			Collection<String> result = new HashSet<>();
			Pattern pattern = Pattern.compile("\"(\\d{1,4}\\.\\d{1,4}.\\d{1,4}.\\d{1,4}(/\\d{1,3})?\")");

			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.contains("//")) {
					Matcher matcher = pattern.matcher(line);

					while (matcher.find()) {
						result.add(matcher.group(1));
					}
				}
			}

			lock.lock();
			whiteList.clear();
			whiteList.addAll(result);
			lock.unlock();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Could not obtain IP whitelist:" + e.getMessage());
		}
	}

	public ReverseProxy() {
		port(3637);
		threadPool(16);

		Unirest.setTimeouts(1000, 6000);
		Unirest.clearDefaultHeaders();

		service.scheduleAtFixedRate(this::downloadAndParseWhiteList, 0, 2, TimeUnit.HOURS);

		post("/*", (request, response) -> {
			lock.lock();

			if (!whiteList.contains(request.ip())) {
				halt(403);
			}

			lock.unlock();

			String queryString = (request.queryString() != null ? "?" + request.queryString() : "");
			String url = request.uri() + queryString;
			Map<String, String> headers = request.headers().stream().collect(Collectors.toMap(h -> h, request::headers));
			headers.remove("Content-Length");

			ServiceRequest serviceRequest = new ServiceRequest(url, headers, request.bodyAsBytes());

			HttpResponse<InputStream> answer = serviceRequest.retry();

			if (answer == null) {
				halt(503);
				return "";
			}

			return IOUtils.toByteArray(answer.getRawBody());
		});
	}
}
