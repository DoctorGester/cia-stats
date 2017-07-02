package com.dglab.cia.controllers;

import com.dglab.cia.util.HTTPHelper;
import com.dglab.cia.ViewApplication;
import com.dglab.cia.json.TournamentParticipantData;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;

/**
 * @author doc
 */
@Controller
@RequestMapping("/tournament")
public class TournamentController {
    private long extractSteamId64(Principal principal) {
        String n = principal.getName();
        return Long.parseLong(n.substring(n.indexOf("/id/") + "/id/".length()));
    }

    private boolean canRegister(Principal principal) {
        String checkUrl = ViewApplication.PROXY_TARGET + "/tournament/eligibility/";

        return HTTPHelper.urlToObject(checkUrl + extractSteamId64(principal), new TypeReference<Boolean>() {});
    }

    @RequestMapping({ "/", "" })
    String main(Model model, Principal principal) {
        String timeUrl = ViewApplication.PROXY_TARGET + "/tournament/time";
        String playersUrl = ViewApplication.PROXY_TARGET + "/tournament/participants";

        long remaining = HTTPHelper.urlToObject(timeUrl, new TypeReference<Long>() {});
        List<TournamentParticipantData> participants
                = HTTPHelper.urlToObject(playersUrl, new TypeReference<List<TournamentParticipantData>>() {});
        boolean open = remaining <= 0;

        participants.sort(Comparator.comparing(TournamentParticipantData::isReplacement));

        model.addAttribute("principal", principal);
        model.addAttribute("timeRemaining", remaining);
        model.addAttribute("open", open);
        model.addAttribute("participants", participants);
        model.addAttribute("loggedIn", principal != null);
        model.addAttribute("full", participants.size() >= 24);

        if (principal != null) {
            boolean registered = participants.stream().anyMatch(e -> e.getSteamId64() == extractSteamId64(principal));
            model.addAttribute("rankIsEnough", canRegister(principal));
            model.addAttribute("alreadyRegistered", registered);
        }

        return "tournament";
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping({ "/register" })
    String register(Principal principal) {
        if (principal != null && canRegister(principal)) {
            String url = ViewApplication.PROXY_TARGET + "/auth/tournament/register/";
            HTTPHelper.urlToObject(url + extractSteamId64(principal), new TypeReference<Boolean>() {});
        }

        return "redirect:/tournament";
    }

}
