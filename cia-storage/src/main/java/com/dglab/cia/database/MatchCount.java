package com.dglab.cia.database;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author doc
 */
@Entity
@Table(name = "match_counts")
public class MatchCount {
    private MatchKey pk;
    private int matches;

    @EmbeddedId
    public MatchKey getPk() {
        return pk;
    }

    @Column(name = "matches", nullable = false)
    public int getMatches() {
        return matches;
    }

    public void setMatches(int matches) {
        this.matches = matches;
    }

    public void setPk(MatchKey pk) {
        this.pk = pk;
    }
}
