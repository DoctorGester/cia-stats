package com.dglab.cia;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import spark.ResponseTransformer;
import spark.Spark;

import java.util.HashMap;

/**
 * @author doc
 */
public class JsonUtil {
	@Autowired
	private ObjectMapper mapper;

	public String toJson(Object object) {
		try {
			return mapper.writeValueAsString(object != null ? object : new HashMap());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			Spark.halt(500);
		}

		return "";
	}

	public ResponseTransformer json() {
		return this::toJson;
	}
}
