package com.dglab.cia.database;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.YearMonth;

/**
 * @author doc
 */
@Entity(name = "subscriptions")
public class Subscription {
    private long steamId64;
    private YearMonth subscription;

    @Id
    public long getSteamId64() {
        return steamId64;
    }

    public void setSteamId64(long steamId64) {
        this.steamId64 = steamId64;
    }

    @Column(nullable = false)
    public YearMonth getSubscription() {
        return subscription;
    }

    public void setSubscription(YearMonth subscription) {
        this.subscription = subscription;
    }
}
