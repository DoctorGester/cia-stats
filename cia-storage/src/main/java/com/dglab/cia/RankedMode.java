package com.dglab.cia;

/**
 * @author doc
 */
public enum RankedMode {
	FFA_FOUR((byte) 3),
	TWO_TEAMS((byte) 1),
	DUEL((byte) 1);

	private byte stars;

	RankedMode(byte stars) {
		this.stars = stars;
	}

	public byte getStars() {
		return stars;
	}
}
