package com.ilummc.tlib.util;

import java.util.Arrays;

public class Strings {

    public static boolean isBlank(String var) {
        return var == null || var.trim().isEmpty();
    }

    public static boolean isEmpty(CharSequence var) {
        return var == null || var.length() == 0;
    }

    
    public static String replaceWithOrder(String template, Object... args) {
        if (args.length == 0 || template.length() == 0) {
            return template;
        }
        char[] arr = template.toCharArray();
        StringBuilder stringBuilder = new StringBuilder(template.length());
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == '{' && Character.isDigit(arr[Math.min(i + 1, arr.length - 1)])
                    && arr[Math.min(i + 1, arr.length - 1)] - '0' < args.length
                    && arr[Math.min(i + 2, arr.length - 1)] == '}') {
                stringBuilder.append(args[arr[i + 1] - '0']);
                i += 2;
            } else {
                stringBuilder.append(arr[i]);
            }
        }
        return stringBuilder.toString();
    }

    // *********************************
    //
    //           Deprecated
    //
    // *********************************

    public static String replaceWithOrder(String template, String... args) {
        return replaceWithOrder(template, (Object[]) args);
    }

    public static String replaceWithOrderI(String template, Integer... args) {
        return replaceWithOrder(template, Arrays.toString(args));
    }
}
