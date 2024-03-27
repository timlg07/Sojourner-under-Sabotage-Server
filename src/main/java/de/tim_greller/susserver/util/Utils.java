package de.tim_greller.susserver.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Utils {

    public static <Key, ValueBefore, ValueAfter> Map<Key, ValueAfter> mapMap(
            Map<Key, ValueBefore> map,
            BiFunction<Key, ValueBefore, ValueAfter> mapMapper
    ) {
        final Map<Key, ValueAfter> mappedMap = new HashMap<>();
        for (Map.Entry<Key, ValueBefore> e : map.entrySet()) {
            mappedMap.put(e.getKey(), mapMapper.apply(e.getKey(), e.getValue()));
        }
        return mappedMap;
    }

    public static <Key, Value> Map<Key, Value> filterMap(
            Map<Key, Value> map,
            BiFunction<Key, Value, Boolean> filter
    ) {
        final Map<Key, Value> filteredMap = new HashMap<>();
        for (Map.Entry<Key, Value> e : map.entrySet()) {
            if (filter.apply(e.getKey(), e.getValue())) {
                filteredMap.put(e.getKey(), e.getValue());
            }
        }
        return filteredMap;
    }
}
