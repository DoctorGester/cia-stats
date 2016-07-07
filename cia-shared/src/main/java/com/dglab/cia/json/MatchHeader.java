package com.dglab.cia.json;

import java.time.Instant;

/**
 * User: kartemov
 * Date: 05.07.2016
 * Time: 22:28
 */
public class MatchHeader {
    private long matchId;
    private String mode;
    private Instant dateTime;

    public MatchHeader(long matchId, String mode, Instant dateTime) {
        this.matchId = matchId;
        this.mode = mode;
        this.dateTime = dateTime;
    }

    public long getMatchId() {
        return matchId;
    }

    public void setMatchId(long matchId) {
        this.matchId = matchId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Instant getDateTime() {
        return dateTime;
    }

    public void setDateTime(Instant dateTime) {
        this.dateTime = dateTime;
    }
}
