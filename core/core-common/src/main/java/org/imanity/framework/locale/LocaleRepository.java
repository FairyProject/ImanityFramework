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
import org.imanity.framework.cache.EnableOwnCacheManager;
import org.imanity.framework.locale.player.LocaleData;
import org.imanity.framework.util.Utility;

import java.util.UUID;

@EnableOwnCacheManager
@Service(name = "locale-repository")
public class LocaleRepository extends ConfigurableRepository<LocaleData, UUID> {

    @Override
    public RepositoryType repositoryType() {
        return ImanityCommon.CORE_CONFIG.STORAGE.DEFAULT_TYPE;
    }

    @Override
    public String name() {
        return "locale";
    }

    @Override
    public Class<LocaleData> type() {
        return LocaleData.class;
    }

    @Cacheable(forever = true, key = "'locale-' + #args[0]")
    public LocaleData find(UUID uuid) {
        return super.findById(uuid).orElse(new LocaleData(uuid));
    }

    @CacheEvict("'locale-' + #args[0].getUuid()")
    public <S extends LocaleData> S save(S localeData) {
        return super.save(localeData);
    }

    @PostDestroy
    @Cacheable.ClearAfter
    public void stop() {

    }

    @ShouldInitialize
    public boolean configure() {
        return ImanityCommon.CORE_CONFIG.USE_LOCALE;
    }

}
