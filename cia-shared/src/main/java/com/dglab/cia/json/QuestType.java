package com.dglab.cia.json;

/**
 * @author doc
 */
public enum QuestType {
    PLAY_ROUNDS_AS(20, 150),
    PLAY_ROUNDS_AS_OR(25, 100),
    PLAY_GAMES(5, 150),
    DEAL_DAMAGE(100, 200),
    EARN_MVP(10, 200),
    EARN_FIRST_BLOOD(12, 150),
    MAKE_KILLS(20, 150);

    private final int goal;
    private final int reward;

    QuestType(int goal, int reward) {
        this.goal = goal;
        this.reward = reward;
    }

    public int getGoal() {
        return goal;
    }

    public int getReward() {
        return reward;
    }
}
