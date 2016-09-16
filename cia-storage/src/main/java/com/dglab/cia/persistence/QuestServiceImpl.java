package com.dglab.cia.persistence;

import com.dglab.cia.database.Quest;
import com.dglab.cia.database.QuestReroll;
import com.dglab.cia.json.PassQuest;
import com.dglab.cia.json.QuestType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author doc
 */
@Service
public class QuestServiceImpl implements QuestService {
    @Autowired
    private QuestRerollsRepository rerollsRepository;

    @Autowired
    private QuestsRepository questsRepository;

    @Override
    public List<PassQuest> getPlayerQuests(long steamId64) {
        return questsRepository.findBySteamId64(steamId64).stream().map(this::convertQuest).collect(Collectors.toList());
    }

    @Override
    public void updateQuestProgress(long questId, short progress) {
        Quest quest = questsRepository.findOne(questId);

        if (quest == null) {
            return;
        }

        quest.setProgress(progress);
    }

    @Override
    public PassQuest rerollQuest(long questId) {
        Quest quest = questsRepository.findOne(questId);

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

        generateNewQuestForPlayer(steamId64, quest);
        quest.setProgress((short) 0);

        return convertQuest(quest);
    }

    private void generateNewQuestForPlayer(long steamId64, Quest quest) {
        List<Quest> quests = questsRepository.findBySteamId64(steamId64);

        List<QuestType> questTypes = Arrays.asList(QuestType.values());
        questTypes.removeAll(quests.stream().map(Quest::getQuestType).collect(Collectors.toList()));

        QuestType nextType = questTypes.get(ThreadLocalRandom.current().nextInt(questTypes.size()));

        quest.setQuestType(nextType);
        quest.setProgress((short) 0);
    }

    private PassQuest convertQuest(Quest quest) {
        PassQuest passQuest = new PassQuest();

        passQuest.setId(quest.getId());
        passQuest.setType(quest.getQuestType());
        passQuest.setProgress(quest.getProgress());

        return passQuest;
    }
}
