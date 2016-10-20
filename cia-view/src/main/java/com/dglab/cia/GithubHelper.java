package com.dglab.cia;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.extras.OkHttpConnector;

import java.io.File;
import java.io.IOException;

/**
 * User: kartemov
 * Date: 07.10.2016
 * Time: 20:39
 */
public class GithubHelper {
    private GHRepository repository;

    public GithubHelper() {
        try {
            Cache cache = new Cache(new File("cache"), 1024 * 1024);
            String token = FileUtils.readFileToString(new File("token.key"));
            GitHub gitHub = GitHub.connectUsingOAuth(token);
            gitHub.setConnector(new OkHttpConnector(new OkUrlFactory(new OkHttpClient().setCache(cache))));
            repository = gitHub.getRepository("doctorgester/crumbling-island-arena");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String requestFile(String url) {
        try {
            return Base64.encodeBase64String(IOUtils.toByteArray(repository.getFileContent(url).read()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public String getHeroesBase64() {
        return requestFile("game/scripts/npc/npc_heroes_custom.txt");
    }

    public String getAbilitiesBase64() {
        return requestFile("game/scripts/npc/npc_abilities_custom.txt");
    }

    public String getLocalizationBase64() {
        return requestFile("game/panorama/localization/addon_english.txt");
    }

    public String getLocalizationRussianBase64() {
        return requestFile("game/panorama/localization/addon_russian.txt");
    }
}
