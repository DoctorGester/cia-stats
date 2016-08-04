package com.dglab.cia.database;

import com.dglab.cia.json.MatchMap;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;

/**
 * @author doc
 */
@Embeddable
public class MatchKey  implements Serializable {
    private String mode;
    private byte players;
    private MatchMap map;

    @Column(name = "playerAmount", nullable = false, updatable = false)
    public byte getPlayers() {
        return players;
    }

    @Column(name = "\"mode\"", nullable = false, updatable = false)
    public String getMode() {
        return mode;
    }

    @Column(name = "\"map\"", nullable = false, updatable = false)
    @Enumerated(EnumType.ORDINAL)
    public MatchMap getMap() {
        return map;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setPlayers(byte players) {
        this.players = players;
    }

    public void setMap(MatchMap map) {
        this.map = map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MatchKey matchKey = (MatchKey) o;

        if (players != matchKey.players) return false;
        if (mode != null ? !mode.equals(matchKey.mode) : matchKey.mode != null) return false;
        return map == matchKey.map;

    }

    @Override
    public int hashCode() {
        int result = mode != null ? mode.hashCode() : 0;
        result = 31 * result + (int) players;
        result = 31 * result + (map != null ? map.hashCode() : 0);
        return result;
    }
}
