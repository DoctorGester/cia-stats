package com.dglab.cia.json;

import java.time.LocalDate;
import java.util.Map;

/**
 * User: kartemov
 * Date: 05.08.2016
 * Time: 1:12
 */
public class MatchCounts {
    private Map<LocalDate, Integer> counts;

    public Map<LocalDate, Integer> getCounts() {
        return counts;
    }

    public void setCounts(Map<LocalDate, Integer> counts) {
        this.counts = counts;
    }
}
