/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.imanity.framework.util;

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
