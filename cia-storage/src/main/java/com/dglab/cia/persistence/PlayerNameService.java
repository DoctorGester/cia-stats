package com.dglab.cia.persistence;

import java.util.concurrent.Future;

/**
 * @author doc
 */
public interface PlayerNameService {
	Future<String> updatePlayerName(long steamId64);
}
