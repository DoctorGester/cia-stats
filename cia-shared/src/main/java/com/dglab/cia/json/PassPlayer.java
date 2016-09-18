package com.dglab.cia.json;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * @author doc
 */
public class PassPlayer {
	@JsonSerialize(using = ToStringSerializer.class)
	private long steamId64;
	private int experience;
	private String name;
    private String avatarUrl;

	public PassPlayer() {}

	public PassPlayer(long steamId64, int experience) {
		this.steamId64 = steamId64;
		this.experience = experience;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSteamId64() {
		return steamId64;
	}

	public void setSteamId64(long steamId64) {
		this.steamId64 = steamId64;
	}

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

	public int getExperience() {
		return experience;
	}

	public void setExperience(int experience) {
		this.experience = experience;
	}
}
