package com.dglab.cia;

import com.dglab.cia.json.ObjectMapperFactory;
import com.dglab.cia.json.RankedPlayer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.TemplateLoader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.authorization.RequireAnyRoleAuthorizer;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.http.HttpActionAdapter;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.http.client.direct.DirectBasicAuthClient;
import org.pac4j.http.client.indirect.IndirectBasicAuthClient;
import org.pac4j.http.credentials.UsernamePasswordCredentials;
import org.pac4j.http.credentials.authenticator.Authenticator;
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator;
import org.pac4j.http.profile.HttpProfile;
import org.pac4j.oauth.client.Google2Client;
import org.pac4j.sparkjava.DefaultHttpActionAdapter;
import org.pac4j.sparkjava.RequiresAuthenticationFilter;
import org.pac4j.sparkjava.SparkWebContext;
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
	private ObjectMapper mapper = ObjectMapperFactory.createObjectMapper();

	public ViewApplication() {
		port(80);
		threadPool(4);

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

            return "";
        }));

		exception(Exception.class, (exception, request, response) -> {
			exception.printStackTrace();
		});

		before("/admin/*", new RequiresAuthenticationFilter(setupAuth(), "DirectBasicAuthClient"));
		mapGet("/admin/ranks/set/:id/:mode/:rank");
		mapGet("/admin/streaks/set/:id/:mode/:current/:max");
	}

	private Config setupAuth() {
		DirectBasicAuthClient directBasicAuthClient = new DirectBasicAuthClient();
		directBasicAuthClient.setAuthenticator(credentials -> {
			if (credentials == null) {
				throw new CredentialsException("No credential");
			}

			String username = credentials.getUsername();
			String password = credentials.getPassword();

			if (CommonHelper.isBlank(username)) {
				throw new CredentialsException("Username cannot be blank");
			}
			if (CommonHelper.isBlank(password)) {
				throw new CredentialsException("Password cannot be blank");
			}

			try {
				String key = FileUtils.readFileToString(new File("private.key"));

				if (CommonHelper.areNotEquals(password, key)) {
					throw new CredentialsException("Incorrect password");
				}

				final HttpProfile profile = new HttpProfile();
				profile.setId(username);
				profile.addAttribute(CommonProfile.USERNAME, username);
				credentials.setUserProfile(profile);
			} catch (IOException e) {
				throw new CredentialsException(e);
			}
		});

		Config config = new Config(new Clients(directBasicAuthClient));
		config.addAuthorizer("admin", new RequireAnyRoleAuthorizer("ROLE_ADMIN"));
		config.setHttpActionAdapter((code, context) -> {
			SparkWebContext webContext = (SparkWebContext) context;
			if (code == HttpConstants.UNAUTHORIZED) {
				context.setResponseHeader("WWW-Authenticate", "Basic realm=\"Dr. Pavel, I am from CIA\"");
				halt(HttpConstants.UNAUTHORIZED, "authentication required");
			} else if (code == HttpConstants.FORBIDDEN) {
				halt(HttpConstants.FORBIDDEN, "forbidden");
			} else if (code == HttpConstants.OK) {
				halt(HttpConstants.OK, webContext.getBody());
			} else if (code == HttpConstants.TEMP_REDIRECT) {
				webContext.getSparkResponse().redirect(webContext.getLocation());
			}

			return null;
		});

		return config;
	}

	private void mapGet(String uri) {
		get(uri, ((request, response) -> {
			String queryString = (request.queryString() != null ? "?" + request.queryString() : "");
			Map<String, String> headers = request.headers().stream().collect(Collectors.toMap(h -> h, request::headers));
			Unirest.get(PROXY_TARGET + request.uri() + queryString)
					.headers(headers)
					.asBinary();

			return "";
		}));
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
