package com.dglab.cia.util;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterator;
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

    public String requestFile(String url) {
        try {
            return new String(IOUtils.toByteArray(repository.getFileContent(url).read()), "UTF8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
