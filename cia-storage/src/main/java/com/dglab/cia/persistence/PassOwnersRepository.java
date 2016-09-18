package com.dglab.cia.persistence;

import com.dglab.cia.database.PassOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author doc
 */
public interface PassOwnersRepository extends JpaRepository<PassOwner, Long> {
    List<PassOwner> findTop5ByOrderByExperienceDesc();
}
