package com.cineclub_backend.cineclub_backend.shared.helpers;

import java.util.Map;

public interface ModelSerializables {
  default String toLinearJson() {
    return SerializeHelper.toJson(this);
  }

  default String toJson() {
    return SerializeHelper.toPrettyJson(this);
  }

  default String toCsv() {
    return SerializeHelper.toCsv(this);
  }

  default Map<String, Object> toMap() {
    return SerializeHelper.toMap(this);
  }
}
