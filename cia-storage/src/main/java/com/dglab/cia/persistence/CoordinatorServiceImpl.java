package com.dglab.cia.persistence;

import com.dglab.cia.json.Achievements;
import com.dglab.cia.json.MatchInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author doc
 */
@Service
public class CoordinatorServiceImpl implements CoordinatorService {

    @Autowired
    private MatchService matchService;

    @Autowired
    private RankService rankService;

    @Override
    @Transactional(readOnly = true)
    public Achievements getAchievements(MatchInfo matchInfo) {
        Achievements achievements = new Achievements();

        achievements.setCurrentSeason(rankService.getCurrentSeason());
        achievements.setAchievements(rankService.getRankedAchievements(matchInfo));
        achievements.setGamesPlayed(matchService.getMatchesPlayed(matchInfo));
        achievements.setPassExperience(matchService.getPassExperience(matchInfo));

        return achievements;
    }
}
