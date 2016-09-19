package com.dglab.cia.persistence;

import com.dglab.cia.database.PassOwner;
import com.dglab.cia.database.Quest;
import com.dglab.cia.database.QuestReroll;
import com.dglab.cia.json.Hero;
import com.dglab.cia.json.PassQuest;
import com.dglab.cia.json.PlayerQuestResult;
import com.dglab.cia.json.QuestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author doc
 */
@Service
public class QuestServiceImpl implements QuestService {
    private static final Logger log = LoggerFactory.getLogger(QuestServiceImpl.class);

    @Autowired
    private PassService passService;

    @Autowired
    private QuestRerollsRepository rerollsRepository;

    @Autowired
    private QuestsRepository questsRepository;

    @Override
    @Transactional
    public List<PassQuest> updatePlayerQuests(long steamId64) {
        PassOwner player = passService.getOrCreate(steamId64);
        List<Quest> quests = questsRepository.findBySteamId64(steamId64);
        Instant now = Instant.now(Clock.systemUTC());

        log.info("Updating quests for {}", steamId64);

        if (quests.size() < 3) {
            int questsToGenerate = 3;

            if (!player.isNew()) {
                ZoneId utc = ZoneId.of("UTC");
                ZonedDateTime nextMidnight = ZonedDateTime.now(utc).toLocalDate().atStartOfDay(utc).plusDays(1);
                long daysPassed = ChronoUnit.DAYS.between(player.getLastQuestUpdate(), nextMidnight);

                log.info("Days passed {}", daysPassed);

                questsToGenerate = (int) Math.min(daysPassed, 3 - quests.size());
            }

            log.info("Generating {} new quests", questsToGenerate);

            for (int i = 0; i < questsToGenerate; i++) {
                Quest quest = new Quest();
                quest.setSteamId64(steamId64);
                quest.setNew(true);

                generateNewQuestForPlayer(quests, quest);
                questsRepository.save(quest);
                quests.add(quest);
            }

            player.setLastQuestUpdate(now);
        }

        return quests.stream().map(this::convertQuest).collect(Collectors.toList());
    }

    public int updateQuestProgress(Quest quest, short progress) {
        quest.setProgress(progress);

        if (progress >= quest.getQuestType().getGoal()) {
            int reward = quest.getQuestType().getReward();

            passService.awardExperience(quest.getSteamId64(), reward);

            return reward;
        }

        return 0;
    }

    @Override
    @Transactional
    public Map<Long, PlayerQuestResult> updateQuestBatch(Map<Long, Integer> progress) {
        Map<Long, PlayerQuestResult> results = new HashMap<>();

        for (Map.Entry<Long, Integer> entry : progress.entrySet()) {
            Quest quest = questsRepository.findOne(entry.getKey());

            if (quest == null) {
                continue;
            }

            int reward = updateQuestProgress(quest, entry.getValue().shortValue());

            PlayerQuestResult result = results.get(quest.getSteamId64());

            if (result == null) {
                result = new PlayerQuestResult();
                result.setExperience(passService.getOrCreate(quest.getSteamId64()).getExperience());

                results.put(quest.getSteamId64(), result);
            }

            result.setEarnedExperience(result.getEarnedExperience() + reward);

            if (reward > 0) {
                List<PassQuest> completedQuests = result.getCompletedQuests();

                if (completedQuests == null) {
                    completedQuests = new ArrayList<>();
                    result.setCompletedQuests(completedQuests);
                }

                completedQuests.add(convertQuest(quest));
            }
        }

        return results;
    }

    @Override
    @Transactional
    public PassQuest rerollQuest(long questId) {
        Quest quest = questsRepository.findOne(questId);

        log.info("Quest reroll requested for quest {}", questId);

        if (quest == null) {
            return null;
        }

        long steamId64 = quest.getSteamId64();

        QuestReroll reroll = rerollsRepository.findOne(steamId64);

        if (reroll != null) {
            return null;
        }

        reroll = new QuestReroll();
        reroll.setSteamId64(steamId64);

        rerollsRepository.save(reroll);

        generateNewQuestForPlayer(questsRepository.findBySteamId64(steamId64), quest);
        quest.setProgress((short) 0);

        log.info("Generated new quest of type {}", quest.getQuestType());

        return convertQuest(quest);
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Async
    public void resetRerolls() {
        log.info("Resetting rerolls");
        rerollsRepository.truncate();
    }

    private void generateNewQuestForPlayer(List<Quest> quests, Quest quest) {
        List<QuestType> questTypes = new ArrayList<>(Arrays.asList(QuestType.values()));
        questTypes.removeAll(quests.stream().map(Quest::getQuestType).collect(Collectors.toList()));

        ThreadLocalRandom random = ThreadLocalRandom.current();
        QuestType nextType = questTypes.get(random.nextInt(questTypes.size()));

        quest.setQuestType(nextType);
        quest.setProgress((short) 0);

        List<Hero> heroPool = new ArrayList<>(Arrays.asList(Hero.values()));

        switch (nextType) {
            case PLAY_ROUNDS_AS_OR:
                quest.setSecondaryHero(heroPool.remove(random.nextInt(heroPool.size())));
            case PLAY_ROUNDS_AS:
                quest.setHero(heroPool.remove(random.nextInt(heroPool.size())));
                break;
        }
    }

    private PassQuest convertQuest(Quest quest) {
        PassQuest passQuest = new PassQuest();

        passQuest.setId(quest.getId());
        passQuest.setSteamId64(quest.getSteamId64());
        passQuest.setType(quest.getQuestType());
        passQuest.setProgress(quest.getProgress());
        passQuest.setIsNew(quest.isNew());
        passQuest.setHero(quest.getHero());
        passQuest.setSecondaryHero(quest.getSecondaryHero());
        passQuest.setReward(quest.getQuestType().getReward());
        passQuest.setGoal(quest.getQuestType().getGoal());

        return passQuest;
    }
}
