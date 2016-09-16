package com.dglab.cia.database;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author doc
 */
@Entity(name = "quest_rerolls")
public class QuestReroll implements Serializable {
    private long steamId64;

    @Id
    public long getSteamId64() {
        return steamId64;
    }

    public void setSteamId64(long steamId64) {
        this.steamId64 = steamId64;
    }
}
