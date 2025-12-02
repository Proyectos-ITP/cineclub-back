package com.cineclub_backend.cineclub_backend.shared.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class SerializeHelper {

  private static final ObjectMapper mapper = new ObjectMapper();

  public static String toJson(Object obj) {
    try {
      return mapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return "{}";
    }
  }

  public static String toPrettyJson(Object obj) {
    try {
      return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return "{}";
    }
  }

  public static Map<String, Object> toMap(Object obj) {
    return mapper.convertValue(obj, new TypeReference<Map<String, Object>>() {});
  }

  public static <T> T toObject(Object record, Class<T> clazz) {
    return mapper.convertValue(record instanceof Map ? record : Map.of(), clazz);
  }

  public static Map<String, Object> toObjectMap(Map<String, Object> payload) {
    Object recordObj = payload.get("record");

    if (recordObj instanceof Map<?, ?> record) {
      return mapper.convertValue(record, new TypeReference<Map<String, Object>>() {});
    }
    return Map.of();
  }

  public static String toCsv(Object obj) {
    return String.join(
      ",",
      toMap(obj).values().stream().map(Object::toString).toArray(String[]::new)
    );
  }
}
