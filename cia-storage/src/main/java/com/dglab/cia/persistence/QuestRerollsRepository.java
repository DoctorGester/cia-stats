package com.dglab.cia.persistence;

import com.dglab.cia.database.QuestReroll;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author doc
 */
public interface QuestRerollsRepository extends JpaRepository<QuestReroll, Long> {
}
