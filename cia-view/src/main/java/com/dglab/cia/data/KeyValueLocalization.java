package com.dglab.cia.data;

import com.dglab.cia.json.util.KeyValueTarget;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shoujo on 2/2/2017.
 */
public class KeyValueLocalization {
    @KeyValueTarget("language")
    private String language;

    @KeyValueTarget("Tokens")
    private Map<String, String> tokens = new HashMap<>();

    public String getString(String token) {
        return tokens.get(token);
    }
}
