package com.dglab.cia;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import spark.ResponseTransformer;
import spark.Spark;

/**
 * @author doc
 */
public class JsonUtil {
	public static String toJson(Object object) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

		try {
			return mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			Spark.halt(500);
		}

		return "";
	}

	public static ResponseTransformer json() {
		return JsonUtil::toJson;
	}
}
