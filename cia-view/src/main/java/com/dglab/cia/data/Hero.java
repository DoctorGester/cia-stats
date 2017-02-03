package com.dglab.cia.data;

import com.dglab.cia.json.HeroStats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shoujo on 2/2/2017.
 */
public class Hero {
    private List<HeroAbility> abilities;
    private HeroStats stats;
    private Map<String, String> name = new HashMap<>();

    public void putName(String language, String nm) {
        name.put(language, nm);
    }

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

    public Map<String, String> getName() {
        return name;
    }
}
