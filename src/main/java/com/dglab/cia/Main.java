package com.dglab.cia;

/**
 * @author doc
 */
public class Main {
	public static void main(String ... args) {
		if (args.length != 1) {
			throw new RuntimeException("Please specify service type, i.e. 'stats' or 'storage'");
		}

		switch (args[0]) {
			case "stats": new StatsApplication(); break;
			case "storage": new StorageApplication(); break;
		}
	}
}
