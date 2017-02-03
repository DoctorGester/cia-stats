package com.dglab.cia.data;

import com.dglab.cia.json.util.KeyValueTarget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shoujo on 2/2/2017.
 */
public class KeyValueHero {
    @KeyValueTarget("Ability0")
    @KeyValueTarget("Ability1")
    @KeyValueTarget("Ability2")
    @KeyValueTarget("Ability3")
    @KeyValueTarget("Ability4")
    @KeyValueTarget("Ability5")
    @KeyValueTarget("Ability6")
    @KeyValueTarget("Ability7")
    @KeyValueTarget("Ability8")
    @KeyValueTarget("Ability9")
    private List<String> abilities = new ArrayList<>();

    @KeyValueTarget("CustomIcons")
    private Map<String, String> customIcons = new HashMap<>();

    public Map<String, String> getCustomIcons() {
        return customIcons;
    }

    public List<String> getAbilities() {
        return abilities;
    }
}
