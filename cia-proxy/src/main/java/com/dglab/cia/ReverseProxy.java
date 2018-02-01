package com.dglab.cia;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.utils.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static spark.Spark.*;

/**
 * @author doc
 */
public class ReverseProxy {
    private static final Logger log = LoggerFactory.getLogger(ReverseProxy.class);

	public static final String PROXY_TARGET = "http://127.0.0.1:5141";
	private static final String WHITE_LIST_URL = "http://media.steampowered.com/apps/sdr/network_config.json";

	private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	private Collection<IpRange> whiteList = new HashSet<>();
	private Lock whiteListLock = new ReentrantLock();
	private Lock propertiesLock = new ReentrantLock();
	private final Properties applicationProperties = new Properties();

	private String getRequestURL(Request request){
		String queryString = (request.queryString() != null ? "?" + request.queryString() : "");

		return request.uri() + queryString;
	}

	private void downloadAndParseWhiteList() {
		Optional<Collection<IpRange>> parsedIpRanges = new WhitelistHandler().downloadAndParse(WHITE_LIST_URL);

		whiteListLock.lock();

		try {
			whiteList.clear();
			parsedIpRanges.ifPresent(whiteList::addAll);
			whiteList.add(new IpRange("127.0.0.1", null));
		} finally {
			whiteListLock.unlock();
		}

		log.info("IP white-list updated successfully, size: {}", whiteList.size());
	}

	private void reloadProperties(final Path file) {
	    if (!Files.exists(file)) {
	        log.warn("No properties file found at {}", file);
	        return;
        }

		try {
		    propertiesLock.lock();

            try (final BufferedReader reader = Files.newBufferedReader(file)) {
                applicationProperties.load(reader);
            }

            log.info("Reloaded properties file from {}, {}", file, applicationProperties);
		} catch (IOException e) {
			log.error("Error while reading properties", e);
		} finally {
            propertiesLock.unlock();
        }
    }

	public ReverseProxy() {
		port(3637);
		threadPool(16);

		Unirest.setTimeouts(3000, 6000);
		Unirest.clearDefaultHeaders();

        final Path propertiesFile = Paths.get("application.properties");
        reloadProperties(propertiesFile);

        new PropertiesWatchDog(propertiesFile, this::reloadProperties).start();

		service.scheduleAtFixedRate(this::downloadAndParseWhiteList, 0, 1, TimeUnit.DAYS);

		get("/*", ((request, response) -> {
			String url = getRequestURL(request);
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
		    final String requestKey = request.headers("Dedicated-Server-Key");
		    final String propertiesKey = applicationProperties.getProperty("dedicated.key");

		    if (StringUtils.isNotBlank(propertiesKey)) {
                if (!Objects.equals(requestKey, propertiesKey)) {
                    log.info("Access rejected to {}, key given: {}", request.ip(), requestKey);
                    halt(403);
                }
            } else {
		        log.warn("No dedicated server key set, received a key: {}", requestKey);
            }

            String url = getRequestURL(request);
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
