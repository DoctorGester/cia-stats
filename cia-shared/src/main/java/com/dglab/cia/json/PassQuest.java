package com.dglab.cia.json;

/**
 * @author doc
 */
public class PassQuest {
    private long id;
    private QuestType type;
    private short progress;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public QuestType getType() {
        return type;
    }

    public void setType(QuestType type) {
        this.type = type;
    }

    public short getProgress() {
        return progress;
    }

    public void setProgress(short progress) {
        this.progress = progress;
    }
}
