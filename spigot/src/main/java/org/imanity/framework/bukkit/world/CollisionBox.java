package org.imanity.framework.bukkit.world;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.packet.wrapper.wrapped.WrappedEnumParticle;

import java.util.Collection;
import java.util.List;

public interface CollisionBox {
    boolean isColliding(CollisionBox other);

    void draw(WrappedEnumParticle particle, Collection<? extends Player> players);

    CollisionBox copy();

    CollisionBox shift(double x, double y, double z);

    //void downCast(List<SimpleCollisionBox> list);

    boolean isNull();
}