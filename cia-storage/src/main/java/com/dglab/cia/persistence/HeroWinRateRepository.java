package com.dglab.cia.persistence;

import com.dglab.cia.database.HeroWinRate;
import com.dglab.cia.database.HeroWinRateKey;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author doc
 */
public interface HeroWinRateRepository extends JpaRepository<HeroWinRate, HeroWinRateKey> {
}
