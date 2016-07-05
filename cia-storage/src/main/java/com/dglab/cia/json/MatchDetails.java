package com.dglab.cia.json;

import java.util.Collection;

/**
 * @author doc
 */
public class MatchDetails {
	private MatchInfo info;
    private Collection<RoundInfo> rounds;

	public MatchDetails(MatchInfo info, Collection<RoundInfo> rounds) {
		this.info = info;
        this.rounds = rounds;
    }

	public MatchInfo getInfo() {
		return info;
	}

	public void setInfo(MatchInfo info) {
		this.info = info;
	}

	public Collection<RoundInfo> getRounds() {
		return rounds;
	}

	public void setRounds(Collection<RoundInfo> rounds) {
		this.rounds = rounds;
	}
}
