package com.dglab.cia.database;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;

/**
 * @author doc
 */
@Entity(name = "tournament_participants")
public class TournamentParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private long steamId64;

    @Column(nullable = false)
    private boolean isReplacementPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(
            name = "steamId64",
            referencedColumnName = "steamId64",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    private PlayerName name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getSteamId64() {
        return steamId64;
    }

    public void setSteamId64(long steamId64) {
        this.steamId64 = steamId64;
    }

    public boolean isReplacementPlayer() {
        return isReplacementPlayer;
    }

    public void setReplacementPlayer(boolean replacementPlayer) {
        isReplacementPlayer = replacementPlayer;
    }

    public PlayerName getName() {
        return name;
    }

    public void setName(PlayerName name) {
        this.name = name;
    }

}
