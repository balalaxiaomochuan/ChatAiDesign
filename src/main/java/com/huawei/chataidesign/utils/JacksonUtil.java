package com.huawei.chataidesign.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JacksonUtil {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    public static JsonNode toJson(String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

    public static String toString(JsonNode jsonNode) {
        try {
            return OBJECT_MAPPER.writeValueAsString(jsonNode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to stringify JSON", e);
        }
    }

    public static ArrayNode toArray(String json) {
        try {
            JsonNode node = OBJECT_MAPPER.readTree(json);
            if (node.isArray()) {
                return (ArrayNode) node;
            }
            throw new IllegalArgumentException("JSON is not an array");
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON array", e);
        }
    }

    public static ObjectNode createObjectNode() {
        return OBJECT_MAPPER.createObjectNode();
    }
}
