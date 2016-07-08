package com.dglab.cia.persistence;

import java.util.Collection;
import java.util.concurrent.Future;

/**
 * @author doc
 */
public interface PlayerNameService {
	Future<?> updatePlayerNames(Collection<Long> steamId64);
}
