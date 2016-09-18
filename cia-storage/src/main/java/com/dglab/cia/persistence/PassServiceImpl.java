package com.dglab.cia.persistence;

import com.dglab.cia.database.PassOwner;
import com.dglab.cia.json.PassPlayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author doc
 */
@Service
public class PassServiceImpl implements PassService {
    @Autowired
    private PassOwnersRepository repository;

    @Override
    @Transactional
    public PassOwner getOrCreate(long steamId64) {
        PassOwner owner = repository.findOne(steamId64);

        if (owner == null) {
            Instant now = Instant.now(Clock.systemUTC());

            owner = new PassOwner();
            owner.setSteamId64(steamId64);
            owner.setExperience(0);
            owner.setLastActivity(now);
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
}
