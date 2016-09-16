package com.dglab.cia.persistence;

import com.dglab.cia.database.Quest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author doc
 */
public interface QuestsRepository extends JpaRepository<Quest, Long> {
    List<Quest> findBySteamId64(long steamId64);
}
