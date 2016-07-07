package com.dglab.cia.json;

/**
 * @author doc
 */
public class Streak {
	private short max, current;

	public Streak(){}

	public Streak(short current, short max) {
		this.current = current;
		this.max = max;
	}

	public short getMax() {
		return max;
	}

	public void setMax(short max) {
		this.max = max;
	}

	public short getCurrent() {
		return current;
	}

	public void setCurrent(short current) {
		this.current = current;
	}
}
