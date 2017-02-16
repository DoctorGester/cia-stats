package com.dglab.cia.persistence;

import com.dglab.cia.database.TournamentParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author doc
 */
public interface TournamentParticipantsRepository extends JpaRepository<TournamentParticipant, Integer> {
    TournamentParticipant findBySteamId64(long steamId64);
}
