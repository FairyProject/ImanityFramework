package org.imanity.framework.metadata;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonMetadataRegistries {

    public static final AbstractMetadataRegistry<UUID> PLAYERS = new AbstractMetadataRegistry<>();

    public static MetadataMap provide(UUID uuid) {
        return PLAYERS.provide(uuid);
    }

    @Nullable
    public static MetadataMap getOrNull(UUID uuid) {
        return PLAYERS.get(uuid).orElse(null);
    }

    public static Optional<MetadataMap> get(UUID uuid) {
        return PLAYERS.get(uuid);
    }

}
