package com.dglab.cia.controllers;

import com.dglab.cia.ViewApplication;
import com.dglab.cia.json.HeroWinRateAndGames;
import com.dglab.cia.json.PlayerProfileInfo;
import com.dglab.cia.json.RankAndStars;
import com.dglab.cia.json.RankedMode;
import com.dglab.cia.util.HTTPHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author doc
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {
    public static class RankHistoryEntry {
        private byte season;
        private Map<RankedMode, RankAndStars> rankByMode;

        RankHistoryEntry(byte season, Map<RankedMode, RankAndStars> rankByMode) {
            this.season = season;
            this.rankByMode = rankByMode;
        }

        public byte getSeason() {
            return season;
        }

        public Map<RankedMode, RankAndStars> getRankByMode() {
            return rankByMode;
        }
    }

    @GetMapping("/{id}")
    String profile(@PathVariable Long id, Model model) {
        String url = ViewApplication.PROXY_TARGET + "/players/" + id;
        PlayerProfileInfo profile = HTTPHelper.urlToObject(url, new TypeReference<PlayerProfileInfo>() {});

        profile.getRankHistory().values().forEach(v -> v.remove(RankedMode.FFA_FOUR));

        List<RankHistoryEntry> sortedRankHistory = profile.getRankHistory().entrySet().stream()
                .map(entry -> new RankHistoryEntry(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(RankHistoryEntry::getSeason))
                .collect(Collectors.toList());

        profile.getWinrates().forEach(w -> w.setHero(w.getHero().substring("npc_dota_hero_".length())));
        profile.getWinrates().sort(Comparator.comparing(HeroWinRateAndGames::getGames).reversed());

        long maxGames = profile.getWinrates().stream().mapToLong(HeroWinRateAndGames::getGames).max().orElse(0);
        double maxWinrate = profile.getWinrates().stream().mapToDouble(HeroWinRateAndGames::getWinRate).max().orElse(0);

        model.addAttribute("profile", profile);
        model.addAttribute("rankHistory", sortedRankHistory);
        model.addAttribute("maxGames", maxGames);
        model.addAttribute("maxWinrate", maxWinrate);
        model.addAttribute("math", Math.class);
        model.addAttribute("modes", RankedMode.class);

        return "profile";
    }
}
