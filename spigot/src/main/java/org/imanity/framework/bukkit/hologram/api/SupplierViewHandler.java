package org.imanity.framework.bukkit.hologram.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class SupplierViewHandler implements ViewHandler {

    private final Supplier<String> supplier;

    @Override
    public String view(@Nullable Player player) {
        return supplier.get();
    }
}
