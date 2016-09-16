package com.dglab.cia.database;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

/**
 * @author doc
 */
@Entity(name = "pass_owners")
public class PassOwner {
    private long steamId64;
    private int experience;
    private Instant lastActivity;

    @Id
    public long getSteamId64() {
        return steamId64;
    }

    public void setSteamId64(long steamId64) {
        this.steamId64 = steamId64;
    }

    @Column(nullable = false)
    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    @Column(nullable = false)
    public Instant getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Instant lastActivity) {
        this.lastActivity = lastActivity;
    }
}
