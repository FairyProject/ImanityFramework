package org.imanity.framework.bukkit.nametag;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.plugin.service.Autowired;

import java.beans.ConstructorProperties;

@Getter
public abstract class NameTagAdapter {

    @Autowired
    private static NameTagService nameTagService;

    public static final NameTagInfo createNametag(final String prefix, final String suffix) {
        return nameTagService.getOrCreate(prefix, suffix);
    }

    private final String name;
    private final int weight;

    public abstract NameTagInfo fetch(final Player receiver, final Player target);

    @ConstructorProperties({ "name", "weight" })
    public NameTagAdapter(final String name, final int weight) {
        this.name = name;
        this.weight = weight;
        ImanityCommon.SERVICE_HANDLER.registerAutowired(this);
    }

}
