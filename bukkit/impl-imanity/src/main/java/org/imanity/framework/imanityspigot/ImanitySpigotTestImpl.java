package org.imanity.framework.imanityspigot;

import org.imanity.framework.bukkit.impl.test.ImplementationTest;
import org.imanity.framework.bukkit.util.SpigotUtil;

public class ImanitySpigotTestImpl implements ImplementationTest {

    @Override
    public boolean test() {
        return SpigotUtil.SPIGOT_TYPE == SpigotUtil.SpigotType.IMANITY;
    }
}
