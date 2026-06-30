package com.jeh.common.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HydrationCache {
    private static final Map<UUID, HydrationData> cache = new HashMap<>();

    public static void save(UUID uuid, HydrationData data) {
        cache.put(uuid, data.copy());
    }

    public static HydrationData load(UUID uuid) {
        HydrationData data = cache.get(uuid);
        return data != null ? data.copy() : null;
    }

    public static void remove(UUID uuid) {
        cache.remove(uuid);
    }
}
