package com.dglab.cia.persistence;

import com.dglab.cia.database.QuestReroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author doc
 */
public interface QuestRerollsRepository extends JpaRepository<QuestReroll, Long> {
    @Query(value = "TRUNCATE TABLE quest_rerolls", nativeQuery = true)
    @Modifying
    @Transactional
    void truncate();
}
