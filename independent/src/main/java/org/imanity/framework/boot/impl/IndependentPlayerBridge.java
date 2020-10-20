package org.imanity.framework.boot.impl;

import lombok.RequiredArgsConstructor;
import org.imanity.framework.boot.user.UserInterface;
import org.imanity.framework.player.IPlayerBridge;

import java.util.Collection;
import java.util.UUID;

@RequiredArgsConstructor
public class IndependentPlayerBridge implements IPlayerBridge {

    private final UserInterface userInterface;

    @Override
    public Collection getOnlinePlayers() {
        return this.userInterface.getAllUsers();
    }

    @Override
    public UUID getUUID(Object o) {
        return this.userInterface.uuid(this.getPlayerClass().cast(o));
    }

    @Override
    public String getName(Object o) {
        return this.userInterface.name(this.getPlayerClass().cast(o));
    }

    @Override
    public Class<?> getPlayerClass() {
        return this.userInterface.type();
    }
}
