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

package org.imanity.framework.locale;

import org.imanity.framework.*;

@Component
public class LocaleSerializer implements ObjectSerializer<Locale, String> {

    @Autowired
    private LocaleHandler localeHandler;

    @ShouldInitialize
    public boolean configure() {
        return ImanityCommon.CORE_CONFIG.USE_LOCALE;
    }

    @Override
    public String serialize(Locale input) {
        return input.getName();
    }

    @Override
    public Locale deserialize(String output) {
        return this.localeHandler.getLocale(output);
    }

    @Override
    public Class<Locale> inputClass() {
        return Locale.class;
    }

    @Override
    public Class<String> outputClass() {
        return String.class;
    }
}
