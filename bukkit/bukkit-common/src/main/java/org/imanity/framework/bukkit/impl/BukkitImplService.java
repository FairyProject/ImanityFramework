package org.imanity.framework.bukkit.impl;

import org.imanity.framework.*;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.command.presence.DefaultPresenceProvider;
import org.imanity.framework.bukkit.impl.server.ServerImplementation;
import org.imanity.framework.command.CommandService;

@Service(name = "bukkit-impl", dependencies = "command")
public class BukkitImplService {

    private final BeanContext beanContext;

    @BeanConstructor
    public BukkitImplService(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @PreInitialize
    public void preInit() {
        CommandService commandService = ImanityCommon.getBean(CommandService.class);

        commandService.registerDefaultPresenceProvider(new DefaultPresenceProvider());

        Imanity.IMPLEMENTATION = ServerImplementation.load(this.beanContext);
    }

}
