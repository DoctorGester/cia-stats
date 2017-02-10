package com.dglab.cia.data;

import com.dglab.cia.json.util.KeyValueConsumer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: kartemov
 * Date: 10.02.2017
 * Time: 23:07
 */
public class KeyValueCosmetics {
    @KeyValueConsumer(KeyValueHeroCosmetics.class)
    private Map<String, KeyValueHeroCosmetics> heroCosmetics = new LinkedHashMap<>();

    public Map<String, KeyValueHeroCosmetics> getHeroCosmetics() {
        return heroCosmetics;
    }

    public void setHeroCosmetics(Map<String, KeyValueHeroCosmetics> heroCosmetics) {
        this.heroCosmetics = heroCosmetics;
    }
}
