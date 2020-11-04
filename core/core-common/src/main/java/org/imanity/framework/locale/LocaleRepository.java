package org.imanity.framework.locale;

import org.imanity.framework.*;
import org.imanity.framework.cache.EnableOwnCacheManager;
import org.imanity.framework.cache.Unless;
import org.imanity.framework.locale.player.LocaleData;

import java.util.UUID;

@EnableOwnCacheManager
@Service(name = "locale-repository")
public class LocaleRepository extends MongoRepository<LocaleData, UUID> {
    @Override
    public String name() {
        return "locale";
    }

    @Override
    public Class<LocaleData> type() {
        return LocaleData.class;
    }

    @Cacheable(key = "locale-$(arg0)", unless = { Unless.ResultIsNull.class })
    public LocaleData find(UUID uuid) {
        return super.findById(uuid).orElse(new LocaleData(uuid));
    }

    @CacheEvict("locale-$(arg0.uuid)")
    public <S extends LocaleData> S save(S localeData) {
        return super.save(localeData);
    }

    @PostInitialize
    @Cacheable.ClearAfter
    public void stop() {

    }
}
