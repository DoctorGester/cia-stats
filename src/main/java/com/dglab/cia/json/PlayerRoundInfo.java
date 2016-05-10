package com.dglab.cia.json;

/**
 * @author doc
 */
public class PlayerRoundInfo {
	private long steamId64;
	private short damageDealt;
	private short projectilesFired;
	private short score;
	private String hero;

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
}
