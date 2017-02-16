package com.dglab.cia;

import com.dglab.cia.data.KeyValueHeroCosmetics;
import com.dglab.cia.data.KeyValueHeroCosmeticsEntry;
import com.dglab.cia.json.AllStats;
import com.dglab.cia.json.HeroStats;
import com.dglab.cia.json.PassPlayer;
import com.dglab.cia.json.RankedPlayer;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author doc
 */
@Controller
@EnableAutoConfiguration
@ComponentScan
public class ViewApplication {
    public static final String PROXY_TARGET = "http://127.0.0.1:5141";

    @Autowired
    private DataFetcherService dataFetcher;

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

        return "heroes";
    }

    @RequestMapping("/reborn")
    String reborn(Model model) {
        return "reborn";
    }

    @RequestMapping("/pass")
    String pass(Model model) {
        setupModelFromURL("/pass/top", model, new TypeReference<List<PassPlayer>>() {});

        Map<String, KeyValueHeroCosmetics> heroCosmetics = dataFetcher.getCosmetics().getHeroCosmetics();

        // Preparing for flat-map
        heroCosmetics
                .entrySet().forEach(
                        e -> e.getValue().getEntries().values().forEach(c -> c.setHero(e.getKey()))
        );

        List<KeyValueHeroCosmeticsEntry> cosmeticsPerLevel = heroCosmetics
                .values()
                .stream()
                .flatMap(c -> c.getEntries().values().stream()).filter(c -> "pass".equals(c.getType()))
                .sorted(Comparator.comparingInt(KeyValueHeroCosmeticsEntry::getLevel))
                .collect(Collectors.toList());

        List<KeyValueHeroCosmeticsEntry> baseCosmetics = heroCosmetics
                .values()
                .stream()
                .flatMap(c -> c.getEntries().values().stream()).filter(c -> "pass_base".equals(c.getType()))
                .sorted(Comparator.comparingInt(c -> {
                    if (c.getItem() != null) {
                        return 0;
                    }

                    if (c.getTaunt() != null) {
                        return 1;
                    }

                    return 2;
                }))
                .collect(Collectors.toList());

        model.addAttribute("perLevel", cosmeticsPerLevel);
        model.addAttribute("base", baseCosmetics);

        return "pass";
    }

    @RequestMapping("/ranks/top/{mode}")
    String ranks(Model model, @PathVariable("mode") String mode) {
        setupModelFromURL(String.format("/ranks/top/%s", mode), model, new TypeReference<List<RankedPlayer>>() {
        });

        return "ranks/top/byMode";
    }

    @RequestMapping("/stats/{hero}")
    @ResponseBody
    Object heroStats(Model model, @PathVariable("hero") String hero) {
        setupModelFromURL(String.format("/stats/%s", hero), model, new TypeReference<HeroStats>() {
        });

        return model.asMap().get("model");
    }

    private void setupModelFromURL(String query, Model model, TypeReference<?> type) {
        model.addAttribute("model", HTTPHelper.urlToObject(PROXY_TARGET + query, type));
        model.addAttribute("stringUtils", StringUtils.class);
        model.addAttribute("math", Math.class);
    }
}
