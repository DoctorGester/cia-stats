package com.dglab.cia.persistence;

import org.hibernate.stat.Statistics;

import javax.management.MXBean;

/**
 * User: kartemov
 * Date: 30.09.2016
 * Time: 14:24
 */
@MXBean
public interface StatisticsService extends Statistics {
}
