package com.dglab.cia.json;

import java.time.LocalDate;

/**
 * User: kartemov
 * Date: 05.08.2016
 * Time: 1:09
 */
public class MatchDateCount {
    private LocalDate date;
    private int count;

    public MatchDateCount(LocalDate date, int count) {
        this.date = date;
        this.count = count;
    }
}
