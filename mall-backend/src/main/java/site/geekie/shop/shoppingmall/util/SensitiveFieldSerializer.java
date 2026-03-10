package site.geekie.shop.shoppingmall.util;

import site.geekie.shop.shoppingmall.annotation.SensitiveField;
import site.geekie.shop.shoppingmall.annotation.SensitiveType;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class SensitiveFieldSerializer {

    private static final int MAX_DEPTH = 3;
    private static final int MAX_STRING_LENGTH = 200;
    private static final String MASKED = "******";
    private static final Set<String> SENSITIVE_FIELD_NAMES = Set.of(
            "password", "secret", "token", "key", "credential", "authorization"
    );

    private static final ConcurrentHashMap<Class<?>, List<FieldMeta>> FIELD_CACHE = new ConcurrentHashMap<>();

    private SensitiveFieldSerializer() {}

    public static String serialize(Object obj) {
        if (obj == null) return "null";
        try {
            StringBuilder sb = new StringBuilder();
            serialize(obj, sb, 0);
            return sb.toString();
        } catch (Exception e) {
            return "[serialize error]";
        }
    }

    private static void serialize(Object obj, StringBuilder sb, int depth) throws IllegalAccessException {
        if (obj == null) {
            sb.append("null");
            return;
        }

        Class<?> clazz = obj.getClass();

        if (isSimpleType(clazz)) {
            String value = obj.toString();
            if (value.length() > MAX_STRING_LENGTH) {
                value = value.substring(0, MAX_STRING_LENGTH) + "...(truncated)";
            }
            sb.append(value);
            return;
        }

        if (obj instanceof Collection<?> collection) {
            sb.append("[Collection size=").append(collection.size()).append("]");
            return;
        }

        if (obj instanceof Map<?, ?> map) {
            sb.append("[Map size=").append(map.size()).append("]");
            return;
        }

        if (clazz.isArray()) {
            sb.append("[Array length=").append(java.lang.reflect.Array.getLength(obj)).append("]");
            return;
        }

        if (depth >= MAX_DEPTH) {
            sb.append("[...]");
            return;
        }

        List<FieldMeta> fieldMetas = FIELD_CACHE.computeIfAbsent(clazz, SensitiveFieldSerializer::extractFields);
        sb.append("{");
        boolean first = true;
        for (FieldMeta meta : fieldMetas) {
            if (!first) sb.append(", ");
            first = false;
            sb.append(meta.name).append("=");

            Object value = meta.field.get(obj);
            if (value == null) {
                sb.append("null");
            } else if (meta.sensitiveType != null) {
                sb.append(mask(value.toString(), meta.sensitiveType));
            } else if (isSensitiveByName(meta.name)) {
                sb.append(MASKED);
            } else {
                serialize(value, sb, depth + 1);
            }
        }
        sb.append("}");
    }

    private static List<FieldMeta> extractFields(Class<?> clazz) {
        List<FieldMeta> metas = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
                field.setAccessible(true);
                SensitiveField annotation = field.getAnnotation(SensitiveField.class);
                SensitiveType type = annotation != null ? annotation.value() : null;
                metas.add(new FieldMeta(field, field.getName(), type));
            }
            current = current.getSuperclass();
        }
        return metas;
    }

    private static boolean isSensitiveByName(String name) {
        String lower = name.toLowerCase();
        return SENSITIVE_FIELD_NAMES.stream().anyMatch(lower::contains);
    }

    private static String mask(String value, SensitiveType type) {
        if (value == null || value.isEmpty()) return MASKED;
        return switch (type) {
            case PASSWORD -> MASKED;
            case PHONE -> value.length() >= 7
                    ? value.substring(0, 3) + "****" + value.substring(value.length() - 4)
                    : MASKED;
            case EMAIL -> {
                int at = value.indexOf('@');
                yield at > 1 ? value.charAt(0) + "**" + value.substring(at) : MASKED;
            }
            case ID_CARD -> value.length() >= 8
                    ? value.substring(0, 4) + "**********" + value.substring(value.length() - 4)
                    : MASKED;
            case TOKEN -> value.length() > 8
                    ? value.substring(0, 4) + "****" + value.substring(value.length() - 4)
                    : MASKED;
            case DEFAULT -> MASKED;
        };
    }

    private static boolean isSimpleType(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == String.class
                || Number.class.isAssignableFrom(clazz)
                || clazz == Boolean.class
                || clazz == Character.class
                || clazz.isEnum()
                || java.time.temporal.Temporal.class.isAssignableFrom(clazz)
                || java.util.Date.class.isAssignableFrom(clazz);
    }

    private record FieldMeta(Field field, String name, SensitiveType sensitiveType) {}
}
