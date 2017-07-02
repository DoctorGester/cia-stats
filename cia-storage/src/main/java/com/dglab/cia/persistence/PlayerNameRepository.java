package com.dglab.cia.persistence;

import com.dglab.cia.database.PlayerName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author doc
 */
public interface PlayerNameRepository extends JpaRepository<PlayerName, Long> {
    List<PlayerName> findTop20ByNameLikeIgnoreCaseOrderByName(String name);
}
