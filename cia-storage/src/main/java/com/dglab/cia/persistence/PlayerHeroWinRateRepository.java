package com.dglab.cia.persistence;

import com.dglab.cia.database.PlayerHeroWinRate;
import com.dglab.cia.database.PlayerHeroWinRateKey;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author doc
 */
public interface PlayerHeroWinRateRepository extends JpaRepository<PlayerHeroWinRate, PlayerHeroWinRateKey> {
}
