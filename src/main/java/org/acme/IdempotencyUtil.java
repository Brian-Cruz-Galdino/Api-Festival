package org.acme;

import jakarta.ws.rs.core.Response;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IdempotencyUtil {
    private static final Map<String, Response> idempotencyMap = new ConcurrentHashMap<>();

    public static Response getResponseIfExists(String key) {
        return idempotencyMap.get(key);
    }

    public static void storeResponse(String key, Response response) {
        idempotencyMap.put(key, response);
    }
}