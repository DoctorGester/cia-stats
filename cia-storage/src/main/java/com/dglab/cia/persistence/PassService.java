package com.dglab.cia.persistence;

import com.dglab.cia.database.PassOwner;
import com.dglab.cia.json.PassPlayer;

import java.util.List;

/**
 * @author doc
 */
public interface PassService {
    PassOwner getOrCreate(long steamId64);
    List<PassPlayer> getTopPlayers();
    void awardExperience(long steamId64, int experience);
}
