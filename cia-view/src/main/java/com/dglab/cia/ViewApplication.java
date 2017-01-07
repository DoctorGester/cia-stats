package com.dglab.cia;

import com.dglab.cia.json.AllStats;
import com.dglab.cia.json.HeroWinRateAndGames;
import com.dglab.cia.json.RankedPlayer;
import com.dglab.cia.json.util.ObjectMapperFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author doc
 */
@Controller
@EnableAutoConfiguration
@ComponentScan
public class ViewApplication {
    public static final String PROXY_TARGET = "http://127.0.0.1:5141";

    private ObjectMapper mapper = ObjectMapperFactory.createObjectMapper();
    private OkHttpClient client = new OkHttpClient();
    private GithubHelper helper = new GithubHelper();

    public ViewApplication() {
		/*threadPool(4);*/

        /*
		exception(Exception.class, (exception, request, response) -> {
			exception.printStackTrace();
		});

		before("/admin/*", new RequiresAuthenticationFilter(setupAuth(), "DirectBasicAuthClient"));
		mapGet("/admin/ranks/set/:id/:mode/:rank");
		mapGet("/admin/elo/set/:id/:mode/:elo");
        mapGet("/admin/stats/recalculate/:stat");*/
    }

    @RequestMapping("/.well-known/acme-challenge/{path}")
    void wellKnown(@PathVariable("path") String path, HttpServletResponse response) {
        try (InputStream stream = FileUtils.openInputStream(new File(".well-known/acme-challenge/", path))) {
            org.apache.commons.io.IOUtils.copy(stream, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/")
    String home(Model model) {
        setupModelFromURL("/", model, new TypeReference<AllStats>() {
        });

        return "home";
    }

    @RequestMapping("/heroes/**")
    String heroes(Model model) {
        model.addAttribute("stringUtils", StringUtils.class);
        model.addAttribute("heroes", helper.getHeroesBase64());
        model.addAttribute("abilities", helper.getAbilitiesBase64());
        model.addAttribute("localization", helper.getLocalizationBase64());
        model.addAttribute("localizationRU", helper.getLocalizationRussianBase64());

        return "heroes";
    }

    @RequestMapping("/reborn")
    String reborn(Model model) {
        return "reborn";
    }

    @RequestMapping("/ranks/top/{mode}")
    String ranks(Model model, @PathVariable("mode") String mode) {
        setupModelFromURL(String.format("/ranks/top/%s", mode), model, new TypeReference<List<RankedPlayer>>() {
        });

        return "ranks/top/byMode";
    }

    @RequestMapping("/stats/{hero}")
    @ResponseBody
    Object heroWinRates(Model model, @PathVariable("hero") String hero) {
        setupModelFromURL(String.format("/stats/%s", hero), model, new TypeReference<Map<LocalDate, HeroWinRateAndGames>>() {
        });

        return model.asMap().get("model");
    }

    private void setupModelFromURL(String query, Model model, TypeReference<?> type) {
        Request.Builder builder = new Request.Builder().get().url(PROXY_TARGET + query);
        Request built = builder.build();

        Object result;
        try {
            Response answer = client.newCall(built).execute();

            if (answer == null) {
                throw new RuntimeException();
            }

            result = mapper.readValue(IOUtils.toByteArray(answer.body().byteStream()), type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        model.addAttribute("model", result);
        model.addAttribute("stringUtils", StringUtils.class);
    }
}
