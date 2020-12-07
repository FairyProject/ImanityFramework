package org.imanity.framework;

import lombok.NonNull;
import org.imanity.framework.config.format.FieldNameFormatters;
import org.imanity.framework.config.yaml.YamlConfiguration;

import java.io.Serializable;
import java.nio.file.Path;

public abstract class YamlRepository<T, ID extends Serializable> extends ConfigurableRepository<T, ID> {

    private YamlImpl yaml;

    @NonNull
    public abstract Path path();

    protected void ensureYamlLoaded() {
        if (this.yaml != null) {
            return;
        }

        this.yaml = new YamlImpl(this.path());
    }

    @Override
    public RepositoryType repositoryType() {
        return this.yaml.TYPE;
    }

    protected static class YamlImpl extends YamlConfiguration {

        protected RepositoryType TYPE;

        protected YamlImpl(Path path) {
            super(path, YamlProperties.builder()
                    .setFormatter(FieldNameFormatters.LOWER_CASE)
                    .build());
        }
    }
}
