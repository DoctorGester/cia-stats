package com.dglab.cia;

import com.dglab.cia.json.RankedPlayer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.TemplateLoader;
import org.apache.commons.lang3.StringUtils;
import spark.ModelAndView;
import spark.template.jade.JadeTemplateEngine;
import spark.utils.GzipUtils;
import spark.utils.IOUtils;

import java.io.*;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static spark.Spark.*;

/**
 * @author doc
 */
public class ViewApplication {
	public static final String PROXY_TARGET = "http://127.0.0.1:5141";

	private JadeTemplateEngine jadeTemplateEngine = createTemplateEngine();
	private ObjectMapper mapper = createObjectMapper();

	public ViewApplication() {
		port(80);
		threadPool(16);

		mapGet("/ranks/top/:mode", "ranks/top/byMode", new TypeReference<List<RankedPlayer>>(){});

        // What a hack
        get("/public/*", ((request, response) -> {
            String path = request.splat()[0];
            try (InputStream stream = getClass().getResourceAsStream("/public/" + path)) {
                if (path.contains("/")) {
                    path = path.substring(path.lastIndexOf('/') + 1);
                }

                byte[] data = IOUtils.toByteArray(stream);

                String guessed = URLConnection.guessContentTypeFromName(path);

                if (guessed == null && path.endsWith(".css")) {
                    guessed = "text/css";
                }

                if (guessed == null) {
                    guessed = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(data));
                }

                if (guessed != null) {
                    response.type(guessed);
                }

                OutputStream wrappedOutputStream = GzipUtils.checkAndWrap(request.raw(), response.raw(), false);

                wrappedOutputStream.write(data);

                wrappedOutputStream.flush();
                wrappedOutputStream.close();
            }

            return null;
        }));

		exception(Exception.class, (exception, request, response) -> {
			exception.printStackTrace();
		});
	}

	private void mapGet(String uri, TypeReference<?> type) {
		mapGet(uri, uri, type);
	}

	private void mapGet(String uri, String view, TypeReference<?> type) {
		get(uri, ((request, response) -> {
			String queryString = (request.queryString() != null ? "?" + request.queryString() : "");
			Map<String, String> headers = request.headers().stream().collect(Collectors.toMap(h -> h, request::headers));
			HttpResponse<InputStream> answer = Unirest
					.get(PROXY_TARGET + request.uri() + queryString)
					.headers(headers)
					.asBinary();

			if (answer == null) {
				return null;
			}

			response.status(answer.getStatus());
			answer.getHeaders().forEach((header, values) -> {
				response.header(header, StringUtils.join(values, ";"));
			});

			Object result = mapper.readValue(IOUtils.toByteArray(answer.getBody()), type);

			HashMap<Object, Object> model = new HashMap<>();
			model.put("model", result);

			return new ModelAndView(model, view);
		}), jadeTemplateEngine);
	}

	private ObjectMapper createObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
		mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

		return mapper;
	}

    private JadeTemplateEngine createTemplateEngine() {
        JadeConfiguration configuration = new JadeConfiguration();
        configuration.setTemplateLoader(new TemplateLoader() {
            @Override
            public long getLastModified(String name) throws IOException {
                return -1;
            }

            @Override
            public Reader getReader(String name) throws IOException {
                return new InputStreamReader(getClass().getResourceAsStream("/templates/" + name));
            }
        });

        return new JadeTemplateEngine(configuration);
    }
}
