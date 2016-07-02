package com.dglab.cia.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author doc
 */
public class RankAndStars {
	private byte rank;
	private byte stars;

	@JsonCreator
	public RankAndStars(
			@JsonProperty(value = "rank") byte rank,
			@JsonProperty(value = "stars") byte stars) {
		this.rank = rank;
		this.stars = stars;
	}

	public byte getRank() {
		return rank;
	}

	public void setRank(byte rank) {
		this.rank = rank;
	}

	public byte getStars() {
		return stars;
	}

	public void setStars(byte stars) {
		this.stars = stars;
	}
}
