package com.dglab.cia.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author doc
 */
public class PlayerRoundInfo {
	private long steamId64;
	private short damageDealt;
	private short projectilesFired;
	private short score;
	private byte connectionState;
	private String hero;

    public PlayerRoundInfo(){}

	@JsonCreator
	public PlayerRoundInfo(
			@JsonProperty(value = "steamId64", required = true) long steamId64,
			@JsonProperty(value = "damageDealt", required = true) short damageDealt,
			@JsonProperty(value = "projectilesFired", required = true) short projectilesFired,
			@JsonProperty(value = "score", required = true) short score,
			@JsonProperty(value = "connectionState", required = true) byte connectionState
	) {
		this.steamId64 = steamId64;
		this.damageDealt = damageDealt;
		this.projectilesFired = projectilesFired;
		this.score = score;
		this.connectionState = connectionState;
	}

	public long getSteamId64() {
		return steamId64;
	}

	public void setSteamId64(long steamId64) {
		this.steamId64 = steamId64;
	}

	public short getDamageDealt() {
		return damageDealt;
	}

	public void setDamageDealt(short damageDealt) {
		this.damageDealt = damageDealt;
	}

	public short getProjectilesFired() {
		return projectilesFired;
	}

	public void setProjectilesFired(short projectilesFired) {
		this.projectilesFired = projectilesFired;
	}

	public short getScore() {
		return score;
	}

	public void setScore(short score) {
		this.score = score;
	}

	public String getHero() {
		return hero;
	}

	public void setHero(String hero) {
		this.hero = hero;
	}

	public byte getConnectionState() {
		return connectionState;
	}

	public void setConnectionState(byte connectionState) {
		this.connectionState = connectionState;
	}
}
