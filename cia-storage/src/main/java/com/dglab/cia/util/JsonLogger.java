package com.dglab.cia.util;

import com.dglab.cia.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import spark.Request;

/**
 * @author doc
 */
public class JsonLogger {
    private static Logger log = LoggerFactory.getLogger(JsonLogger.class);

    @Autowired
    private JsonUtil jsonUtil;

    public void log(String uri, Object object) {
        log.info("{} {}", uri, jsonUtil.toJson(object));
    }

    public void log(Request request) {
        log.info("{} {}", request.requestMethod() + request.uri(), request.raw().getParameter("data"));
    }
}
