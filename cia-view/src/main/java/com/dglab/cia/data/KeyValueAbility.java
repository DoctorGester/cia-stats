package com.dglab.cia.data;

import com.dglab.cia.json.util.KeyValueTarget;

/**
 * Created by shoujo on 2/2/2017.
 */
public class KeyValueAbility {
    @KeyValueTarget("AbilityTextureName")
    private String texture;

    @KeyValueTarget("Damage")
    private Integer damage;

    @KeyValueTarget("AbilityCooldown")
    private Double cooldown;

    public String getTexture() {
        return texture;
    }

    public Integer getDamage() {
        return damage;
    }

    public void setDamage(Integer damage) {
        this.damage = damage;
    }

    public Double getCooldown() {
        return cooldown;
    }

    public void setCooldown(Double cooldown) {
        this.cooldown = cooldown;
    }
}
