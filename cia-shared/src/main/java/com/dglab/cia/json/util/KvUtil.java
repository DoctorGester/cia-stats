package com.dglab.cia.json.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Created by shoujo on 2/1/2017.
 */
@SuppressWarnings("unchecked")
public class KvUtil {
    public static Map<String, Object> parseKV(String content) {
        Map<String, Object> result = new LinkedHashMap<>();

        parseKV(content, result);

        return result;
    }

    private static final int STATE_INITIAL = 0;
    private static final int STATE_IN_KEY = 1;
    private static final int STATE_SKIP_TO_VALUE = 2;
    private static final int STATE_IN_VALUE = 3;
    private static final int STATE_COMMENT = 4;

    private static int parseKV(String content, Map<String, Object> target) {
        int index = 0;
        int state = 0;
        int preCommentState = -1;
        String buffer = "";
        String lastKey = null;
        char prevToken = '\0';

        while (index < content.length()) {
            char token = content.charAt(index);

            if (token == '}' && state != 1 && state != 3 && state != STATE_COMMENT) {
                return index;
            }

            if (token == '\"' && prevToken  != '\\' && state != STATE_COMMENT) {
                if (state == STATE_INITIAL) {
                    state = STATE_IN_KEY;
                } else if (state == STATE_IN_KEY) {
                    state = STATE_SKIP_TO_VALUE;
                    lastKey = buffer;
                    buffer = "";
                } else if (state == STATE_SKIP_TO_VALUE) {
                    state = STATE_IN_VALUE;
                } else if (state == STATE_IN_VALUE) {
                    target.put(lastKey, buffer);
                    buffer = "";
                    state = 0;
                }
            } else if (token == '{' && state == STATE_SKIP_TO_VALUE) {
                Map<String, Object> internal = new LinkedHashMap<>();
                index += parseKV(content.substring(index), internal);
                target.put(lastKey, internal);
                state = STATE_INITIAL;
            } else if (token == '/' && prevToken == '/' && state != STATE_COMMENT) {
                preCommentState = state;
                state = STATE_COMMENT;
            } else if (token == '\n' && state == STATE_COMMENT) {
                state = preCommentState;
            } else if ((state == STATE_IN_KEY || state == STATE_IN_VALUE) && token != '\r') {
                buffer = buffer + token;
            }

            prevToken = token;
            index++;
        }

        return index;
    }

    private static void mapMapToObject(Map map, Object object)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        Map<String, Object> temporary = new LinkedHashMap();
        temporary.putAll(map);

        for (Field field : object.getClass().getDeclaredFields()) {
            for (KeyValueTarget ann : field.getDeclaredAnnotationsByType(KeyValueTarget.class)) {
                String targetName = ann.value();

                if (targetName.isEmpty()) {
                    targetName = field.getName();
                }

                Object value = temporary.remove(targetName);

                if (value != null) {
                    Class<?> type = field.getType();
                    field.setAccessible(true);

                    if (value instanceof String) {
                        String valueString = value.toString();

                        if (type == String.class) {
                            field.set(object, value);
                        } else if (type == int.class) {
                            field.setInt(object, Integer.parseInt(valueString));
                        } else if (type == double.class) {
                            field.setDouble(object, Double.parseDouble(valueString));
                        } else if (type == Integer.class) {
                            field.set(object, Integer.parseInt(valueString));
                        } else if (type == Double.class) {
                            field.set(object, Double.parseDouble(valueString));
                        } else if (Collection.class.isAssignableFrom(type)) {
                            ((Collection<String>) field.get(object)).add(valueString);
                        }
                    } else {
                        if (Map.class.isAssignableFrom(type)) {
                            Map<String, Object> target = (Map<String, Object>) field.get(object);

                            for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
                                target.put(entry.getKey(), entry.getValue());
                            }
                        } else {
                            Constructor<?> constructor = type.getConstructor();
                            Object sub = constructor.newInstance();

                            mapMapToObject((Map) value, sub);
                        }
                    }
                }
            }
        }

        for (Field field : object.getClass().getDeclaredFields()) {
            KeyValueConsumer consumer = field.getDeclaredAnnotation(KeyValueConsumer.class);

            if (consumer != null) {
                if (!Map.class.isAssignableFrom(field.getType())) {
                    throw new IllegalArgumentException("Not a map");
                }

                field.setAccessible(true);

                Map<String, Object> target = (Map<String, Object>) field.get(object);

                for (Map.Entry<String, Object> mapEntry : temporary.entrySet()) {
                    boolean isIgnored = false;

                    for (String ignored : consumer.ignored()) {
                        if (mapEntry.getKey().equals(ignored)) {
                            isIgnored = true;
                            break;
                        }
                    }

                    if (isIgnored) {
                        continue;
                    }

                    Constructor<?> constructor = consumer.value().getConstructor();
                    Object sub = constructor.newInstance();

                    mapMapToObject((Map) mapEntry.getValue(), sub);

                    target.put(mapEntry.getKey(), sub);
                }
            }
        }
    }

    public static <T> T parseKV(String content, Class<T> type, boolean stripTopLevel) {
        try {
            Map<String, Object> rawObject = parseKV(content);

            if (stripTopLevel) {
                while (rawObject.size() == 1) {
                    rawObject = (Map<String, Object>) rawObject.get(rawObject.keySet().iterator().next());
                }
            }

            Constructor<T> constructor = type.getConstructor();
            T result = constructor.newInstance();

            mapMapToObject(rawObject, result);

            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String convert(Map<String, Object> data, String indent) {
        StringBuilder builder = new StringBuilder();

        Function<String, BiConsumer<String, Object>> f
                = template -> (k, v) -> {
            if (v instanceof String) {
                builder.append(String.format(indent + template, k, "\"" + v + "\""));
            } else {
                String result = convert((Map<String, Object>) v, indent + "\t");
                String firstLine = builder.length() > 0 ? "\n" : "";

                builder.append(String.format(firstLine + indent + template, k, "{\n" + result + indent + "}"));
            }
        };

        data.forEach(f.apply("\"%s\" %s\n"));

        return builder.toString();
    }
}
