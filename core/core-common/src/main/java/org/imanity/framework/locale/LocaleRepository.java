package org.imanity.framework.locale;

import org.imanity.framework.*;
import org.imanity.framework.cache.EnableOwnCacheManager;
import org.imanity.framework.cache.Unless;
import org.imanity.framework.locale.player.LocaleData;

import java.io.File;
import java.nio.file.Path;
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

    @Cacheable(forever = true, key = "locale-$(arg0)")
    public LocaleData find(UUID uuid) {
        return super.findById(uuid).orElse(new LocaleData(uuid));
    }

    @CacheEvict("locale-$(arg0.uuid)")
    public <S extends LocaleData> S save(S localeData) {
        return super.save(localeData);
    }

    @PostDestroy
    @Cacheable.ClearAfter
    public void stop() {

    }

}
