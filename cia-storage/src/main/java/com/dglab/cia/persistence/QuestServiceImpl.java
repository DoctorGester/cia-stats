package com.dglab.cia.persistence;

import com.dglab.cia.database.PassOwner;
import com.dglab.cia.database.Quest;
import com.dglab.cia.database.QuestReroll;
import com.dglab.cia.json.Hero;
import com.dglab.cia.json.HeroWinRateAndGames;
import com.dglab.cia.json.PassQuest;
import com.dglab.cia.json.QuestType;
import com.dglab.cia.util.ExpiringObject;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Autowired
    private StatsService statsService;

    private ExpiringObject<List<HeroWinRateAndGames>> cachedWinRates;

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

    @Override
    @Transactional
    public PassQuest updateQuestProgress(long questId, short progress) {
        Quest quest = questsRepository.findOne(questId);

        if (quest == null) {
            return null;
        }

        quest.setProgress(progress);

        if (progress >= quest.getQuestType().getGoal()) {
            log.info("Quest {} of type {} completed by {}", quest.getId(), quest.getQuestType(), quest.getSteamId64());

            int reward = quest.getQuestType().getReward();

            passService.awardExperience(quest.getSteamId64(), reward);
            questsRepository.delete(quest);

            return convertQuest(quest);
        }

        return null;
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

    private synchronized Map<Hero, Double> getHeroWeights() {
        if (cachedWinRates == null) {
            cachedWinRates = new ExpiringObject<>(statsService::getGeneralWinRates, ChronoUnit.DAYS, 1);
        }

        List<HeroWinRateAndGames> heroWinRates = cachedWinRates.get();

        Map<Hero, Long> heroGames = heroWinRates.stream().collect(Collectors.toMap(
                h -> Hero.valueOf(h.getHero().substring("npc_dota_hero_".length()).toUpperCase()),
                HeroWinRateAndGames::getGames
        ));

        double avg = heroGames.values().stream().mapToLong(l -> l).average().orElse(1);

        for (Hero hero : Hero.values()) {
            if (!heroGames.containsKey(hero)) {
                heroGames.put(hero, (long) avg);
            }
        }

        double sum = heroGames.values().stream().mapToLong(Long::longValue).sum();
        return heroGames.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, g -> g.getValue() / sum));
    }

    // https://en.wikipedia.org/wiki/Exponential_distribution
    private <E> E weightedRandomValue(Stream<Map.Entry<E, Double>> weights, Random random) {
        return weights
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), -Math.log(random.nextDouble()) / e.getValue()))
                .min((e0,e1)-> e0.getValue().compareTo(e1.getValue()))
                .orElseThrow(IllegalArgumentException::new).getKey();
    }

    private void generateNewQuestForPlayer(List<Quest> quests, Quest quest) {
        List<QuestType> questTypes = new ArrayList<>(Arrays.asList(QuestType.values()));
        questTypes.removeAll(quests.stream().map(Quest::getQuestType).collect(Collectors.toList()));

        ThreadLocalRandom random = ThreadLocalRandom.current();
        QuestType nextType = questTypes.get(random.nextInt(questTypes.size()));

        quest.setQuestType(nextType);
        quest.setProgress((short) 0);

        Map<Hero, Double> heroPool = getHeroWeights();

        Consumer<Consumer<Hero>> heroSetter = (consumer) -> {
            Hero hero = weightedRandomValue(heroPool.entrySet().stream(), random);
            consumer.accept(hero);
            heroPool.remove(hero);
        };

        switch (nextType) {
            case PLAY_ROUNDS_AS_OR:
                heroSetter.accept(quest::setSecondaryHero);
            case PLAY_ROUNDS_AS:
                heroSetter.accept(quest::setHero);
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