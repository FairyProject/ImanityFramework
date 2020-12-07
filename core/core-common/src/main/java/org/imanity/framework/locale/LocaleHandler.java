/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
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

package org.imanity.framework.locale;

import lombok.Getter;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.PostInitialize;
import org.imanity.framework.Service;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(name = "locale")
public class LocaleHandler {

    private Map<String, Locale> locales;
    @Getter
    private Locale defaultLocale;

    @PostInitialize
    public void init() {
        this.locales = new HashMap<>();
        this.defaultLocale = this.getOrRegister(ImanityCommon.CORE_CONFIG.DEFAULT_LOCALE);
        this.getOrRegister("zh_tw"); // test
    }

    public Locale getOrRegister(String name) {
        Locale locale;

        if (this.locales.containsKey(name)) {
            locale = this.locales.get(name);
        } else {
            locale = new Locale(name);
            this.locales.put(name, locale);
        }

        return locale;
    }

    public Locale registerFromYml(File file) {

        Map<String, Object> map = ImanityCommon.PLATFORM.loadYaml(file);
        String name = map.get("locale").toString();

        Locale locale = this.getOrRegister(name);

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey().equals("locale")) {
                continue;
            }
            locale.registerEntry(entry.getKey(), entry.getValue().toString());
        }

        return locale;

    }

    public Locale registerFromYml(InputStream inputStream) {
        Map<String, Object> map = ImanityCommon.PLATFORM.loadYaml(inputStream);
        String name = map.get("locale").toString();

        Locale locale = this.getOrRegister(name);

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey().equals("locale")) {
                continue;
            }

            if (entry.getValue() instanceof List) {
                List list = (List) entry.getValue();
                locale.registerEntry(entry.getKey(), (String[]) list.stream().map(Object::toString).toArray(String[]::new));
            } else if (entry.getValue() instanceof String) {
                locale.registerEntry(entry.getKey(), entry.getValue().toString());
            }
        }

        return locale;
    }

    public void unregisterLocale(String name) {
        this.locales.remove(name);
    }

    public Locale getLocale(String name) {
        if (this.locales.containsKey(name)) {
            return this.locales.get(name);
        }

        return null;
    }

}
