package com.dglab.cia.json;

import java.util.Map;

/**
 * @author doc
 */
public class RankUpdateDetails {
	private Map<Long, RankAndStars> previous;
	private Map<Long, RankAndStars> updated;

	public Map<Long, RankAndStars> getPrevious() {
		return previous;
	}

	public void setPrevious(Map<Long, RankAndStars> previous) {
		this.previous = previous;
	}

	public Map<Long, RankAndStars> getUpdated() {
		return updated;
	}

	public void setUpdated(Map<Long, RankAndStars> updated) {
		this.updated = updated;
	}
}

