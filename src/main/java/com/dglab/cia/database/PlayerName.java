package com.dglab.cia.database;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;

/**
 * @author doc
 */

@Entity
@Table(name = "player_names")
public class PlayerName implements Serializable {
	private long steamId64;
	private String name;

	@Id
	@Column(name = "steamId64")
	public long getSteamId64() {
		return steamId64;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setSteamId64(long steamId64) {
		this.steamId64 = steamId64;
	}

	public void setName(String name) {
		this.name = name;
	}
}
