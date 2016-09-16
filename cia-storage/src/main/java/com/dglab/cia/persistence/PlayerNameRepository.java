package com.dglab.cia.persistence;

import com.dglab.cia.database.*;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author doc
 */
public interface PlayerNameRepository extends JpaRepository<PlayerName, Long> {
}
