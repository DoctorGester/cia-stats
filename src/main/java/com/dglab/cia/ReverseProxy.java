package com.dglab.cia;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import spark.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static spark.Spark.*;

/**
 * @author doc
 */
public class ReverseProxy {
	private static Logger logger = Logger.getLogger(ReverseProxy.class.getName());

	private static final String PROXY_TARGET = "http://127.0.0.1:5141";
	private ScheduledExecutorService service = Executors.newScheduledThreadPool(16);

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
			} catch (UnirestException e) {
				logger.log(Level.SEVERE, e.getMessage());

				if (--triesLeft > 0) {
					service.schedule(this::retry, 30, TimeUnit.SECONDS);
				} else {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}
	}

	public ReverseProxy() {
		port(3637);
		threadPool(16);

		Unirest.setTimeouts(1000, 6000);
		Unirest.clearDefaultHeaders();

		post("/*", (request, response) -> {
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
