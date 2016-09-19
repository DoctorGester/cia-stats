package com.dglab.cia.database;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.time.Instant;

/**
 * @author doc
 */
@Entity(name = "pass_owners")
public class PassOwner {
    private long steamId64;
    private int experience;
    private Instant lastQuestUpdate;
    private PlayerName name;
    private boolean isNew = false;

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
    public Instant getLastQuestUpdate() {
        return lastQuestUpdate;
    }

    public void setLastQuestUpdate(Instant lastQuestUpdate) {
        this.lastQuestUpdate = lastQuestUpdate;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(
            name = "steamId64",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    public PlayerName getName() {
        return name;
    }

    public void setName(PlayerName name) {
        this.name = name;
    }

    @Transient
    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }
}
