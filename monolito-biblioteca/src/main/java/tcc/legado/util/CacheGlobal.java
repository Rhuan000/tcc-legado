package tcc.legado.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.IntFunction;

public class CacheGlobal {

    private static final Map<String, Object> CACHE = new HashMap<>();
    private static final ConcurrentMap<Integer, List<LocalDate>> FERIADOS_POR_ANO =
            new ConcurrentHashMap<>();

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

    public static List<LocalDate> getFeriados(int ano) {
        return FERIADOS_POR_ANO.get(ano);
    }

    public static List<LocalDate> getFeriados(int ano,
                                               IntFunction<List<LocalDate>> carregador) {
        return FERIADOS_POR_ANO.computeIfAbsent(ano, chave -> copiar(carregador.apply(chave)));
    }

    public static void setFeriados(int ano, List<LocalDate> feriados) {
        FERIADOS_POR_ANO.put(ano, copiar(feriados));
    }

    public static void clearFeriados() {
        FERIADOS_POR_ANO.clear();
    }

    private static List<LocalDate> copiar(List<LocalDate> feriados) {
        return Collections.unmodifiableList(new ArrayList<>(feriados));
    }
}
