package com.dglab.cia.persistence;

import com.dglab.cia.json.*;
import com.dglab.cia.util.MatchAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @author doc
 */
@Service
public class CoordinatorServiceImpl implements CoordinatorService {
    private static Logger log = LoggerFactory.getLogger(CoordinatorService.class);

    @Autowired
    private MatchService matchService;

    @Autowired
    private RankService rankService;

    @Autowired
    private PassService passService;

    @Override
    @Transactional(readOnly = true)
    public Achievements getAchievements(PlayerList players) {
        Achievements achievements = new Achievements();

        achievements.setCurrentSeason(rankService.getCurrentSeason());
        achievements.setAchievements(rankService.getRankedAchievements(players));
        achievements.setGamesPlayed(matchService.getMatchesPlayed(players));
        achievements.setPassExperience(matchService.getPassExperience(players));

        return achievements;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public MatchResults processMatch(MatchInfo matchInfo) throws MatchAlreadyExistsException {
        MatchResults matchResults = new MatchResults();

        log.info("Saving match {}", matchInfo.getMatchId());
        matchService.putMatch(matchInfo);

        log.info("Processing ranks for match {}", matchInfo.getMatchId());
        RankUpdateDetails details = rankService.processMatchResults(matchInfo);

        log.info("Updating quest results for match {}", matchInfo.getMatchId());
        Map<Long, PlayerQuestResult> questResults = passService.processMatchUpdate(matchInfo);

        matchResults.setRankDetails(details);
        matchResults.setQuestResults(questResults);

        return matchResults;
    }
}
