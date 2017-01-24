package com.dglab.cia.persistence;

import com.dglab.cia.database.PassOwner;
import com.dglab.cia.json.*;
import com.dglab.cia.util.ExpiringObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author doc
 */
@Service
public class PassServiceImpl implements PassService {
    private static final Logger log = LoggerFactory.getLogger(PassServiceImpl.class);

    @Autowired
    private PassOwnersRepository repository;

    @Autowired
    private QuestService questService;

    @Autowired
    private MatchDao matchDao;

    private ExpiringObject<List<PassPlayer>> cachedTop = new ExpiringObject<>(
            this::getTopPlayersInternal,
            ChronoUnit.MINUTES,
            10
    );

    @Override
    @Transactional(readOnly = true)
    public PassOwner get(long steamId64) {
        return repository.findOne(steamId64);
    }

    @Override
    @Transactional
    public PassOwner getOrCreate(long steamId64) {
        PassOwner owner = repository.findOne(steamId64);

        if (owner == null) {
            Instant now = Instant.now(Clock.systemUTC());

            owner = new PassOwner();
            owner.setSteamId64(steamId64);
            owner.setExperience(0);
            owner.setLastQuestUpdate(now.minus(3, ChronoUnit.DAYS));
            owner.setNew(true);

            repository.save(owner);
        }

        return owner;
    }

    @Override
    @Transactional
    public List<PassPlayer> getTopPlayers() {
        return cachedTop.get();
    }

    private List<PassPlayer> getTopPlayersInternal() {
        return repository.findTop5ByOrderByExperienceDesc().stream().map(owner -> {
            PassPlayer passPlayer = new PassPlayer();
            passPlayer.setSteamId64(owner.getSteamId64());
            passPlayer.setExperience(owner.getExperience());

            if (owner.getName() != null) {
                passPlayer.setName(owner.getName().getName());
                passPlayer.setAvatarUrl(owner.getName().getAvatarUrl());
            }

            return passPlayer;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void awardExperience(long steamId64, int experience) {
        PassOwner owner = getOrCreate(steamId64);

        owner.setExperience(owner.getExperience() + experience);
    }

    @Override
    @Transactional
    public Map<Long, PlayerQuestResult> processMatchUpdate(MatchInfo match) {
        QuestProgressReport progress = match.getQuestProgress();

        if (progress == null) {
            log.info("No quest results to process for match {}", match.getMatchId());
            return null;
        }

        if (match.getGameLength() < 90) {
            log.info("Insufficient game length to process rewards ({})", match.getGameLength());
            return null;
        }

        Map<Long, PlayerQuestResult> result = new HashMap<>();

        int award = (int) Math.ceil(Math.min(match.getGameLength() * EXPERIENCE_PER_SECOND, 100));
        for (Long passPlayer : progress.getPassPlayers()) {
            PassOwner passOwner = getOrCreate(passPlayer);

            int sum = match.getRounds()
                    .stream()
                    .flatMap(r -> r.getPlayers().stream())
                    .filter(p -> p.getSteamId64() == passPlayer)
                    .mapToInt(PlayerRoundInfo::getDamageDealt)
                    .sum();

            if (sum >= 20) {
                PlayerQuestResult questResult = new PlayerQuestResult();
                questResult.setExperience(passOwner.getExperience());
                questResult.setEarnedExperience(award);

                result.put(passOwner.getSteamId64(), questResult);

                awardExperience(passPlayer, award);
            }
        }

        for (Map.Entry<Long, Integer> entry : progress.getQuestProgress().entrySet()) {
            PassQuest quest = questService.updateQuestProgress(entry.getKey(), entry.getValue().shortValue());

            if (quest == null) {
                continue;
            }

            PlayerQuestResult questResult = result.get(quest.getSteamId64());

            if (questResult == null) {
                continue;
            }

            questResult.setEarnedExperience(questResult.getEarnedExperience() + quest.getReward());

            List<PassQuest> completedQuests = questResult.getCompletedQuests();

            if (completedQuests == null) {
                completedQuests = new ArrayList<>();
                questResult.setCompletedQuests(completedQuests);
            }

            completedQuests.add(quest);
        }

        if (progress.getPassPlayers().size() > 0) {
            log.info("Awarded {} experience for match {}", award, match.getMatchId());
        }

        return result;
    }
}
