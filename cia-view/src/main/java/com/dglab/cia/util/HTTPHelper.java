package com.dglab.cia.util;

import com.dglab.cia.json.util.ObjectMapperFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * @author doc
 */
public class HTTPHelper {
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = ObjectMapperFactory.createObjectMapper();

    public static <T> T urlToObject(String query, TypeReference<T> type) {
        Request.Builder builder = new Request.Builder().get().url(query);
        Request built = builder.build();

        try {
            Response answer = client.newCall(built).execute();

            if (answer == null) {
                throw new RuntimeException();
            }

            return mapper.readValue(IOUtils.toByteArray(answer.body().byteStream()), type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
