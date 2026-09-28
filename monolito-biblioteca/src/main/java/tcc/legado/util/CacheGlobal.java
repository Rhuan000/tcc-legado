package tcc.legado.util;

import java.util.HashMap;
import java.util.Map;

public class CacheGlobal {

    private static final Map<String, Object> CACHE = new HashMap<>();

    public static void put(String key, Object value) {
        synchronized (CACHE) {
            CACHE.put(key, value);
        }
    }

    public static Object get(String key) {
        synchronized (CACHE) {
            return CACHE.get(key);
        }
    }
}
