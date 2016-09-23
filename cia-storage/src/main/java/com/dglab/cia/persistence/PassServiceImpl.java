package com.dglab.cia.persistence;

import com.dglab.cia.database.PassOwner;
import com.dglab.cia.json.PassPlayer;
import com.dglab.cia.json.PassQuest;
import com.dglab.cia.json.PlayerQuestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
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

    @Override
    @Transactional
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
            owner.setLastQuestUpdate(now);
            owner.setNew(true);

            repository.save(owner);
        }

        return owner;
    }

    @Override
    @Transactional
    public List<PassPlayer> getTopPlayers() {
        return repository.findTop5ByOrderByExperienceDesc().stream().map(owner -> {
            PassPlayer passPlayer = new PassPlayer();
            passPlayer.setSteamId64(owner.getSteamId64());
            passPlayer.setExperience(owner.getExperience());
            passPlayer.setName(owner.getName().getName());
            passPlayer.setAvatarUrl(owner.getName().getAvatarUrl());

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
    public Map<Long, PlayerQuestResult> processMatchUpdate(List<Long> passPlayers, Map<Long, Integer> progress, int gameLength) {
        if (gameLength < 90) {
            log.info("Insufficient game length to process rewards ({})", gameLength);
            return null;
        }

        Map<Long, PlayerQuestResult> result = new HashMap<>();

        int award = (int) Math.ceil(Math.min(gameLength * EXPERIENCE_PER_SECOND, 100));
        for (Long passPlayer : passPlayers) {
            PassOwner passOwner = getOrCreate(passPlayer);

            PlayerQuestResult questResult = new PlayerQuestResult();
            questResult.setExperience(passOwner.getExperience());
            questResult.setEarnedExperience(award);

            result.put(passOwner.getSteamId64(), questResult);

            awardExperience(passPlayer, award);
        }

        for (Map.Entry<Long, Integer> entry : progress.entrySet()) {
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

        log.info("Awarded {} experience", award);

        return result;
    }
}
