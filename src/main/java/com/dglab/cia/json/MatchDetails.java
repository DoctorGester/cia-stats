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
}
