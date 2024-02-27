package io.github.xiaou66.sdk.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author xiaou
 * @date 2024/2/23
 */
public final class ReflectUtils {

    private ReflectUtils() {}

    /**
     * 提取字段
     * @param o 带提取提字段的对象
     * @return
     */
    public static Map<String, String> extractFields(Object o) {
        Map<String, String> map = new HashMap<>();

        for (Field field : FieldUtils.getAllFields(o.getClass())) {
            try {
                field.setAccessible(true);
                Object value = field.get(o);
                if (Objects.isNull(value)) {
                    continue;
                }
                String name = Optional.ofNullable(field.getAnnotation(JsonProperty.class))
                        .map(JsonProperty::value)
                        .orElse(field.getName());
                map.put(name, value.toString());
            } catch (Exception ignore) {}
        }
        return map;
    }
}
