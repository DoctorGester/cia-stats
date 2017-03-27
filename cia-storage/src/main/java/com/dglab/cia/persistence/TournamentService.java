package com.dglab.cia.persistence;

import com.dglab.cia.database.TournamentParticipant;
import com.dglab.cia.json.TournamentParticipantData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author doc
 */
@Service
public class TournamentService {
    private static Logger log = LoggerFactory.getLogger(TournamentService.class);

    @Autowired
    private TournamentParticipantsRepository repository;

    @Autowired
    private RankService rankService;

    private static final ZonedDateTime REGISTRATION_OPEN = ZonedDateTime.of(2017, 2, 17, 15, 0, 0, 0, ZoneOffset.UTC);

    public List<TournamentParticipantData> getParticipants() {
        return repository.findAll().stream().sorted(Comparator.comparingInt(TournamentParticipant::getId)).map(p -> {
            TournamentParticipantData data = new TournamentParticipantData();
            data.setSteamId64(p.getSteamId64());
            data.setReplacement(p.isReplacementPlayer());

            if (p.getName() != null) {
                data.setName(p.getName().getName());
                data.setAvatarUrl(p.getName().getAvatarUrl());
            }

            return data;
        }).collect(Collectors.toList());
    }

    @Transactional
    public boolean register(long steamId64) {
        if (ZonedDateTime.now(ZoneOffset.UTC).isBefore(REGISTRATION_OPEN)) {
            log.info("Registration denied for {}, too early", steamId64);
            return false;
        }

        long count = repository.count();

        if (count >= 24) {
            log.info("Registration denied for {}, tournament is full", steamId64);
            return false;
        }

        if (repository.findBySteamId64(steamId64) != null) {
            log.info("Registration denied for {}, already registered", steamId64);
            return false;
        }

        if (!canRegister(steamId64)) {
            log.info("Registration denied for {}, not eligible", steamId64);
            return false;
        }

        boolean isReplacement = count >= 16;

        TournamentParticipant participant = new TournamentParticipant();
        participant.setSteamId64(steamId64);
        participant.setReplacementPlayer(isReplacement);

        repository.save(participant);

        log.info("Registered {}, isReplacement: {}", steamId64, isReplacement);

        return true;
    }

    public boolean canRegister(long steamId64) {
        return rankService
                .getPlayerRankHistory(steamId64)
                .values()
                .stream()
                .flatMap(e -> e.entrySet().stream())
                .map(Map.Entry::getValue)
                .anyMatch(r -> r.getRank() == 1);
    }

    public long getTimeUntilRegistration() {
        return ZonedDateTime.now(ZoneOffset.UTC).until(REGISTRATION_OPEN, ChronoUnit.MILLIS);
    }
}
