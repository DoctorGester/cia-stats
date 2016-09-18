package com.dglab.cia.database;

import com.dglab.cia.json.QuestType;

import javax.persistence.*;

/**
 * @author doc
 */
@Entity(name = "quests")
public class Quest {
    private long id;
    private long steamId64;
    private QuestType questType;
    private short progress;
    private String hero;
    private String secondaryHero;
    private boolean isNew = false;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "steamId64")
    public long getSteamId64() {
        return steamId64;
    }

    public void setSteamId64(long steamId64) {
        this.steamId64 = steamId64;
    }

    @Column(nullable = false)
    public short getProgress() {
        return progress;
    }

    public void setProgress(short progress) {
        this.progress = progress;
    }

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public QuestType getQuestType() {
        return questType;
    }

    public void setQuestType(QuestType questType) {
        this.questType = questType;
    }

    @Column
    public String getHero() {
        return hero;
    }

    public void setHero(String hero) {
        this.hero = hero;
    }

    @Column
    public String getSecondaryHero() {
        return secondaryHero;
    }

    public void setSecondaryHero(String secondaryHero) {
        this.secondaryHero = secondaryHero;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public boolean isNew() {
        return isNew;
    }
}
