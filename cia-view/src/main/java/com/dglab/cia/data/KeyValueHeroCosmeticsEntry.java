package com.dglab.cia.data;

import com.dglab.cia.json.util.KeyValueTarget;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: kartemov
 * Date: 10.02.2017
 * Time: 22:49
 */
public class KeyValueHeroCosmeticsEntry {
    private String hero;

    @KeyValueTarget
    private String type;

    @KeyValueTarget
    private Integer level;

    @KeyValueTarget
    private String item;

    @KeyValueTarget
    private String set;

    @KeyValueTarget
    private Map<String, String> taunt = new LinkedHashMap<>();

    @KeyValueTarget
    private String emote;

    public String getHero() {
        return hero;
    }

    public void setHero(String hero) {
        this.hero = hero;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getSet() {
        return set;
    }

    public void setSet(String set) {
        this.set = set;
    }

    public Map<String, String> getTaunt() {
        return taunt;
    }

    public void setTaunt(Map<String, String> taunt) {
        this.taunt = taunt;
    }

    public String getEmote() {
        return emote;
    }

    public void setEmote(String emote) {
        this.emote = emote;
    }
}
