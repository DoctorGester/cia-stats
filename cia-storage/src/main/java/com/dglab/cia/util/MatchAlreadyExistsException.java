package com.dglab.cia.util;

/**
 * @author doc
 */
public class MatchAlreadyExistsException extends Exception {
    private long matchId;

    public MatchAlreadyExistsException(long matchId) {
        this.matchId = matchId;
    }
}
