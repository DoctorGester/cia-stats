package com.dglab.cia.data;

/**
 * Created by shoujo on 2/2/2017.
 */
public class HeroAbility {
    private String name;
    private Integer damage;
    private Double cooldown;
    private String texture;
    private HeroAbility sub;

    public String getTexture() {
        return texture;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public HeroAbility getSub() {
        return sub;
    }

    public void setSub(HeroAbility sub) {
        this.sub = sub;
    }
}
