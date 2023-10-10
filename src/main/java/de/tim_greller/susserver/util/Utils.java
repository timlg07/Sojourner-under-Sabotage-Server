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
}
