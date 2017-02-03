package com.dglab.cia.data;

import com.dglab.cia.json.HeroStats;

import java.util.List;

/**
 * Created by shoujo on 2/2/2017.
 */
public class Hero {
    private List<HeroAbility> abilities;
    private HeroStats stats;

    public List<HeroAbility> getAbilities() {
        return abilities;
    }

    public void setAbilities(List<HeroAbility> abilities) {
        this.abilities = abilities;
    }

    public HeroStats getStats() {
        return stats;
    }

    public void setStats(HeroStats stats) {
        this.stats = stats;
    }
}
