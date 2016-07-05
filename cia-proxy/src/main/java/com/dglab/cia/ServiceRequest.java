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

/**
 * @author doc
 */
public class ServiceRequest {
	private ScheduledExecutorService service = Executors.newScheduledThreadPool(16);
	private static Logger logger = Logger.getLogger(ServiceRequest.class.getName());

	private int triesLeft = 5;
	private Map<String, String> headers;
	private byte[] body;
	private String url;

	public ServiceRequest(String url, Map<String, String> headers, byte[] body) {
		this.url = url;
		this.headers = headers;
		this.body = body;
	}

	public HttpResponse<InputStream> retry() {
		try {
			HttpResponse<InputStream> answer = Unirest
					.post(ReverseProxy.PROXY_TARGET + url)
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
