package com.dglab.cia.controllers;

import com.dglab.cia.ViewApplication;
import com.dglab.cia.json.HeroWinRateAndGames;
import com.dglab.cia.json.PlayerProfileInfo;
import com.dglab.cia.json.RankedMode;
import com.dglab.cia.util.HTTPHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;

/**
 * @author doc
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {
    @GetMapping("/{id}")
    String profile(@PathVariable Long id, Model model) {
        String url = ViewApplication.PROXY_TARGET + "/players/" + id;
        PlayerProfileInfo profile = HTTPHelper.urlToObject(url, new TypeReference<PlayerProfileInfo>() {});

        profile.getRankHistory().values().forEach(v -> v.remove(RankedMode.FFA_FOUR));
        profile.getWinrates().forEach(w -> w.setHero(w.getHero().substring("npc_dota_hero_".length())));
        profile.getWinrates().sort(Comparator.comparing(HeroWinRateAndGames::getGames).reversed());

        long maxGames = profile.getWinrates().stream().mapToLong(HeroWinRateAndGames::getGames).max().orElse(0);
        double maxWinrate = profile.getWinrates().stream().mapToDouble(HeroWinRateAndGames::getWinRate).max().orElse(0);

        model.addAttribute("profile", profile);
        model.addAttribute("maxGames", maxGames);
        model.addAttribute("maxWinrate", maxWinrate);
        model.addAttribute("math", Math.class);
        model.addAttribute("modes", RankedMode.class);

        return "profile";
    }
}
