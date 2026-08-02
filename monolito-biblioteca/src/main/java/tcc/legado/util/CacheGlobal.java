package tcc.legado.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheGlobal {

    private static final Map<String, Object> CACHE = new HashMap<>();

    static {
        // Inicializa com lista vazia (isso faz o sistema tentar buscar na API ao primeiro uso)
        CACHE.put("feriados", new ArrayList<LocalDate>());
        CACHE.put("anoCorrente", LocalDate.now().getYear());
    }

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

    @SuppressWarnings("unchecked")
    public static List<LocalDate> getFeriados() {
        return (List<LocalDate>) get("feriados");
    }

    public static void setFeriados(List<LocalDate> feriados) {
        put("feriados", feriados);
    }

    public static int getAnoCorrente() {
        return (int) get("anoCorrente");
    }

    public static void setAnoCorrente(int ano) {
        put("anoCorrente", ano);
    }
}
