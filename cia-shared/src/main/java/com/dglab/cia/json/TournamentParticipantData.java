package com.dglab.cia.json;

/**
 * @author doc
 */
public class TournamentParticipantData {
    private long steamId64;
    private boolean isReplacement;
    private String name;
    private String avatarUrl;

    public long getSteamId64() {
        return steamId64;
    }

    public void setSteamId64(long steamId64) {
        this.steamId64 = steamId64;
    }

    public boolean isReplacement() {
        return isReplacement;
    }

    public void setReplacement(boolean replacement) {
        isReplacement = replacement;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
