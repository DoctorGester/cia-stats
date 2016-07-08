package com.dglab.cia;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.utils.IOUtils;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author doc
 */
public class ServiceRequest {
	private static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private static final Logger log = LoggerFactory.getLogger(ServiceRequest.class);

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
		} catch (Exception e) {
			log.info(e.getMessage());

			if (--triesLeft > 0) {
                log.info("Retrying request to {}", url);
				service.schedule(this::retry, 30, TimeUnit.SECONDS);
			} else {
				log.info("Not retrying anymore: {}", url);
			}
		}

		return null;
	}
}
