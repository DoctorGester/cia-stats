package com.dglab.cia;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.StringUtils;
import spark.Request;
import spark.utils.IOUtils;

import java.io.BufferedReader;
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

	public static final String PROXY_TARGET = "http://127.0.0.1:5141";
	private static final String IP_POOL = "https://raw.githubusercontent.com/SteamDatabase/GameTracking/master/dota/game/dota/pak01_dir/scripts/regions.txt";

	private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	private Collection<IpRange> whiteList = new HashSet<>();
	private Lock lock = new ReentrantLock();

	private void downloadAndParseWhiteList() {
		try {
			logger.log(Level.INFO, "Downloading IP white-list");

			HttpResponse<String> response = Unirest.get(IP_POOL).asString();

			if (response == null) {
				throw new UnirestException("No response");
			}

			if (response.getStatus() != 200) {
				throw new UnirestException("Server answered with code " + response.getStatus());
			}

			String body = response.getBody();
			BufferedReader reader = new BufferedReader(new StringReader(body));
			Collection<IpRange> result = new HashSet<>();
			Pattern pattern = Pattern.compile("\"((?:\\d|\\.)+)(?:/(\\d{1,2}))?\"");

			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.contains("//")) {
					Matcher matcher = pattern.matcher(line);

					while (matcher.find()) {
						result.add(new IpRange(matcher.group(1), matcher.group(2)));
					}
				}
			}

			lock.lock();
			whiteList.clear();
			whiteList.addAll(result);
			whiteList.add(new IpRange("127.0.0.1", null));
			lock.unlock();

			logger.log(Level.INFO, "IP white-list updated successfully");
		} catch (Exception e) {
			logger.log(Level.WARNING, "Could not obtain IP white-list:" + e.getMessage());
		}
	}

	private String initialRequestStage(Request request){
		/*lock.lock();

		String ip = request.ip();
		if (whiteList.stream().noneMatch(range -> range.isInRange(ip))) {
			logger.log(Level.INFO, "Access rejected to " + ip);
			halt(403);
		}

		lock.unlock();*/

		String queryString = (request.queryString() != null ? "?" + request.queryString() : "");

		logger.log(Level.INFO, "Access " + request.ip() + " " + queryString);

		return request.uri() + queryString;
	}

	public ReverseProxy() {
		port(3637);
		threadPool(16);

		Unirest.setTimeouts(1000, 6000);
		Unirest.clearDefaultHeaders();

		service.scheduleAtFixedRate(this::downloadAndParseWhiteList, 0, 2, TimeUnit.HOURS);

		get("/*", ((request, response) -> {
			String url = initialRequestStage(request);
			Map<String, String> headers = request.headers().stream().collect(Collectors.toMap(h -> h, request::headers));

			HttpResponse<InputStream> answer = Unirest
					.get(PROXY_TARGET + url)
					.headers(headers)
					.asBinary();

			if (answer == null) {
				return null;
			}

			response.status(answer.getStatus());
			answer.getHeaders().forEach((header, values) -> {
				response.header(header, StringUtils.join(values, ";"));
			});

			return IOUtils.toByteArray(answer.getBody());
		}));

		post("/*", (request, response) -> {
			String url = initialRequestStage(request);
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

	public static void main(String[] args) {
		new ReverseProxy();
	}
}
