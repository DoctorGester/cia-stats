package com.dglab.cia.data;

import com.dglab.cia.json.util.KeyValueConsumer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: kartemov
 * Date: 10.02.2017
 * Time: 22:43
 */
public class KeyValueHeroCosmetics {
    @KeyValueConsumer(value = KeyValueHeroCosmeticsEntry.class, ignored = { "ignore" })
    private Map<String, KeyValueHeroCosmeticsEntry> entries = new LinkedHashMap<>();

    public Map<String, KeyValueHeroCosmeticsEntry> getEntries() {
        return entries;
    }

    public void setEntries(Map<String, KeyValueHeroCosmeticsEntry> entries) {
        this.entries = entries;
    }
}
