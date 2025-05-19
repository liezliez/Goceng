package id.co.bcaf.goceng.utils;

import java.lang.reflect.Field;

public class NullAwareBeanUtils {

    public static void copyNonNullProperties(Object src, Object target) {
        Field[] fields = src.getClass().getDeclaredFields();

        for (Field srcField : fields) {
            try {
                srcField.setAccessible(true);
                Object value = srcField.get(src);
                if (value != null) {
                    Field targetField = getField(target.getClass(), srcField.getName());
                    if (targetField != null) {
                        targetField.setAccessible(true);
                        targetField.set(target, value);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error copying properties", e);
            }
        }
    }

    private static Field getField(Class<?> clazz, String name) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}
